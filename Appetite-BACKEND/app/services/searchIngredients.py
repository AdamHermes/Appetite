from app.db.firestore import db

def search_recipes_by_ingredients(ingredients: list[str], top_k: int = 8):
    """
    Find recipes that contain any of the provided ingredients.
    Uses weighted overlap: generic/common ingredients count less.
    """

    # Low-weight "generic" ingredients
    GENERIC_KEYWORDS = ["salt", "pepper", "oil", "water", "sugar", "onion", "garlic","sauce", "butter", "sugar"]

    results = []

    recipes_ref = db.collection("recipes").order_by("name")
    recipes = list(recipes_ref.stream())  

    query_ingredients = [ing.strip().lower() for ing in ingredients]

    for doc in recipes:
        data = doc.to_dict()
        recipe_ingredients = [ri.lower() for ri in data.get("ingredient_names", [])]

        # Calculate overlap
        overlap = set(recipe_ingredients) & set(query_ingredients)

        if not overlap:
            continue  # skip recipes with no matches

        # Weighted score
        score = 0.0
        for ing in overlap:
            if any(keyword in ing for keyword in GENERIC_KEYWORDS):
                score += 0.5  # low importance
            else:
                score += 2.0  # high importance

        results.append({
            "id": doc.id,
            "name": data.get("name", "Unknown"),
            "matched_ingredients": list(overlap),
            "all_ingredients": recipe_ingredients,
            "score": score
        })

    # Sort by weighted score, then by number of matches as fallback
    results = sorted(
        results,
        key=lambda r: (r["score"], len(r["matched_ingredients"]), r["name"]),
        reverse=True
    )


    return results[:top_k]

