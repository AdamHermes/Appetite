#!/usr/bin/env python3
"""
Deduplicate recipes by 'mealDbId'.

Keeps exactly one document per mealDbId:
  1) Prefer a doc with Firestore auto-ID (20+ chars, not all digits)
  2) Among those, prefer newest createdAt (if present)
  3) Else keep first seen

Deletes all other duplicates. Run with DRY_RUN=True first.
"""

import os
from typing import Dict, List, Any, Tuple

import firebase_admin
from firebase_admin import credentials, firestore

# -------- CONFIG --------
COLLECTION = "recipes"
DRY_RUN = False           # Preview only; set to False to actually delete
BATCH_SIZE = 400         # < 500 (Firestore limit)
# ------------------------

def ensure_app():
    if not firebase_admin._apps:
        cred_path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
        if cred_path and os.path.isfile(cred_path):
            firebase_admin.initialize_app(credentials.Certificate(cred_path))
        else:
            firebase_admin.initialize_app()  # ADC / Emulator
    return firestore.client()

def is_auto_id(doc_id: str) -> bool:
    return len(doc_id) >= 20 and not doc_id.isdigit()

def created_at_of(doc: Dict[str, Any]):
    # May be None or a Firestore DatetimeWithNanoseconds
    return doc.get("createdAt")

def choose_keeper(snaps: List[firestore.DocumentSnapshot]) -> firestore.DocumentSnapshot:
    # 1) Prefer auto-ID docs
    auto = [s for s in snaps if is_auto_id(s.id)]
    pool = auto if auto else snaps

    # 2) Prefer newest createdAt (if present)
    def key_fn(s: firestore.DocumentSnapshot):
        d = s.to_dict() or {}
        ts = created_at_of(d)
        has_ts = ts is not None
        return (has_ts, ts)

    # max() with (has_ts, ts) → any doc with ts is > doc without ts; among ts, later ts wins
    return max(pool, key=key_fn)

def main():
    db = ensure_app()
    coll = db.collection(COLLECTION)

    print("Scanning for duplicates by mealDbId ...")

    groups: Dict[str, List[firestore.DocumentSnapshot]] = {}
    total_scanned = 0
    considered = 0

    # Only consider docs that have mealDbId (using a where filter reduces reads)
    # If you prefer client-side filtering, comment this and just coll.stream()
    query = coll.where("mealDbId", ">", "")  # matches present & non-empty strings
    # Note: if some mealDbId are numeric, they won't match this string filter.
    # Fallback: stream all and filter in Python:
    snaps = list(coll.stream())

    for snap in snaps:
        total_scanned += 1
        d = snap.to_dict() or {}
        if "mealDbId" not in d:
            continue
        meal_id = d["mealDbId"]
        if meal_id is None:
            continue
        meal_id = str(meal_id).strip()
        if not meal_id:
            continue

        considered += 1
        groups.setdefault(meal_id, []).append(snap)

    dup_groups: List[Tuple[str, List[firestore.DocumentSnapshot]]] = [
        (k, v) for k, v in groups.items() if len(v) > 1
    ]

    print(f"Docs scanned: {total_scanned}")
    print(f"Docs with mealDbId: {considered}")
    print(f"Duplicate groups: {len(dup_groups)}")

    if not dup_groups:
        print("No duplicates found. Done.")
        return

    # Decide deletions
    to_delete: List[firestore.DocumentReference] = []
    for meal_id, snaps in dup_groups:
        keeper = choose_keeper(snaps)
        deletions = [s.reference for s in snaps if s.id != keeper.id]

        ids_all = [s.id for s in snaps]
        ids_del = [s.id for s in snaps if s.id != keeper.id]
        print(f"mealDbId={meal_id} -> keep {keeper.id}, delete {ids_del} (all: {ids_all})")

        to_delete.extend(deletions)

    if DRY_RUN:
        print(f"[DRY-RUN] Would delete {len(to_delete)} documents.")
        return

    # Execute deletes in batches
    deleted = 0
    batch = db.batch()
    in_batch = 0

    for ref in to_delete:
        batch.delete(ref)
        in_batch += 1
        if in_batch >= BATCH_SIZE:
            batch.commit()
            deleted += in_batch
            print(f"Committed batch; total deleted so far: {deleted}")
            batch = db.batch()
            in_batch = 0

    if in_batch > 0:
        batch.commit()
        deleted += in_batch

    print("---- DONE ----")
    print(f"Duplicates deleted: {deleted}")

if __name__ == "__main__":
    main()
