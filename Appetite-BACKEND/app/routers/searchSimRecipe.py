import requests
import faiss
import numpy as np
from io import BytesIO
from PIL import Image
from fastapi import APIRouter, UploadFile, File, HTTPException, Query, Request
from app.services.searchSimRecipe import encode_image, get_recipe_details

router = APIRouter(prefix="/api/v1/similarity", tags=["similarity"])

# ------------------------
# API Endpoints
# ------------------------

@router.post("/by-upload")
async def search_by_upload(
    request: Request,   # 👈 just add this
    file: UploadFile = File(...),
    top_k: int = Query(8, ge=1, le=20)
):
    faiss_index = getattr(request.app.state, "faiss_index", None)
    id_mapping = getattr(request.app.state, "id_mapping", None)

    if faiss_index is None:
        raise HTTPException(status_code=404, detail="FAISS index not built")

    try:
        pil_img = Image.open(BytesIO(await file.read())).convert("RGB")
    except Exception:
        raise HTTPException(status_code=400, detail="Invalid image file")

    query_vec = encode_image(pil_img, request).reshape(1, -1).astype("float32")
    faiss.normalize_L2(query_vec)

    sims, idxs = faiss_index.search(query_vec, top_k)

    results = []
    for score, idx in zip(sims[0], idxs[0]):
        recipe_id = id_mapping[idx]
        details = get_recipe_details(recipe_id, request)  # 👈 pass request
        if details:
            results.append({**details, "similarity": float(score)})

    return {"results": results}

@router.get("/by-url")
async def search_by_url(
    request: Request,   # 👈 just add this
    url: str = Query(..., description="Publicly accessible image URL"),
    top_k: int = Query(8, ge=1, le=20)
):
    faiss_index = getattr(request.app.state, "faiss_index", None)
    id_mapping = getattr(request.app.state, "id_mapping", None)


    if faiss_index is None:
        raise HTTPException(status_code=404, detail="FAISS index not built")

    try:
        response = requests.get(url, timeout=10)
        pil_img = Image.open(BytesIO(response.content)).convert("RGB")
    except Exception:
        raise HTTPException(status_code=400, detail="Invalid image URL")

    query_vec = encode_image(pil_img, request).reshape(1, -1).astype("float32")
    faiss.normalize_L2(query_vec)

    sims, idxs = faiss_index.search(query_vec, top_k)

    results = []
    for score, idx in zip(sims[0], idxs[0]):
        recipe_id = id_mapping[idx]
        details = get_recipe_details(recipe_id, request)  # 👈 pass request
        if details:
            results.append({**details, "similarity": float(score)})

    return {"results": results}
