#!/usr/bin/env python3
"""
Migrate recipes docs with 5-digit IDs to Firestore auto-IDs.
Copies each matching doc into a new auto-ID doc and stores the old 5-digit ID in field "mealDbId".
Skips docs that already have Firestore-style auto-IDs.
"""

import os
import firebase_admin
from firebase_admin import credentials, firestore

# -------- CONFIG --------
COLLECTION = "recipes"
DRY_RUN = False   # Set True to preview changes without writing
# ------------------------

def ensure_app():
    if not firebase_admin._apps:
        cred_path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
        if cred_path and os.path.isfile(cred_path):
            firebase_admin.initialize_app(credentials.Certificate(cred_path))
        else:
            firebase_admin.initialize_app()  # fallback: ADC or emulator
    return firestore.client()

def is_mealdb_id(doc_id: str) -> bool:
    """Return True if the doc_id looks like a 5-digit numeric MealDB ID."""
    return doc_id.isdigit() and len(doc_id) == 5

def migrate():
    db = ensure_app()
    coll = db.collection(COLLECTION)

    total = 0
    migrated = 0
    skipped = 0

    for snap in coll.stream():
        total += 1
        old_id = snap.id

        if not is_mealdb_id(old_id):
            skipped += 1
            continue  # already auto-ID, skip

        data = snap.to_dict() or {}
        data["mealDbId"] = old_id  # preserve old ID in field

        if DRY_RUN:
            print(f"[DRY-RUN] Would migrate doc {old_id} → auto-ID with mealDbId={old_id}")
            continue

        # Create new doc with Firestore auto-ID
        new_ref = coll.document()
        new_ref.set(data)

        # (optional) delete the old doc after copying
        snap.reference.delete()

        migrated += 1
        print(f"Migrated {old_id} → {new_ref.id} (mealDbId={old_id})")

    print("---- DONE ----")
    print(f"Docs scanned:   {total}")
    print(f"Docs migrated:  {migrated}")
    print(f"Docs skipped:   {skipped}")

if __name__ == "__main__":
    migrate()
