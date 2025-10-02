import random
import firebase_admin
from firebase_admin import credentials, firestore
from datetime import datetime

# Initialize Firebase Admin
cred = credentials.Certificate("app/serviceAccount.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

USER_IDS = [
    "DOWx7tYj1mWpPnHP3gIITMD7Cnl2",
    "GQLfXrDMBqYI5uaEYkw0LSzTVGZ2",
    "TZtKbXI1YsPvchXYZLQ02srRQkH3",
    "fSELsXbX7rNX65jTQhjSEwKO03g2",
    "lHEa94N7iCNeVd3T1pteoQ2Fhdh2"
]

USER_NAMES = [
    "Kim Chau",
    "Con Meo",
    "Con Cao",
    "Con Cho",
    "Con Ca"
]

COMMENTS = [
    "Great recipe!",
    "I loved it!",
    "Easy to follow and delicious.",
    "Will make again.",
    "My family enjoyed this a lot.",
    "Thanks for sharing!",
    "Turned out perfect.",
    "Yummy!",
    "Simple and tasty.",
    "Highly recommend."
]

def populate_comments_for_all_recipes():
    recipes_ref = db.collection("recipes")
    recipes = recipes_ref.stream()
    total_comments = 0
    for recipe_doc in recipes:
        recipe_id = recipe_doc.id
        comments_ref = recipes_ref.document(recipe_id).collection("comments")
        num_comments = random.randint(2, 5)
        for i in range(num_comments):
            idx = random.randint(0, len(USER_IDS) - 1)
            user_id = USER_IDS[idx]
            user_name = USER_NAMES[idx]
            comment_text = random.choice(COMMENTS)
            comment_data = {
                "userId": user_id,
                "userName": user_name,
                "text": comment_text,
                "createdAt": datetime.now().timestamp()
            }
            comments_ref.add(comment_data)
            total_comments += 1
    print(f"Added {total_comments} comments to all recipes.")

def delete_all_comments_in_all_recipes():
    recipes_ref = db.collection("recipes")
    recipes = recipes_ref.stream()
    total_comments = 0
    for recipe_doc in recipes:
        recipe_id = recipe_doc.id
        comments_ref = recipes_ref.document(recipe_id).collection("comments")
        comments = comments_ref.stream()
        for comment_doc in comments:
            comment_doc.reference.delete()
            total_comments += 1
    print(f"Deleted {total_comments} comments from all recipes.")

if __name__ == "__main__":
    populate_comments_for_all_recipes()
    #delete_all_comments_in_all_recipes()