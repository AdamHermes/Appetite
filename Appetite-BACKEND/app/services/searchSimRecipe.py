import clip
import torch
import numpy as np
from io import BytesIO
from PIL import Image
from fastapi import Request
from firebase_admin import firestore
import faiss
from fastapi import FastAPI

# ------------------------
# Setup
# ------------------------

def encode_image(pil_image: Image.Image, request: Request):
    """Encode a PIL image into CLIP embedding"""
    preprocess = request.app.state.preprocess
    model = request.app.state.model
    device = request.app.state.device

    image = preprocess(pil_image).unsqueeze(0).to(device)
    with torch.no_grad():
        features = model.encode_image(image)
    return features.cpu().numpy().flatten()

def get_all_recipe_embeddings(app: FastAPI):
    db = app.state.db
    """Fetch all recipe embeddings from Firestore"""
    docs = db.collection("recipe_embeddings").stream()
    recipes = []
    for doc in docs:
        data = doc.to_dict()
        if "embedding" in data:
            recipes.append({
                "id": doc.id,
                "embedding": np.array(data["embedding"], dtype=np.float32)
            })
    return recipes


def get_recipe_details(recipe_id: str, request: Request):
    """Fetch recipe metadata (name, thumbnail, etc.)"""
    db = request.app.state.db
    doc = db.collection("recipes").document(recipe_id).get()
    if not doc.exists:
        return None
    data = doc.to_dict()
        
    data = doc.to_dict()
    data["id"] = recipe_id  # always include the id
    return data

def build_faiss_index(request: Request):
    global faiss_index, id_mapping
    recipes = get_all_recipe_embeddings(request)
    if not recipes:
        return
    embeddings = np.vstack([r["embedding"] for r in recipes]).astype("float32")
    faiss.normalize_L2(embeddings)
    dim = embeddings.shape[1]
    faiss_index = faiss.IndexFlatIP(dim)
    faiss_index.add(embeddings)
    id_mapping = [r["id"] for r in recipes]
    return faiss_index, id_mapping