from fastapi import (
    APIRouter, Depends, HTTPException, status
)
from typing import Optional

from app.auth import get_current_user, FirebaseUser
from app.services.follow import (
    svc_follow_user, 
    svc_unfollow_user, 
    svc_get_followers, 
    svc_get_following,
    svc_check_follow_status
)

router = APIRouter(prefix="/api/v1/follow", tags=["follow"])

@router.post("/{target_uid}")
def follow_user(target_uid: str, current_user: FirebaseUser = Depends(get_current_user)):
    try:
        result = svc_follow_user(current_user.uid, target_uid)
        if not result:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Unable to follow user")
        return {"success": True, "message": "Successfully followed user"}
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=str(e))


@router.delete("/{target_uid}")
def unfollow_user(target_uid: str, current_user: FirebaseUser = Depends(get_current_user)):
    try:
        result = svc_unfollow_user(current_user.uid, target_uid)
        if not result:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Unable to unfollow user")
        return {"success": True, "message": "Successfully unfollowed user"}
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=str(e))


@router.get("/followers/{user_id}")
def get_followers(user_id: str, limit: int = 50, last_doc_id: Optional[str] = None):
    try:
        followers = svc_get_followers(user_id, limit, last_doc_id)
        return {"followers": followers, "count": len(followers)}
    except Exception as e:
        raise HTTPException(status_code=500, detail="Failed to get followers")

@router.get("/following/{user_id}")
def get_following(user_id: str, limit: int = 50, last_doc_id: Optional[str] = None):
    try:
        following = svc_get_following(user_id, limit, last_doc_id)
        return {"following": following, "count": len(following)}
    except Exception as e:
        raise HTTPException(status_code=500, detail="Failed to get following")

@router.get("/status/{target_uid}")
def check_follow_status(target_uid: str, current_user: FirebaseUser = Depends(get_current_user)):
    try:
        is_following = svc_check_follow_status(current_user.uid, target_uid)
        return {"success": True, "isFollowing": is_following}
    except Exception as e:
        raise HTTPException(status_code=500, detail="Failed to check follow status")