#!/usr/bin/env python3
"""
Import TheMealDB meals.json into Firebase Firestore + seed synthetic users.

Usage:
  export GOOGLE_APPLICATION_CREDENTIALS=/path/to/serviceAccount.json
  python db_init/import_themealdb.py --in db_init/meals.json --limit 100 --merge
"""

import os
import json
import argparse
from typing import Any, Dict, List, Optional

from tqdm import tqdm

import firebase_admin
from firebase_admin import credentials, firestore

# ---------------------------
# Firebase init
# ---------------------------
def ensure_app():
    if not firebase_admin._apps:
        cred_path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
        if not cred_path:
            raise ValueError("GOOGLE_APPLICATION_CREDENTIALS not set in environment variables")
        cred = credentials.Certificate(cred_path)
        firebase_admin.initialize_app(cred)
    return firestore.client()

# ---------------------------
# Contributor (user) defaults
# ---------------------------
DEFAULT_UIDS = [
    "DOWx7tYj1mWpPnHP3gIITMD7Cnl2",
    "GQLfXrDMBqYI5uaEYkw0LSzTVGZ2",
    "fSELsXbX7rNX65jTQhjSEwKO03g2",
    "lHEa94N7iCNeVd3T1pteoQ2Fhdh2",
    "TZtKbXI1YsPvchXYZLQ02srRQkH3",
]

SYNTHETIC_USERS = [
    {"uid": DEFAULT_UIDS[0], "name": "Kim Chau", "email": "lekimchau@gmail.com", "bio": "Love Cooking Italian food"},
    {"uid": DEFAULT_UIDS[1], "name": "Con Meo", "email": "conmeo@gmail.com", "bio": "Love Cooking Italian food"},
    {"uid": DEFAULT_UIDS[2], "name": "Con Cho", "email": "concho@gmail.com", "bio": "Love Cooking Italian food"},
    {"uid": DEFAULT_UIDS[3], "name": "Con Ca", "email": "conca@gmail.com", "bio": "Love Cooking Italian food"},
    {"uid": DEFAULT_UIDS[4], "name": "Con Cao", "email": "concao@gmail.com", "bio": "Love Cooking Italian food"},
]

TARGET_PER_USER = 20  # <-- each user should have 20 recipes

# ---------------------------
# Normalization helpers
# ---------------------------
def _clean(s: Optional[str]) -> Optional[str]:
    if s is None:
        return None
    s2 = str(s).strip()
    return s2 if s2 else None

def split_tags(str_tags: Optional[str]) -> List[str]:
    if not str_tags:
        return []
    tags = [t.strip() for t in str(str_tags).split(",")]
    return [t for t in tags if t]

def extract_ingredients(meal: Dict[str, Any]) -> List[Dict[str, str]]:
    out: List[Dict[str, str]] = []
    for i in range(1, 21):
        ing = _clean(meal.get(f"strIngredient{i}"))
        mea = _clean(meal.get(f"strMeasure{i}"))
        if ing:
            out.append({"ingredient": ing, "measure": mea or ""})
    return out

def build_search_keywords(name: Optional[str], category: Optional[str], area: Optional[str], tags: List[str]) -> List[str]:
    parts = []
    if name: parts += str(name).lower().split()
    if category: parts += str(category).lower().split()
    if area: parts += str(area).lower().split()
    parts += [t.lower() for t in tags]
    seen = set()
    result = []
    for p in parts:
        if p and p not in seen:
            seen.add(p)
            result.append(p)
    return result

def normalize_meal(meal: Dict[str, Any]) -> Dict[str, Any]:
    name = _clean(meal.get("strMeal"))
    category = _clean(meal.get("strCategory"))
    area = _clean(meal.get("strArea"))
    instructions = _clean(meal.get("strInstructions"))
    thumb = _clean(meal.get("strMealThumb"))
    youtube = _clean(meal.get("strYoutube"))
    source_url = _clean(meal.get("strSource"))
    str_tags = _clean(meal.get("strTags"))
    tags = split_tags(str_tags)
    ingredients = extract_ingredients(meal)

    return {
        "name": name or "",
        "category": category,
        "area": area,
        "instructions": instructions,
        "thumbnail": thumb,
        "youtubeUrl": youtube,
        "sourceUrl": source_url,
        "tags": tags,
        "ingredients": ingredients,
        "minutes": 0,
        "contributorId": None, 
        "contributorName": None, # will be set below
        "n_steps": len(instructions.split("\n")) if instructions else 0,
        "n_ingredients": len(ingredients),
        "ratingAvg": 0.0,
        "ratingCount": 0,
        "ratingBuckets": {"1": 0, "2": 0, "3": 0, "4": 0, "5": 0},
        "popularity": 0,
        "lastInteractionAt": None,
        "searchKeywords": build_search_keywords(name, category, area, tags),
        "source": "TheMealDB",
        "externalId": _clean(meal.get("idMeal")),
    }

# ---------------------------
# Firestore uploader
# ---------------------------
def chunk_iterable(seq, n):
    for i in range(0, len(seq), n):
        yield seq[i:i + n]

def make_assignment_order(num_meals: int) -> List[str]:
    """
    Build an ordered list of user IDs of length `num_meals` such that:
      - First TARGET_PER_USER items for each user are guaranteed (i.e., 20 each)
      - Remaining slots (if any) are distributed round-robin across users
    """
    base = []
    # guarantee 20 per user first
    for uid in DEFAULT_UIDS:
        base.extend([uid] * TARGET_PER_USER)  # 20 occurrences of uid

    if num_meals <= len(base):
        return base[:num_meals]

    # extra meals beyond 20*len(users): round-robin
    extras = num_meals - len(base)
    rr = []
    i = 0
    while len(rr) < extras:
        rr.append(DEFAULT_UIDS[i % len(DEFAULT_UIDS)])
        i += 1

    return base + rr

def upload_meals(db, meals: List[Dict[str, Any]], id_prefix: str, merge: bool):
    """
    Assign contributorId IGNORING any present in JSON:
      - Ensure each of the DEFAULT_UIDS gets TARGET_PER_USER recipes.
      - Extra recipes (if any) are assigned round-robin.
    """
    to_write = []
    uid_to_name = {u["uid"]: u.get("name", "Unknown") for u in SYNTHETIC_USERS}
    assignment = make_assignment_order(len(meals))
    print(f"Assignment plan: {len(DEFAULT_UIDS)} users × {TARGET_PER_USER} each, "
          f"{'+' + str(len(meals) - len(DEFAULT_UIDS)*TARGET_PER_USER) + ' extra' if len(meals) > len(DEFAULT_UIDS)*TARGET_PER_USER else 'no extras'}.")

    for idx, m in enumerate(meals):
        id_meal = _clean(m.get("idMeal"))
        if not id_meal:
            continue
        doc_id = f"{id_prefix}{id_meal}"
        doc = normalize_meal(m)

        # Force contributorId from the plan
        uid = assignment[idx]
        doc["contributorId"] = uid
        doc["contributorName"] = uid_to_name.get(uid, "Unknown")

        to_write.append((doc_id, doc))

    print(f"Uploading {len(to_write)} recipes to /recipes ...")
    for batch in tqdm(list(chunk_iterable(to_write, 450)), desc="Firestore meal batches", unit="batch"):
        wb = db.batch()
        for doc_id, doc in batch:
            ref = db.collection("recipes").document(doc_id)
            if merge:
                wb.set(ref, doc, merge=True)
            else:
                wb.set(ref, doc)
        wb.commit()

def upload_users(db, users: List[Dict[str, Any]]):
    print(f"Uploading {len(users)} synthetic users to /users ...")
    for batch in tqdm(list(chunk_iterable(users, 450)), desc="Firestore user batches", unit="batch"):
        wb = db.batch()
        for user in batch:
            ref = db.collection("users").document(user["uid"])
            wb.set(ref, user, merge=True)
        wb.commit()

# ---------------------------
# Main
# ---------------------------
def main():
    ap = argparse.ArgumentParser(description="Import TheMealDB JSON into Firestore /recipes + seed /users")
    ap.add_argument("--in", dest="inp", required=True, help="Path to meals.json")
    ap.add_argument("--limit", type=int, default=None, help="Import at most N meals (default: all)")
    ap.add_argument("--id-prefix", type=str, default="", help="Prefix for recipe doc IDs (default: '')")
    ap.add_argument("--merge", action="store_true", help="Use merge=True for writes (default: overwrite doc)")
    args = ap.parse_args()

    with open(args.inp, "r", encoding="utf-8") as f:
        payload = json.load(f)

    if not isinstance(payload, dict) or "meals" not in payload or not isinstance(payload["meals"], list):
        raise SystemExit("Invalid input JSON: expected top-level object with key 'meals' as a list.")

    meals = payload["meals"]
    if args.limit is not None:
        meals = meals[: max(0, args.limit)]

    db = ensure_app()

    # Upload users first
    upload_users(db, SYNTHETIC_USERS)

    # Upload meals (forces contributorId assignment: 20 per user)
    upload_meals(db, meals, id_prefix=args.id_prefix, merge=args.merge)

    print("Done.")

if __name__ == "__main__":
    main()
