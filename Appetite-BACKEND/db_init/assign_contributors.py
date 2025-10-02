#!/usr/bin/env python3
"""
Assign contributor_id to the first 100 meals in meals.json.

- Takes the first 100 meals in order and assigns UIDs in blocks of 20.
- Defaults to in-place update (writes back to the same file) and creates a .bak backup.
- Use --out to write to a different output file.

Usage: 
    python db_init/assign_contributors.py --in db_init/100meals_raw.json --out db_init/meals_with_contributors.json

"""

import argparse
import json
import shutil
import random
from pathlib import Path

# Replace with your actual Firebase Auth UIDs
DEFAULT_UIDS = [
    "DOWx7tYj1mWpPnHP3gIITMD7Cnl2"
    "GQLfXrDMBqYI5uaEYkw0LSzTVGZ2",
    "fSELsXbX7rNX65jTQhjSEwKO03g2",
    "lHEa94N7iCNeVd3T1pteoQ2Fhdh2",
    "TZtKbXI1YsPvchXYZLQ02srRQkH3",
    "1KD0XKqVirO10ALwqcys8peVgPD2"  # you had 6 in your Firebase
]

def assign_contributors(meals, uids, overwrite=False):
    """
    Randomly assign contributor_id from the given UIDs to each meal.
    """
    n = min(100, len(meals))
    for i in range(n):
        uid = random.choice(uids)  # <-- random assignment instead of block
        if overwrite or "contributor_id" not in meals[i] or not meals[i].get("contributor_id"):
            meals[i]["contributor_id"] = uid
    return n

def main():
    p = argparse.ArgumentParser(description="Randomly assign contributor_id to meals in meals.json")
    p.add_argument("--in", dest="inp", default="100meals_raw.json", help="Input JSON file (default: meals.json)")
    p.add_argument("--out", dest="out", default="meals_with_contributors", help="Output JSON file (default: overwrite input)")
    p.add_argument("--overwrite", action="store_true", help="Overwrite existing contributor_id if present")
    p.add_argument("--uids", nargs="+", metavar="UID", help="Contributor UIDs (default: Firebase UIDs above)")
    args = p.parse_args()

    in_path = Path(args.inp)
    if not in_path.exists():
        raise SystemExit(f"Input not found: {in_path}")

    # Load
    with in_path.open("r", encoding="utf-8") as f:
        data = json.load(f)

    if not isinstance(data, dict) or "meals" not in data or not isinstance(data["meals"], list):
        raise SystemExit("Invalid input JSON: expected object with key 'meals' as a list.")

    uids = args.uids if args.uids else DEFAULT_UIDS
    if len(uids) < 1:
        raise SystemExit("At least one UID is required.")

    # Assign
    assigned = assign_contributors(data["meals"], uids, overwrite=args.overwrite)
    print(f"Assigned contributor_id to {assigned} meal(s).")

    # Write
    out_path = Path(args.out) if args.out else in_path
    if args.out is None:
        # in-place: make a backup
        backup_path = in_path.with_suffix(in_path.suffix + ".bak")
        shutil.copyfile(in_path, backup_path)
        print(f"Backup created at {backup_path}")

    with out_path.open("w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    print(f"Wrote updated JSON to {out_path}")

if __name__ == "__main__":
    main()
