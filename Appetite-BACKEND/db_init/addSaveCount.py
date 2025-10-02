import firebase_admin
from firebase_admin import credentials, firestore

# --- Init Firestore ---
if not firebase_admin._apps:    
    cred = credentials.Certificate("D:/MobileFinal/Appetite-BACKEND/app/serviceAccount.json")
    firebase_admin.initialize_app(cred)

db = firestore.client()

def add_save_count_to_recipes():
    print("Starting migration: Adding saveCount to recipes...")

    # Step 1: Build a map recipe_id -> count of saves
    recipe_save_counts = {}

    users_ref = db.collection("users").stream()
    for user_doc in users_ref:
        user_data = user_doc.to_dict() or {}
        saved = user_data.get("savedrecipes", [])
        for rid in saved:
            recipe_save_counts[rid] = recipe_save_counts.get(rid, 0) + 1

    print("Computed save counts for", len(recipe_save_counts), "recipes")

    # Step 2: Go through ALL recipes and set saveCount
    recipes_ref = db.collection("recipes").stream()
    updated = 0
    for rec_doc in recipes_ref:
        rid = rec_doc.id
        count = recipe_save_counts.get(rid, 0)  # default to 0 if never saved
        db.collection("recipes").document(rid).update({"saveCount": count})
        print(f"Updated recipe {rid} with saveCount={count}")
        updated += 1

    print(f"Migration completed ✅ Updated {updated} recipes")

if __name__ == "__main__":
    add_save_count_to_recipes()
