import torch
import clip
from PIL import Image
import requests
import numpy as np
import firebase_admin
from firebase_admin import credentials, firestore

# --- Init Firebase ---
cred = credentials.Certificate("D:/MobileFinal/Appetite-BACKEND/app/serviceAccount.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

# --- Load CLIP ---
device = "cuda" if torch.cuda.is_available() else "cpu"
model, preprocess = clip.load("ViT-B/32", device=device)

def encode_image_from_url(url):
    try:
        response = requests.get(url, stream=True, timeout=10)
        image = preprocess(Image.open(response.raw).convert("RGB")).unsqueeze(0).to(device)
        with torch.no_grad():
            features = model.encode_image(image)
        return features.cpu().numpy().flatten()
    except Exception as e:
        print(f"❌ Failed to process {url}: {e}")
        return None

def embed_all_recipes():
    recipes_ref = db.collection("recipes").stream()
    for recipe in recipes_ref:
        data = recipe.to_dict()
        recipe_id = recipe.id
        thumbnail_url = data.get("thumbnail")

        if not thumbnail_url:
            print(f"⚠️ No thumbnail for {recipe_id}")
            continue

        print(f"➡️ Processing {recipe_id} ...")

        embedding = encode_image_from_url(thumbnail_url)
        if embedding is not None:
            # Save embedding to Firestore
            db.collection("recipe_embeddings").document(recipe_id).set({
                "embedding": embedding.tolist()
            })
            print(f"✅ Saved embedding for {recipe_id}")
        else:
            print(f"❌ Skipped {recipe_id}")

if __name__ == "__main__":
    embed_all_recipes()
