import firebase_admin
from firebase_admin import credentials, firestore
# Initialize Firestore client (make sure GOOGLE_APPLICATION_CREDENTIALS is set)
cred = credentials.Certificate("D:/MobileFinal/Appetite-BACKEND/app/serviceAccount.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

def migrate_ingredient_names():
    recipes_ref = db.collection("recipes")
    recipes = recipes_ref.stream()

    for doc in recipes:
        data = doc.to_dict()

        # Extract ingredient names
        ingredients = data.get("ingredients", [])
        ingredient_names = []

        for ing in ingredients:
            if isinstance(ing, dict) and "ingredient" in ing:
                name = ing["ingredient"].strip().lower()
                ingredient_names.append(name)

        # Update Firestore document
        db.collection("recipes").document(doc.id).update({
            "ingredient_names": ingredient_names
        })

        print(f"✅ Updated {doc.id} with ingredient_names: {ingredient_names}")

if __name__ == "__main__":
    migrate_ingredient_names()
