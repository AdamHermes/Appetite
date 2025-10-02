# app/utils/users.py
from typing import Optional, Dict, Any, List
from functools import lru_cache
import firebase_admin
from firebase_admin import firestore, auth as fb_auth
from app.db.firestore import db

# Whitelist what you consider "public" profile fields
PUBLIC_FIELDS: List[str] = [
    "name", "email", "bio"
]

def _filter_public(d: Dict[str, Any]) -> Dict[str, Any]:
    return {k: v for k, v in (d or {}).items() if k in PUBLIC_FIELDS}

@lru_cache(maxsize=2048)
def get_user_public(uid: str) -> Optional[Dict[str, Any]]:
    """
    Return the *public* profile for a given user id (users/{uid}).
    Falls back to Firebase Auth display_name/photo_url if Firestore doc missing.
    Result is cached in-process for speed.
    """
    if not uid:
        return None

    snap = db.collection("users").document(uid).get()
    if snap.exists:
        data = snap.to_dict() or {}
        data["uid"] = uid
        return _filter_public(data)

    # Fallback to Auth record (minimal public info)
    try:
        u = fb_auth.get_user(uid)
        return {
            "uid": uid,
            "displayName": (u.display_name or None),
            "photoURL": (u.photo_url or None),
        }
    except Exception:
        return {"uid": uid}

def get_user_private(uid: str) -> Optional[Dict[str, Any]]:
    """
    Return the *full* profile document for internal/server use.
    Do NOT expose this raw to clients—filter first.
    """
    if not uid:
        return None
    snap = db.collection("users").document(uid).get()
    if not snap.exists:
        return None
    data = snap.to_dict() or {}
    data["uid"] = uid
    return data
