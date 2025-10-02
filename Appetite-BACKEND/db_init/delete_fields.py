import os, sys
from typing import List
import firebase_admin
from firebase_admin import credentials, firestore

# ------------- CONFIG -------------
COLLECTION = "recipes"
FIELDS_TO_DELETE: List[str] = ["sourceUrl", "ratingCount", "ratingBuckets", "ratingAvg", "externalId"]
BATCH_SIZE = 400        # Firestore max 500; keep headroom
DRY_RUN = False         # Set True to preview changes without writing
# ----------------------------------

def ensure_app():
    if not firebase_admin._apps:
        cred_path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
        if cred_path and os.path.isfile(cred_path):
            firebase_admin.initialize_app(credentials.Certificate(cred_path))
        else:
            # Falls back to ADC if available
            firebase_admin.initialize_app()
    return firestore.client()

def main():
    db = ensure_app()
    coll = db.collection(COLLECTION)

    total = 0
    changed = 0

    batch = db.batch()
    in_batch = 0

    # Stream all docs. For very large collections, consider paginating with .limit() + start_after().
    for snap in coll.stream():
        total += 1
        doc_data = snap.to_dict() or {}

        # Build per-doc update mask using DELETE_FIELD for fields that are present
        update_payload = {}
        for f in FIELDS_TO_DELETE:
            # Only delete if present to keep writes minimal
            if f in doc_data:
                update_payload[f] = firestore.DELETE_FIELD

        if not update_payload:
            continue  # nothing to do for this doc

        changed += 1
        if DRY_RUN:
            print(f"[DRY-RUN] Would delete {list(update_payload.keys())} from {snap.id}")
            continue

        batch.update(snap.reference, update_payload)
        in_batch += 1

        if in_batch >= BATCH_SIZE:
            batch.commit()
            batch = db.batch()
            in_batch = 0
            print(f"Committed a batch. Progress: {changed}/{total} docs modified/seen.")

    # Commit any remaining ops
    if not DRY_RUN and in_batch > 0:
        batch.commit()

    print("---- DONE ----")
    print(f"Docs scanned:  {total}")
    print(f"Docs updated:  {changed}")
    print(f"Fields removed: {FIELDS_TO_DELETE}")

if __name__ == "__main__":
    # Usage:
    #   export GOOGLE_APPLICATION_CREDENTIALS=/path/to/serviceAccount.json
    #   python delete_fields.py
    main()
