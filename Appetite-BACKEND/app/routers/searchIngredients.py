from fastapi import APIRouter, UploadFile, File, Query, Request, HTTPException
from PIL import Image
import io

from app.services.searchIngredients import search_recipes_by_ingredients
from app.services.searchSimRecipe import get_recipe_details

router = APIRouter(prefix="/api/v1/ingredients", tags=["ingredients"])

@router.post("/by-upload")
async def search_ingredients_by_upload(
    request: Request,
    file: UploadFile = File(...),
    top_k: int = Query(5, ge=1, le=20),
):
    """
    Upload an image -> Gemini extracts ingredient list -> 
    search recipes in Firestore that contain those ingredients ->
    return recipe details.
    """
    try:
        # 1. Read uploaded image
        image_bytes = await file.read()
        pil_image = Image.open(io.BytesIO(image_bytes)).convert("RGB")

        # 2. Extract ingredients using Gemini
        from app.services.gemini_api import extract_ingredients_from_image
        ingredients = await extract_ingredients_from_image(image_bytes)

        if not ingredients:
            raise HTTPException(status_code=400, detail="No ingredients detected in image")

        # 3. Find candidate recipes from Firestore
        raw_matches = search_recipes_by_ingredients(ingredients, top_k=top_k)

        # 4. Enrich with recipe details
        enriched_results = []
        for match in raw_matches:
            details = get_recipe_details(match["id"], request)
            if details:
                enriched_results.append({
                    **details,  # includes id, title/name, thumbnail, etc.
                    "matched_ingredients": match["matched_ingredients"],
                    "all_ingredients": match["all_ingredients"]
                })

        return {
            "detected_ingredients": ingredients,
            "results": enriched_results
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Ingredient search failed: {str(e)}")

