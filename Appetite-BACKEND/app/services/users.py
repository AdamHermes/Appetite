# services/user_service.py
from typing import Optional,List
from pydantic import BaseModel
from app.db.firestore import db
from app.utils.search import make_keywords, fuzzy_search_user

class Profile(BaseModel):
    profileUrl: Optional[str] = None
    id: str
    name: str
    email: Optional[str] = None
    bio: Optional[str] = None
    recipesCount: int = 0
    followersCount: int = 0
    followingCount: int = 0

class UserSearchResult(BaseModel):
    id: str
    name: str
    profileUrl: Optional[str] = None

class ProfileCreate(BaseModel):
    name: str
    email: str

class ProfileUpdate(BaseModel):
    name: Optional[str] = None
    bio: Optional[str] = None
    profileUrl: Optional[str] = None


def svc_get_all_users() -> List[Profile]:
    users_ref = db.collection("users").stream()
    users = []

    for doc in users_ref:
        user_data = doc.to_dict() or {}
        uid = doc.id

        # Count recipes created by this user
        recipes = db.collection("recipes").where("contributorId", "==", uid).stream()
        recipe_count = sum(1 for _ in recipes)

        users.append(
            Profile(
                id=uid,
                name=user_data.get("name", "Unknown"),
                email=user_data.get("email"),
                bio=user_data.get("bio"),
                profileUrl=user_data.get("profileUrl"),
                recipesCount=recipe_count,
            )
        )

    return users


def svc_get_user_by_id(user_id: str) -> Optional[Profile]:
    doc = db.collection("users").document(user_id).get()
    if not doc.exists:
        return None
    data = doc.to_dict() or {}

    followers_count = data.get("followersCount", 0)
    following_count = data.get("followingCount", 0)

    return Profile(
        id=user_id,
        name=data.get("name", "Unknown"),
        email=data.get("email"),
        bio=data.get("bio"),
        profileUrl=data.get("profileUrl"),
        recipesCount=data.get("recipesCount", 0),
        followersCount=followers_count,
        followingCount=following_count,
    )


def svc_search_user(name: Optional[str], limit: int = 20) -> List[UserSearchResult]:
    user_docs = list(db.collection("users").stream())
    items: List[dict] = []

    for doc in user_docs:
        data = doc.to_dict() or {}
        uid = doc.id
        user_name = data.get("name", "") or ""

        keywords = user_name.strip().lower().split()

        items.append(
            {
                "id": uid,
                "name": user_name or "Unknown",
                "profileUrl": data.get("profileUrl"),
                "searchKeywords": keywords,
            }
        )

    filtered = fuzzy_search_user(items, query=name or "", limit=limit)

    results: List[UserSearchResult] = []
    for it in filtered:
        results.append(
            UserSearchResult(
                id=it["id"],
                name=it.get("name", "Unknown"),
                profileUrl=it.get("profileUrl"),
            )
        )

    return results
