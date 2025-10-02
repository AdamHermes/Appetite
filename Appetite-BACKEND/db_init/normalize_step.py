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

def split_instructions(text: str):
    """Split instructions into clean steps."""
    if not text:
        return []
    # Normalize newlines
    text = text.replace("\r\n", "\n").strip()
    # First split on newlines
    parts = [p.strip() for p in text.split("\n") if p.strip()]
    if len(parts) > 1:
        return parts
    # Fallback: split into sentences by period
    sentences = re.split(r'(?<=[.!?])\s+', text)
    return [s.strip() for s in sentences if s.strip()]

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

        instr = doc.get("instructions")
        if not instr:
            continue

        steps = split_instructions(instr)
        update_payload = {
            "steps": steps,
            "n_steps": len(steps),
            "instructions": firestore.DELETE_FIELD,
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
