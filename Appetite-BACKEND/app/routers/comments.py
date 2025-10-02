from fastapi import APIRouter, Depends, HTTPException
from firebase_admin import firestore
from app.auth import get_current_user, FirebaseUser
from datetime import datetime
from app.schemas.comments import CommentIn

router = APIRouter(prefix="/api/v1/recipes", tags=["recipes"])
db = firestore.client()

@router.post("/{recipe_id}/comments")
def add_comment(
    recipe_id: str,
    comment: CommentIn,
    user: FirebaseUser = Depends(get_current_user)
):
    # Get Firestore user profile
    user_doc = db.collection("users").document(user.uid).get()
    user_data = user_doc.to_dict() if user_doc.exists else {}

    comment_data = {
        "userId": user.uid,
        "userName": user.name or user_data.get("name", "Anonymous"),
        "text": comment.text,
        "createdAt": datetime.now().timestamp(),
        "profilePhotoUrl": user_data.get("profileUrl"),
    }

    db.collection("recipes").document(recipe_id).collection("comments").add(comment_data)
    return {"message": "Comment added"}


@router.get("/{recipe_id}/comments")
def list_comments(recipe_id: str, limit: int = 20):
    comments_ref = db.collection("recipes").document(recipe_id).collection("comments").order_by("createdAt", direction=firestore.Query.DESCENDING).limit(limit)
    comments = [doc.to_dict() for doc in comments_ref.stream()]
    return {"items": comments}