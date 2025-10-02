# routers/auth.py
from fastapi import APIRouter, Depends
from app.auth import get_current_user, FirebaseUser

router = APIRouter(prefix="/api/v1/auth", tags=["auth"])

@router.get("/me")
def me(user: FirebaseUser = Depends(get_current_user)):
    return {
        "uid": user.uid,
        "email": user.email,
        "provider": user.claims.get("firebase", {}).get("sign_in_provider"),
    }