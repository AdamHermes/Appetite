import firebase_admin
from firebase_admin import credentials, firestore
import random
from datetime import datetime, timedelta

# Initialize Firebase Admin
cred = credentials.Certificate("app/serviceAccount.json")  # Adjust path if needed
firebase_admin.initialize_app(cred)
db = firestore.client()

def random_timestamp_pair():
    """Generate a random createdAt and updatedAt timestamp pair"""
    # Define the time range for random timestamps
    start_date = datetime(2023, 1, 1)
    end_date = datetime(2025, 8, 31)
    
    # Calculate total seconds in the range
    time_range = end_date - start_date
    total_seconds = int(time_range.total_seconds())
    
    # Generate random created timestamp
    random_seconds = random.randint(0, total_seconds)
    created_at = start_date + timedelta(seconds=random_seconds)
    
    # Updated timestamp logic:
    # 70% chance it's the same as created (never edited)
    # 30% chance it's edited later
    if random.random() < 0.7:
        updated_at = created_at
    else:
        # Edited between 1-90 days after creation
        remaining_seconds = int((end_date - created_at).total_seconds())
        if remaining_seconds > 0:
            max_edit_days = min(90, remaining_seconds // 86400)  # Don't exceed end_date
            edit_days = random.randint(1, max(1, max_edit_days))
            updated_at = created_at + timedelta(days=edit_days)
        else:
            updated_at = created_at
    
    return created_at, updated_at

def realistic_timestamp_clusters():
    """Generate timestamps from realistic clusters (simulating batch uploads)"""
    # Create time clusters for more realistic data
    cluster_dates = [
        datetime(2023, 3, 15),   # Early batch
        datetime(2023, 6, 20),   # Mid-year batch  
        datetime(2023, 9, 10),   # Fall batch
        datetime(2024, 1, 5),    # New year batch
        datetime(2024, 4, 12),   # Spring batch
        datetime(2024, 8, 25),   # Recent batch
        datetime(2024, 11, 30),  # Latest batch
        datetime(2025, 2, 14),   # Valentine's batch
        datetime(2025, 5, 18),   # Late spring batch
        datetime(2025, 8, 1)     # Current batch
    ]
    
    # Pick a random cluster date
    base_date = random.choice(cluster_dates)
    
    # Add some random variance (±15 days)
    variance_days = random.randint(-15, 15)
    created_at = base_date + timedelta(days=variance_days)
    
    # 70% chance updated timestamp is same as created (no edits)
    # 30% chance it was edited later
    if random.random() < 0.7:
        updated_at = created_at
    else:
        # Edited between 1-90 days after creation
        edit_days = random.randint(1, 90)
        updated_at = created_at + timedelta(days=edit_days)
        
        # Don't let it go beyond current time
        if updated_at > datetime.now():
            updated_at = created_at
    
    return created_at, updated_at

def add_timestamp_fields_to_recipes(use_clusters=True):
    """Add createdAt and updatedAt fields to all recipes that don't have them"""
    recipes_ref = db.collection("recipes")
    recipes = recipes_ref.stream()
    count = 0
    updated_count = 0
    
    for doc in recipes:
        doc_data = doc.to_dict()
        doc_ref = recipes_ref.document(doc.id)
        
        # Only update if timestamps don't exist
        needs_created = 'createdAt' not in doc_data
        needs_updated = 'updatedAt' not in doc_data
        
        if needs_created or needs_updated:
            # Generate timestamps
            if use_clusters:
                created_at, updated_at = realistic_timestamp_clusters()
            else:
                created_at, updated_at = random_timestamp_pair()
            
            # Prepare update data
            update_data = {}
            if needs_created:
                update_data['createdAt'] = created_at
            if needs_updated:
                update_data['updatedAt'] = updated_at
            
            # Update the document
            doc_ref.update(update_data)
            updated_count += 1
            
            recipe_name = doc_data.get('name', 'Unknown')[:30]
            print(f"Updated recipe '{recipe_name}' (ID: {doc.id}) - Created: {created_at.date()}, Updated: {updated_at.date()}")
        else:
            recipe_name = doc_data.get('name', 'Unknown')[:30]
            print(f"Skipped recipe '{recipe_name}' (ID: {doc.id}) - Already has timestamps")
        
        count += 1
    
    print(f"Done! Processed {count} recipes, updated {updated_count} with timestamps.")

def add_timestamp_fields_force_update():
    """Force update ALL recipes with new timestamps (overwrites existing ones)"""
    recipes_ref = db.collection("recipes")
    recipes = recipes_ref.stream()
    count = 0
    
    print("WARNING: This will overwrite existing timestamps!")
    confirm = input("Are you sure? Type 'yes' to continue: ").strip().lower()
    if confirm != 'yes':
        print("Cancelled.")
        return
    
    for doc in recipes:
        doc_data = doc.to_dict()
        doc_ref = recipes_ref.document(doc.id)
        
        # Generate new timestamps
        created_at, updated_at = realistic_timestamp_clusters()
        
        # Update the document
        doc_ref.update({
            'createdAt': created_at,
            'updatedAt': updated_at
        })
        
        count += 1
        recipe_name = doc_data.get('name', 'Unknown')[:30]
        print(f"Force updated recipe '{recipe_name}' (ID: {doc.id}) - Created: {created_at.date()}, Updated: {updated_at.date()}")
    
    print(f"Done! Force updated {count} recipes with new timestamps.")

def preview_timestamp_generation(num_samples=5):
    """Preview what timestamps would be generated (for testing)"""
    print("=== PREVIEW MODE - No data will be updated ===")
    print(f"Generating {num_samples} sample timestamp pairs:\n")
    
    print("Random timestamps:")
    for i in range(num_samples):
        created, updated = random_timestamp_pair()
        same = "✓" if created == updated else "✗"
        print(f"  {i+1}. Created: {created} | Updated: {updated} | Same: {same}")
    
    print("\nCluster-based timestamps:")
    for i in range(num_samples):
        created, updated = realistic_timestamp_clusters()
        same = "✓" if created == updated else "✗"
        print(f"  {i+1}. Created: {created} | Updated: {updated} | Same: {same}")

if __name__ == "__main__":
    print("Firebase Timestamp Population Script")
    print("====================================")
    
    while True:
        print("\nChoose an option:")
        print("1. Add timestamps to recipes (skip existing)")
        print("2. Add timestamps using clusters (skip existing)")
        print("3. Force update ALL recipes (overwrites existing)")
        print("4. Preview timestamp generation")
        print("5. Exit")
        
        choice = input("Enter choice (1-5): ").strip()
        
        if choice == "1":
            add_timestamp_fields_to_recipes(use_clusters=False)
            break
        elif choice == "2":
            add_timestamp_fields_to_recipes(use_clusters=True)
            break
        elif choice == "3":
            add_timestamp_fields_force_update()
            break
        elif choice == "4":
            preview_timestamp_generation()
        elif choice == "5":
            print("Goodbye!")
            break
        else:
            print("Invalid choice. Please try again.")