import uuid
from typing import Any, Dict, List, Optional
from firebase_admin import firestore, storage
from app.db.firestore import db
from app.schemas.recipes import RecipeCreate
from app.utils.search import fuzzy_filter, make_keywords  # ensure make_keywords exists
from app.utils.users import get_user_public
import cloudinary

ALLOWED_MIME = {"image/jpeg", "image/png", "image/webp"}
MAX_BYTES = 5 * 1024 * 1024  # 5MB

# ---------- internal helpers ----------
def _doc_to_public_recipe(doc_snap) -> Dict[str, Any]:
    d = doc_snap.to_dict() or {}
    d["id"] = doc_snap.id
    return d

def _validate_positive_limit(limit: int, max_limit: int = 100) -> int:
    return max(1, min(limit, max_limit))

# ---------- services ----------
def list_by_contributor(uid: str, limit: int, page_token: Optional[str]) -> Dict[str, Any]:
    limit = _validate_positive_limit(limit)

    coll = db.collection("recipes")
    q = (coll.where("contributorId", "==", uid)
             .order_by("__name__")
             .limit(limit + 1))

    if page_token:
        token_ref = coll.document(page_token)
        token_snap = token_ref.get()
        if not token_snap.exists:
            raise ValueError("Invalid page_token (doc not found).")
        tok = token_snap.to_dict() or {}
        if tok.get("contributorId") != uid:
            raise PermissionError("page_token does not belong to this contributor.")
        q = q.start_after(token_snap)

    docs = list(q.stream())
    has_more = len(docs) > limit
    page_docs = docs[:limit]

    items = [_doc_to_public_recipe(s) for s in page_docs]
    next_token = page_docs[-1].id if (has_more and page_docs) else None
    return {"items": items, "nextPageToken": next_token}

def get_recipe_by_id(id: str) -> Optional[Dict[str, Any]]:
    snap = db.collection("recipes").document(id).get()
    return _doc_to_public_recipe(snap) if snap.exists else None


def search_recipes(
    *,
    name: Optional[str],
    sort: Optional[str],
    categories: Optional[List[str]],
    areas: Optional[List[str]],
    minutes_bucket: Optional[str],
    limit: int,
    page_token: Optional[str],
    fuzzy_threshold: int = 60,
    enable_fuzzy: bool = True,
) -> Dict[str, Any]:
    limit = _validate_positive_limit(limit)

    coll = db.collection("recipes")
    q = coll

    used_name_search = False
    if name:
        terms = name.strip().lower().split()
        if terms:
            q = q.where("searchKeywords", "array_contains_any", terms[:10])
            used_name_search = True

    if categories:
        cats = [c.strip() for c in categories if c and c.strip()]
        if cats:
            q = q.where("category", "==", cats[0]) if len(cats) == 1 else q.where("category", "in", cats[:10])

    if areas:
        ars = [a.strip() for a in areas if a and a.strip()]
        if ars:
            q = q.where("area", "==", ars[0]) if len(ars) == 1 else q.where("area", "in", ars[:10])

    if minutes_bucket:
        if minutes_bucket == "quick":
            q = q.where("minutes", "<=", 15)
        elif minutes_bucket == "short":
            q = q.where("minutes", "<=", 30)
        elif minutes_bucket == "medium":
            q = q.where("minutes", "<=", 45)
        elif minutes_bucket == "long":
            q = q.where("minutes", ">", 60)

    ordered = False
    if minutes_bucket:
        q = q.order_by("minutes", direction=firestore.Query.ASCENDING)
        ordered = True

    if sort == "popularity":
        q = q.order_by("popularity", direction=firestore.Query.DESCENDING)
        ordered = True
    elif sort == "rating":
        q = q.order_by("ratingAvg", direction=firestore.Query.DESCENDING)
        ordered = True

    if not ordered and not used_name_search:
        q = q.order_by("__name__")

    if page_token:
        token_ref = coll.document(page_token)
        token_snap = token_ref.get()
        if not token_snap.exists:
            raise ValueError("Invalid page_token (doc not found).")
        q = q.start_after(token_snap)

    fetch_size = (limit * 3 + 1) if (name and enable_fuzzy and fuzzy_threshold > 0 and not sort) else (limit + 1)
    docs = list(q.limit(fetch_size).stream())

    items = [_doc_to_public_recipe(s) for s in docs]

    if name and enable_fuzzy and fuzzy_threshold > 0 and not sort:
        items = fuzzy_filter(items, name, fuzzy_threshold, limit)
    else:
        items = items[:limit]

    if name and enable_fuzzy and fuzzy_threshold > 0 and not sort:
        next_token = items[-1]["id"] if len(items) == limit else None
    else:
        next_token = docs[-1].id if len(docs) == fetch_size else None

    return {"items": items, "nextPageToken": next_token}




def save_recipe_for_user(user_id: str, recipe_id: str):
    user_ref = db.collection("users").document(user_id)
    recipe_ref = db.collection("recipes").document(recipe_id)

    # 1. Add recipe_id to user's savedrecipes safely
    user_ref.update({"savedrecipes": firestore.ArrayUnion([recipe_id])})

    # 2. Increment saveCount atomically
    recipe_ref.update({"saveCount": firestore.Increment(1)})

    return {
        "message": "Recipe saved successfully",
        "recipe_id": recipe_id
    }



def unsave_recipe_for_user(user_id: str, recipe_id: str):
    user_ref = db.collection("users").document(user_id)
    recipe_ref = db.collection("recipes").document(recipe_id)

    user_doc = user_ref.get()
    if not user_doc.exists:
        raise ValueError("User not found.")

    saved_recipes = user_doc.to_dict().get("savedrecipes", [])
    if recipe_id not in saved_recipes:
        return {
            "message": "Recipe was not saved.",
            "recipe_id": recipe_id
        }

    # 1. Remove recipe_id from user's savedrecipes
    user_ref.update({"savedrecipes": firestore.ArrayRemove([recipe_id])})

    # 2. Decrement saveCount atomically (but not below 0)
    recipe_snapshot = recipe_ref.get()
    if recipe_snapshot.exists:
        current_count = recipe_snapshot.to_dict().get("saveCount", 0)
        if current_count > 0:
            recipe_ref.update({"saveCount": firestore.Increment(-1)})

    return {
        "message": "Recipe unsaved successfully",
        "recipe_id": recipe_id
    }


def get_saved_recipes_for_user(user_id: str):
    print("Fetching saved recipes for user:", user_id)
    user_ref = db.collection("users").document(user_id)
    doc = user_ref.get()
    if not doc.exists:
        return []

    saved_ids = doc.to_dict().get("savedrecipes", [])
    print("Saved recipe IDs:", saved_ids)
    recipes = []
    for rid in saved_ids:
        rec_doc = db.collection("recipes").document(rid).get()
        if rec_doc.exists:
            recipe_data = rec_doc.to_dict()
            recipe_data["id"] = rec_doc.id
            recipes.append(recipe_data)
    for r in recipes:
        print("Recipe:", r.get("name"))
    return recipes


def list_all_recipes(limit: int = 100, page_token: Optional[str] = None):
    query = (
        db.collection("recipes")
        .order_by("createdAt", direction=firestore.Query.DESCENDING)
        .limit(limit)
    )
    if page_token:
        start_doc = db.collection("recipes").document(page_token).get()
        if not start_doc.exists:
            raise ValueError("Invalid page token")
        query = query.start_after(start_doc)

    docs = query.stream()
    recipes = []
    last_doc_id = None

    for doc in docs:
        data = doc.to_dict()
        data["id"] = doc.id
        recipes.append(data)
        last_doc_id = doc.id

    return {
        "items": recipes,
        "nextPageToken": last_doc_id if len(recipes) == limit else None,
    }

# ---------------------------
# Create Recipe (included image upload)
# ---------------------------

async def upload_recipe_image(file) -> str:
    if file.content_type not in ALLOWED_MIME:
        raise ValueError("Unsupported image type. Use jpg/png/webp.")
    
    ext = (file.filename or "upload").split(".")[-1].lower()
    public_id = f"recipes/{uuid.uuid4()}.{ext}"

    try:
        result = cloudinary.uploader.upload(
            file.file,
            public_id=public_id,
            resource_type="image",
            overwrite=True
        )
        return result["secure_url"]
    except Exception as e:
        raise RuntimeError(f"Failed to upload image to Cloudinary: {e}")


async def create_recipe(uid: str, file, body: RecipeCreate) -> Dict[str, Any]:
    coll = db.collection("recipes")

    # --- normalize ingredients to {"measure","ingredient"} ---
    norm_ingredients = []
    for it in (body.ingredients):
        d = it.dict() if hasattr(it, "dict") else dict(it)
        name = (d.get("ingredient") or d.get("name") or "").strip()
        measure = (d.get("measure") or d.get("amount") or "").strip()
        if name:
            norm_ingredients.append({"measure": measure, "ingredient": name})

    # --- steps & counters ---
    steps = [s.strip() for s in (body.steps or []) if s and s.strip()]
    n_steps = len(steps)
    n_ingredients = len(norm_ingredients)

    thumbnail = await upload_recipe_image(file)

    # --- search keywords (lowercased, deduped) ---
    def _mk_keywords():
        parts = []
        parts += (body.name or "").lower().split()
        if body.category:
            parts += (body.category or "").lower().split()
        if body.area:
            parts += (body.area or "").lower().split()
        parts += [t.lower().strip() for t in (body.tags or []) if t and t.strip()]
        # If you already have make_keywords(), you can merge both sets:
        try:
            parts += list(make_keywords(body.name, body.category or "", body.area or "", body.tags or []))
        except NameError:
            pass
        # de-dupe while preserving order
        seen, out = set(), []
        for p in parts:
            if p and p not in seen:
                seen.add(p); out.append(p)
        return out
    
    data: Dict[str, Any] = {
        "name": (body.name or "").strip(),
        "category": (body.category or "").strip() or None,
        "area": (body.area or "").strip() or None,
        "minutes": body.minutes or 0,  # your DB shows 0 as default
        "ratingAvg": body.ratingAvg if body.ratingAvg is not None else 0.0,
        "tags": [t for t in (body.tags or []) if t and str(t).strip()],  # keep original casing like DB
        "thumbnail": thumbnail,
        "ingredients": norm_ingredients,
        "steps": steps,
        "contributorId": uid,
        "contributorName": get_user_public(uid)["name"],
        "source": getattr(body, "source", None) or "App",
        "youtubeUrl": getattr(body, "youtubeUrl", None) or None,
        "lastInteractionAt": None,
        "searchKeywords": _mk_keywords(),
        "n_ingredients": n_ingredients,
        "n_steps": n_steps,
        "popularity": 0,
        "createdAt": firestore.SERVER_TIMESTAMP,
        "updatedAt": firestore.SERVER_TIMESTAMP,
    }

    # remove keys with value None (to match your existing docs’ sparsity)
    data = {k: v for k, v in data.items() if v is not None}

    ref = coll.document()  # auto-ID
    ref.set(data)
    snap = ref.get()
    if not snap.exists:
        raise RuntimeError("Failed to create recipe.")
    return _doc_to_public_recipe(snap)  # attaches "id" for clients, not stored


# ---------------------------
# Helpers to fetch the step text from database (Firestore)
# ---------------------------

def get_step(recipe_id: str, step_index: int) -> str:
    doc = db.collection("recipes").document(recipe_id).get()
    data = doc.to_dict()
    steps = data.get("steps", [])
    if 0 <= step_index < len(steps):
        return steps[step_index]
    return "No step found."


def get_next_step(recipe_id: str, step_index: int) -> str:
    return get_step(recipe_id, step_index + 1)

def get_previous_step(recipe_id: str, step_index: int) -> str:
    return get_step(recipe_id, step_index - 1)

def get_step_count(recipe_id: str) -> int:
    doc = db.collection("recipes").document(recipe_id).get()
    data = doc.to_dict() or {}
    steps = data.get("steps", [])
    return len(steps)
