from fastapi import APIRouter, HTTPException, Depends, UploadFile, File, status
from typing import Optional,List
from firebase_admin import firestore
from app.auth import get_current_user, FirebaseUser
import cloudinary
import cloudinary.uploader
from app.services.users import svc_get_all_users, Profile, ProfileCreate,ProfileUpdate, UserSearchResult
from app.services.users import svc_search_user

router = APIRouter(prefix="/api/v1/user", tags=["User"])

from app.db.firestore import db
# Response model



@router.get("/info/{uid}", response_model=Profile)
def get_user_info(uid: str):
    """Fetch user info by UID"""
    user_ref = db.collection("users").document(uid)
    doc = user_ref.get()

    if not doc.exists:
        raise HTTPException(status_code=404, detail="User not found")

    user_data = doc.to_dict() or {}

    # Count recipes created by this user
    recipes = (
        db.collection("recipes")
        .where("contributorId", "==", uid)
        .stream()
    )
    recipe_count = sum(1 for _ in recipes)

    return Profile(
        id=uid,
        name=user_data.get("name", "Unknown"),
        email=user_data.get("email"),
        bio=user_data.get("bio"),
        profileUrl=user_data.get("profileUrl"),
        recipesCount=recipe_count,
        followersCount= user_data.get("followersCount", 0),
        followingCount = user_data.get("followingCount", 0)
    )


@router.get("/me", response_model=Profile)
def get_my_user_info(user: FirebaseUser = Depends(get_current_user)):
    """Fetch info for the currently authenticated user"""
    return get_user_info(uid=user.uid)




@router.post("/me", response_model=Profile)
def create_my_profile(
    profile: ProfileCreate,
    user: FirebaseUser = Depends(get_current_user)
):
    """
    Create user profile after signup.
    """
    user_ref = db.collection("users").document(user.uid)
    user_data = {
        "uid": user.uid,                
        "name": profile.name,
        "email": profile.email,
        "profileUrl": "https://res.cloudinary.com/dbcp47rg8/image/upload/v1757539408/default_ava_oc0ig3.png",
        "bio": "Hello! I love cooking and sharing recipes.",
        "savedRecipes": []
    }
    user_ref.set(user_data, merge=True)
    return get_user_info(uid=user.uid)



@router.patch("/me", response_model=Profile)
def update_my_profile(
    update: ProfileUpdate,
    user: FirebaseUser = Depends(get_current_user)
):
    update_data = {k: v for k, v in update.dict().items() if v is not None}
    if not update_data:
        raise HTTPException(status_code=400, detail="No fields to update")
    user_ref = db.collection("users").document(user.uid)
    user_ref.set(update_data, merge=True)

    # If name is being updated, also update contributorName in recipes
    if "name" in update_data:
        new_name = update_data["name"]
        recipes_ref = db.collection("recipes").where("contributorId", "==", user.uid)
        for recipe_doc in recipes_ref.stream():
            recipe_doc.reference.update({"contributorName": new_name})

    return get_user_info(uid=user.uid)

@router.post("/me/photo", response_model=Profile)
async def upload_my_profile_photo(
    file: UploadFile = File(...),
    user: FirebaseUser = Depends(get_current_user)
):
    """
    Upload a profile photo to Cloudinary and update profileUrl in Firestore.
    """

    # Read file content
    contents = await file.read()

    # Upload to Cloudinary
    result = cloudinary.uploader.upload(
        contents,
        public_id=f"profile_photos/user_{user.uid}_profile",
        overwrite=True,
        resource_type="image"
    )
    image_url = result.get("secure_url")

    if not image_url:
        raise HTTPException(status_code=500, detail="Image upload failed")

    # Update Firestore user profile
    user_ref = db.collection("users").document(user.uid)
    user_ref.set({"profileUrl": image_url}, merge=True)

    return get_user_info(uid=user.uid)

@router.get("/all", response_model=List[Profile])
def get_all_users():
    return svc_get_all_users()

@router.get("/search", response_model=List[UserSearchResult])
def search_user(name: Optional[str], limit: int = 20):
    try:
        if name is None:
            return []
        return svc_search_user(name=name, limit=limit)
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))


