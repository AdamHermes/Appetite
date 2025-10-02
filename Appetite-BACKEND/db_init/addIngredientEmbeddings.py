import torch
import clip
import firebase_admin
from firebase_admin import credentials, firestore

# --- Init Firebase ---
cred = credentials.Certificate("D:/MobileFinal/Appetite-BACKEND/app/serviceAccount.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

# --- Load CLIP ---
device = "cuda" if torch.cuda.is_available() else "cpu"
model, preprocess = clip.load("ViT-B/32", device=device)

def encode_text(text: str):
    """Encode a single ingredient name into CLIP embedding"""
    tokens = clip.tokenize([text]).to(device)
    with torch.no_grad():
        features = model.encode_text(tokens)
    return features.cpu().numpy().flatten()

def embed_all_ingredients():
    vocab = set()

    # Step 1: Collect all ingredient names from recipes
    recipes_ref = db.collection("recipes").stream()
    for recipe in recipes_ref:
        data = recipe.to_dict()
        ingredients = data.get("ingredient_names", [])
        vocab.update(ing.strip().lower() for ing in ingredients)

    print(f"📦 Found {len(vocab)} unique ingredients")

    # Step 2: Encode and save
    for ing in vocab:
        print(f"➡️ Processing {ing} ...")
        embedding = encode_text(ing)
        db.collection("ingredient_embeddings").document(ing).set({
            "embedding": embedding.tolist()
        })
        print(f"✅ Saved embedding for {ing}")

if __name__ == "__main__":
    embed_all_ingredients()
