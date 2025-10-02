import firebase_admin
from firebase_admin import credentials, firestore
import random

# Initialize Firebase Admin
cred = credentials.Certificate("app/serviceAccount.json")  # Adjust path if needed
firebase_admin.initialize_app(cred)
db = firestore.client()

# Define possible minutes and weights
minutes_options = [30, 40, 50, 60, 90]
minutes_weights = [4, 4, 3, 2, 1]  # Bell curve-ish distribution

def assign_realistic_minutes_to_recipes():
    """Assign realistic minutes to recipes with minutes = 0"""
    recipes_ref = db.collection("recipes")
    recipes = recipes_ref.stream()
    count = 0
    updated_count = 0

    for doc in recipes:
        doc_data = doc.to_dict()
        doc_ref = recipes_ref.document(doc.id)

        # Only update if minutes == 0
        if doc_data.get("minutes", 0) != 0:
            chosen_minutes = random.choices(minutes_options, weights=minutes_weights, k=1)[0]

            doc_ref.update({
                "minutes": chosen_minutes
            })

            updated_count += 1
            recipe_name = doc_data.get('name', 'Unknown')[:30]
            print(f"Updated recipe '{recipe_name}' (ID: {doc.id}) - Minutes set to {chosen_minutes}")
        else:
            count += 1

    print(f"Done! Processed {count + updated_count} recipes, updated {updated_count} with minutes.")

if __name__ == "__main__":
    print("Populating missing 'minutes' values...")
    assign_realistic_minutes_to_recipes()
