import firebase_admin
from firebase_admin import credentials, firestore
import random

# Initialize Firebase Admin
cred = credentials.Certificate("app/serviceAccount.json")  # Adjust path if needed
firebase_admin.initialize_app(cred)
db = firestore.client()

def random_buckets(min_total=0, max_total=50):
    # Randomly distribute total ratings into 5 buckets
    total = random.randint(min_total, max_total)
    buckets = [0, 0, 0, 0, 0]
    for _ in range(total):
        buckets[random.randint(0, 4)] += 1
    return {str(i+1): buckets[i] for i in range(5)}

def calc_rating(buckets):
    total = sum(buckets.values())
    if total == 0:
        return 0, 0.0
    avg = sum(int(star) * count for star, count in buckets.items()) / total
    return total, round(avg, 1)

def add_rating_fields_to_recipes():
    recipes_ref = db.collection("recipes")
    recipes = recipes_ref.stream()
    count = 0
    for doc in recipes:
        doc_ref = recipes_ref.document(doc.id)
        rating_buckets = random_buckets(0, 50)
        rating_count, rating_avg = calc_rating(rating_buckets)
        doc_ref.update({
            "ratingBuckets": rating_buckets,
            "ratingCount": rating_count,
            "ratingAvg": rating_avg
        })
        count += 1
        print(f"Updated recipe {doc.id} with ratingAvg={rating_avg}, ratingCount={rating_count}, ratingBuckets={rating_buckets}")
    print(f"Done! Updated {count} recipes.")

if __name__ == "__main__":
    add_rating_fields_to_recipes()