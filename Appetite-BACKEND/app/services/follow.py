from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime, timezone
from google.cloud import firestore
from app.db.firestore import db
from app.services.users import svc_get_user_by_id, Profile

class Follow(BaseModel):
    id: str
    followerId: str
    followerName: str 
    followerProfileUrl: Optional[str] = None  
    followingId: str
    followingName: str 
    followingProfileUrl: Optional[str] = None 
    createdAt: datetime

@firestore.transactional
def _follow_transaction(transaction, follower_id: str, following_id: str, follower_data: dict, following_data: dict):
    follower_ref = db.collection("users").document(follower_id)
    following_ref = db.collection("users").document(following_id)
    follow_ref = db.collection("follows").document(f"{follower_id}_{following_id}")
    
    # Use document.get(transaction=transaction) to get a snapshot
    follow_snap = follow_ref.get(transaction=transaction)
    if follow_snap.exists:
        return False

    transaction.set(follow_ref, {
        "followerId": follower_id,
        "followerName": follower_data.get("name"),
        "followerProfileUrl": follower_data.get("profileUrl"),
        "followingId": following_id,
        "followingName": following_data.get("name"),
        "followingProfileUrl": following_data.get("profileUrl"),
        "createdAt": datetime.now(timezone.utc)
    })

    transaction.update(follower_ref, {"followingCount": firestore.Increment(1)})
    transaction.update(following_ref, {"followersCount": firestore.Increment(1)})
    return True

def svc_follow_user(follower_id: str, following_id: str) -> bool:
    if follower_id == following_id:
        return False
    follower_doc = db.collection("users").document(follower_id).get()
    following_doc = db.collection("users").document(following_id).get()
    if not follower_doc.exists or not following_doc.exists:
        return False

    follower_data = follower_doc.to_dict() or {}
    following_data = following_doc.to_dict() or {}

    transaction = db.transaction()
    return _follow_transaction(transaction, follower_id, following_id, follower_data, following_data)


@firestore.transactional
def _unfollow_transaction(transaction, follower_id: str, following_id: str):
    follower_ref = db.collection("users").document(follower_id)
    following_ref = db.collection("users").document(following_id)
    follow_ref = db.collection("follows").document(f"{follower_id}_{following_id}")
    
    # Use document.get(transaction=transaction) to get a snapshot
    follow_snap = follow_ref.get(transaction=transaction)
    if not follow_snap.exists:
        return False

    transaction.delete(follow_ref)
    transaction.update(follower_ref, {"followingCount": firestore.Increment(-1)})
    transaction.update(following_ref, {"followersCount": firestore.Increment(-1)})
    return True

def svc_unfollow_user(follower_id: str, following_id: str) -> bool:
    transaction = db.transaction()
    return _unfollow_transaction(transaction, follower_id, following_id)


def svc_get_followers(user_id: str, limit: int = 50, last_doc_id: Optional[str] = None) -> List[Profile]:
    query = db.collection("follows").where("followingId", "==", user_id).order_by("createdAt", direction=firestore.Query.DESCENDING).limit(limit)
    if last_doc_id:
        last_doc = db.collection("follows").document(last_doc_id).get()
        if last_doc.exists:
            query = query.start_after(last_doc)

    followers = []
    for doc in query.stream():
        follower_id = doc.to_dict().get("followerId")
        profile = svc_get_user_by_id(follower_id)
        if profile:
            followers.append(profile)
    return followers

def svc_get_following(user_id: str, limit: int = 50, last_doc_id: Optional[str] = None) -> List[Profile]:
    query = db.collection("follows").where("followerId", "==", user_id).order_by("createdAt", direction=firestore.Query.DESCENDING).limit(limit)
    if last_doc_id:
        last_doc = db.collection("follows").document(last_doc_id).get()
        if last_doc.exists:
            query = query.start_after(last_doc)

    following = []
    for doc in query.stream():
        following_id = doc.to_dict().get("followingId")
        profile = svc_get_user_by_id(following_id)
        if profile:
            following.append(profile)
    return following

def svc_get_followers_quick(user_id: str, limit: int = 50) -> List[Follow]:
    docs = db.collection("follows").where("followingId", "==", user_id).order_by("createdAt", direction=firestore.Query.DESCENDING).limit(limit).stream()
    return [Follow(id=doc.id, **doc.to_dict()) for doc in docs]

def svc_get_following_quick(user_id: str, limit: int = 50) -> List[Follow]:
    docs = db.collection("follows").where("followerId", "==", user_id).order_by("createdAt", direction=firestore.Query.DESCENDING).limit(limit).stream()
    return [Follow(id=doc.id, **doc.to_dict()) for doc in docs]

def svc_check_follow_status(follower_id: str, following_id: str) -> bool:
    if follower_id == following_id:
        return False
    
    follow_ref = db.collection("follows").document(f"{follower_id}_{following_id}")
    follow_doc = follow_ref.get()
    return follow_doc.exists