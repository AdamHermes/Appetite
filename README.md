## Video Demo



## Run Server

`uvicorn app.main:app --reload --port 8000`

## Todo

indefinitely suspended!

**`GET /api/v1/recipes/feed`**
  Personalized “For You” feed

**`POST /api/v1/recipes/events`**
  log user events (impression, click, like, save, share) for recommend system

**`POST /api/v1/recipes/feedback`**
  explicit feedback (like/dislike/hide) if you want a separate, simpler body (post it include the recipe id)

## API Endpoints (Current)

### Auth

* **`GET /api/v1/auth/me`**
  Returns the authenticated user’s profile (uid, email, provider).

  > Requires `Authorization: Bearer <Firebase ID Token>`

### Recipes

* **`POST /api/v1/recipes`**
  Create the recipe

* **`GET /api/v1/recipes/by-contributor/{uid}`**
  Returns all recipes contributed by the specified user.

  * Supports pagination with `?limit=N& page_token=<docId>`
  * Response:

    ```json
    {
      "items": [ { "id": "...", "name": "...", ... } ],
      "nextPageToken": "docId" | null
    }
    ```

* **`GET /api/v1/recipes/me`**
  Convenience endpoint — returns recipes of the currently authenticated user.

  > Requires `Authorization: Bearer <Firebase ID Token>`

* **`GET /api/v1/recipes/id/{id}`**
  Get recipe details by id

* **`GET /api/v1/recipes/search`**
  Search and filter recipes with optional fuzzy ranking.
    - Coarse filtering is performed in Firestore (by `searchKeywords`, `category`, `area`, and minutes range).
    - If `minutes_bucket` is used, query orders by `minutes` first to satisfy Firestore’s inequality rule. Secondary order by `sort` if provided.
    - When `sort` is set, results are returned strictly in Firestore order (no fuzzy, normal `limit+1` pagination).
    - When `sort` is not set and `name` is present, endpoint overfetches (`~3x limit`) and applies fuzzy ranking client-side, then trims to `limit`.
    - Pagination token:
      - With fuzzy (no `sort`): token is taken from the last returned item’s `id` when `limit` items are returned.
      - Otherwise: token comes from the raw Firestore batch (standard `limit+1` behavior).

### Hand-free AI agent

* **`GET /api/v1/voice-agent/{recipe_id}`**
  Input: Recipe ID, current step index, Audio file
  Return: Audio file, flag (the next intent of user)

### Debug

* **`GET /api/v1/debug/firebase`**
  Returns Firebase project and app initialization info.

⚠️ **Notes**

* All `/me` endpoints require a valid Firebase ID token in the `Authorization` header.
* Recipe queries return normalized Firestore documents with an added `id` field (document ID).


# Database Initialization
### Comments

* **`POST /api/v1/recipes/{recipe_id}/comments`**  
  Add a comment to a recipe.  
  **Body:**  
  ```json
  {
    "text": "Your comment text"
  }
  ```
  > Requires `Authorization: Bearer <Firebase ID Token>`
  Recipe_id is the external_id

* **`GET /api/v1/recipes/{recipe_id}/comments`**  
  Recipe_id is the external_id
  List comments for a recipe.  
  **Query:**  
  - `limit` (optional, default: 20)  
  **Response:**  
  ```json
  {
    "items": [
      {
        "userId": "...",
        "userName": "...",
        "text": "...",
        "createdAt": 1234567890.0
      }
    ]
  }
  ```
Still update ...
