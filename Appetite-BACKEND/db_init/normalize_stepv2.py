#!/usr/bin/env python3
import os, re
import firebase_admin
from firebase_admin import credentials, firestore

# ---------- CONFIG ----------
COLLECTION = "recipes"
BATCH_SIZE = 400
DRY_RUN = False
# ----------------------------

def ensure_app():
    if not firebase_admin._apps:
        cred_path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
        if cred_path and os.path.isfile(cred_path):
            firebase_admin.initialize_app(credentials.Certificate(cred_path))
        else:
            firebase_admin.initialize_app()
    return firestore.client()

def split_by_dot(text: str):
    """Split text by '.' into clean steps."""
    if not text:
        return []
    # Normalize spacing
    text = text.replace("\r\n", " ").replace("\n", " ").strip()
    # Split by '.' and strip whitespace
    parts = [p.strip() for p in text.split(".") if p.strip()]
    return parts

def main():
    db = ensure_app()
    coll = db.collection(COLLECTION)

    total = 0
    updated = 0
    batch = db.batch()
    in_batch = 0

    for snap in coll.stream():
        total += 1
        doc = snap.to_dict() or {}

        # Case 1: already migrated and has steps (list of strings)
        if "steps" in doc and isinstance(doc["steps"], list) and doc["steps"]:
            merged = " ".join(doc["steps"])
            steps = split_by_dot(merged)

        # Case 2: still has old "instructions"
        elif "instructions" in doc and isinstance(doc["instructions"], str):
            steps = split_by_dot(doc["instructions"])

        else:
            continue  # nothing to update

        update_payload = {
            "steps": steps,
            "n_steps": len(steps),
            "instructions": firestore.DELETE_FIELD,  # ensure removed
        }

        updated += 1
        if DRY_RUN:
            print(f"[DRY-RUN] {snap.id} → {len(steps)} steps")
            continue

        batch.update(snap.reference, update_payload)
        in_batch += 1

        if in_batch >= BATCH_SIZE:
            batch.commit()
            batch = db.batch()
            in_batch = 0
            print(f"Committed batch. Progress: {updated}/{total} docs updated.")

    if not DRY_RUN and in_batch > 0:
        batch.commit()

    print("---- DONE ----")
    print(f"Docs scanned:  {total}")
    print(f"Docs updated:  {updated}")

if __name__ == "__main__":
    main()
