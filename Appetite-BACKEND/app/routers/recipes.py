import json
from typing import Optional, Dict, Any, List
from fastapi import (
    APIRouter, Depends, HTTPException, Query, status,
    UploadFile, File, Body, Form
)
from pydantic import BaseModel, conint 
from firebase_admin import firestore

from app.auth import get_current_user, FirebaseUser
from app.schemas.recipes import RecipeCreate
from app.services.recipes import (
    list_by_contributor,
    get_recipe_by_id as svc_get_recipe_by_id,
    create_recipe as svc_create_recipe,
    search_recipes as svc_search_recipes,
    save_recipe_for_user as svc_save_recipes,
    unsave_recipe_for_user as svc_unsave_recipe,
    get_saved_recipes_for_user as svc_get_saved_recipes,
    list_all_recipes as svc_list_all_recipes,
    upload_recipe_image as svc_upload_recipe_image
)

router = APIRouter(prefix="/api/v1/recipes", tags=["recipes"])

@router.get("/by-contributor/{uid}")
def recipes_by_contributor_endpoint(
    uid: str,
    limit: int = Query(20, ge=1, le=100, description="Max items to return (1..100)"),
    page_token: Optional[str] = Query(None, description="Document ID cursor for next page"),
) -> Dict[str, Any]:
    try:
        return list_by_contributor(uid=uid, limit=limit, page_token=page_token)
    except (ValueError, PermissionError) as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))

@router.get("/me")
def my_recipes(
    limit: int = Query(20, ge=1, le=100),
    page_token: Optional[str] = None,
    user: FirebaseUser = Depends(get_current_user),
):
    return recipes_by_contributor_endpoint(user.uid, limit=limit, page_token=page_token)

@router.get("/id/{id}")
def get_recipe(id: str) -> Dict[str, Any]:
    rec = svc_get_recipe_by_id(id)
    if not rec:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Recipe not found.")
    return rec

import json

import json
from google.cloud.firestore_v1 import DocumentSnapshot

def serialize_firestore(data):
    def convert_value(v):
        # Convert Firestore timestamp to ISO string
        if hasattr(v, "isoformat"):
            return v.isoformat()
        return v
    return {k: convert_value(v) for k, v in data.items()}

@router.delete("/id/{id}", status_code=status.HTTP_200_OK)
def delete_recipe(
    id: str,
    user: FirebaseUser = Depends(get_current_user)
):
    recipe_ref = firestore.client().collection("recipes").document(id)
    doc = recipe_ref.get()
    if not doc.exists:
        raise HTTPException(status_code=404, detail="Recipe not found.")

    data = doc.to_dict()
    if data.get("contributorId") != user.uid:
        raise HTTPException(status_code=403, detail="You do not have permission to delete this recipe.")

    # Serialize Firestore timestamps for printing
    def convert_value(v):
        if hasattr(v, "isoformat"):
            return v.isoformat()
        return v
    printable = {k: convert_value(v) for k, v in data.items()}
    printable["id"] = id
    print("Deleted recipe:", json.dumps(printable, ensure_ascii=False, indent=2))

    recipe_ref.delete()
    return {"message": "Recipe deleted successfully.", "id": id}

@router.get("/search")
def search_recipes(
    name: Optional[str] = Query(None, description="Recipe name to search (fuzzy matching)"),
    sort: Optional[str] = Query(None, regex="^(popularity|rating)$", description="Sort results by popularity or rating"),
    categories: Optional[List[str]] = Query(None, description="Category tags to match (repeat param for multiple)"),
    areas: Optional[List[str]] = Query(None, description="Area/cuisine tags to match (repeat param for multiple)"),
    minutes_bucket: Optional[str] = Query(None, regex="^(quick|short|medium|long)$",
                                          description="quick(<=15), short(<=30), medium(<=45), long(>60)"),
    limit: int = Query(20, ge=1, le=100, description="Max items to return"),
    page_token: Optional[str] = Query(None, description="Document ID cursor for next page"),
    fuzzy_threshold: int = Query(60, ge=0, le=100, description="Fuzzy match threshold (0-100)"),
    enable_fuzzy: bool = Query(True, description="Enable fuzzy search for name parameter"),
) -> Dict[str, Any]:
    try:
        return svc_search_recipes(
            name=name,
            sort=sort,
            categories=categories,
            areas=areas,
            minutes_bucket=minutes_bucket,
            limit=limit,
            page_token=page_token,
            fuzzy_threshold=fuzzy_threshold,
            enable_fuzzy=enable_fuzzy,
        )
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))
    

class SaveRecipeRequest(BaseModel):
    recipe_id: str

@router.post("/save", status_code=status.HTTP_200_OK)
def save_recipe(
    body: SaveRecipeRequest,
    user: FirebaseUser = Depends(get_current_user)
):
    """
    Save a recipe for the current user.
    Returns the full saved recipe object.
    """
    try:
        # Use service layer to save the recipe
        recipe = svc_save_recipes(user.uid, body.recipe_id)
        if not recipe:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Recipe not found.")
        return {"savedrecipe": recipe}
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=str(e))

@router.post("/unsave", status_code=status.HTTP_200_OK)
def unsave_recipe(
    body: SaveRecipeRequest,
    user: FirebaseUser = Depends(get_current_user)
):
    try:
        removed_recipe = svc_unsave_recipe(user.uid, body.recipe_id)
        if not removed_recipe:
            raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Recipe not found or not saved.")
        return {"removedRecipe": removed_recipe}
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=str(e))

@router.get("/saved", status_code=status.HTTP_200_OK)
def get_saved_recipes(
    user: FirebaseUser = Depends(get_current_user),
):
    """
    Get all saved recipes for the current user.
    Returns a list of RecipeFire objects.
    """
    try:
        saved_recipes = svc_get_saved_recipes(user.uid)  # no limit, no page_token
        return {"savedrecipes": saved_recipes}
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=str(e)
        )


@router.get("/all", status_code=status.HTTP_200_OK)
def get_all_recipes(
    limit: int = Query(100, ge=1, le=100, description="Max items to return"),
    page_token: Optional[str] = Query(None, description="Document ID cursor for next page"),
) -> Dict[str, Any]:
    """
    Get all recipes in the database.
    Supports pagination via `limit` and `page_token`.
    """
    try:
        return svc_list_all_recipes(limit=limit, page_token=page_token)
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=str(e))
class RateIn(BaseModel):
    value: conint(ge=1, le=5)  # Only allow 1-5



# ---------------------------
# Recipe Rating Endpoints
# ---------------------------

@router.post("/{recipe_id}/rate")
def rate_recipe(
    recipe_id: str,
    rating: RateIn,
    user: FirebaseUser = Depends(get_current_user)
):
    """
    Rate a recipe (1-5 stars). Each user can rate a recipe once.
    Updates ratingBuckets, ratingCount, and ratingAvg.
    """
    recipe_ref = firestore.client().collection("recipes").document(recipe_id)
    user_rating_ref = recipe_ref.collection("ratings").document(user.uid)

    # Check if user already rated
    prev = user_rating_ref.get()
    batch = firestore.client().batch()

    # Remove previous rating from buckets if exists
    if prev.exists:
        prev_value = prev.to_dict().get("value")
        if prev_value == rating.value:
            return {"message": "Already rated with this value."}
        batch.update(recipe_ref, {f"ratingBuckets.{str(prev_value)}": firestore.Increment(-1)})
    else:
        batch.update(recipe_ref, {"ratingCount": firestore.Increment(1)})

    # Add new rating to buckets
    batch.update(recipe_ref, {f"ratingBuckets.{str(rating.value)}": firestore.Increment(1)})
    batch.set(user_rating_ref, {"value": rating.value})

    batch.commit()

    # Recalculate average
    recipe_doc = recipe_ref.get()
    buckets = recipe_doc.to_dict().get("ratingBuckets", {})
    count = recipe_doc.to_dict().get("ratingCount", 0)
    avg = (
        sum(int(star) * buckets.get(star, 0) for star in ["1", "2", "3", "4", "5"]) / count
        if count > 0 else 0.0
    )
    recipe_ref.update({"ratingAvg": round(avg, 1)})

    return {"message": "Rating updated", "ratingAvg": round(avg, 1), "ratingCount": count, "ratingBuckets": buckets}

@router.get("/{recipe_id}/my-rating", status_code=status.HTTP_200_OK)
def get_my_rating(
    recipe_id: str,
    user: FirebaseUser = Depends(get_current_user)
):
    """
    Get the current user's star rating for a recipe.
    Returns {"value": int} if rated, or {"value": None} if not rated.
    """
    recipe_ref = firestore.client().collection("recipes").document(recipe_id)
    user_rating_ref = recipe_ref.collection("ratings").document(user.uid)
    doc = user_rating_ref.get()
    if doc.exists:
        return {"value": doc.to_dict().get("value")}
    else:
        return {"value": None}
    

# ---------------------------
# Create Recipe (included image upload)
# ---------------------------

@router.post("/", status_code=status.HTTP_201_CREATED)
async def create_recipe(
    data: str = Form(...),                 # JSON string for RecipeCreate
    file: UploadFile = File(...),          # forces binary image
    user: FirebaseUser = Depends(get_current_user),
):
    try:
        body = RecipeCreate(**json.loads(data))
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Invalid recipe data: {e}")

    try:
        recipe = await svc_create_recipe(user.uid, file, body)   # <-- await
        return recipe
    except RuntimeError as e:
        raise HTTPException(status_code=500, detail=str(e))
