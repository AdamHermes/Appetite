# app/auth.py
from __future__ import annotations
from typing import Optional
import os, json, logging
from pathlib import Path

from fastapi import Depends, Header, HTTPException, status
from firebase_admin import auth, credentials
import firebase_admin

# --- Logging setup ---
logger = logging.getLogger("auth")
logger.setLevel(logging.INFO)
from dotenv import load_dotenv
load_dotenv()
# --- Firebase Admin initialization ---
if not firebase_admin._apps:
    # Try Railway-style JSON env first
    fb_sa_json = os.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")
    project_id = None

    if fb_sa_json:
        try:
            sa = json.loads(fb_sa_json)
            project_id = sa.get("project_id")
            cred = credentials.Certificate(sa)
            firebase_admin.initialize_app(cred, {"projectId": project_id} if project_id else None)
            logger.info(f"Firebase Admin initialized from FIREBASE_SERVICE_ACCOUNT_JSON for project: {project_id}")
        except Exception as e:
            raise RuntimeError(f"Failed to parse FIREBASE_SERVICE_ACCOUNT_JSON: {e}")

    else:
        # Fallback: look for GOOGLE_APPLICATION_CREDENTIALS file path (local dev)
        creds_path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
        if not creds_path:
            raise RuntimeError(
                "No Firebase credentials found. "
                "Set FIREBASE_SERVICE_ACCOUNT_JSON (Railway) or GOOGLE_APPLICATION_CREDENTIALS (local)."
            )

        cred_file = Path(creds_path)
        if not cred_file.is_file():
            raise RuntimeError(f"GOOGLE_APPLICATION_CREDENTIALS points to a missing file: {cred_file}")

        try:
            with cred_file.open("r", encoding="utf-8") as f:
                sa = json.load(f)
            project_id = sa.get("project_id")
        except Exception as e:
            raise RuntimeError(f"Failed to read service account JSON: {e}")

        cred = credentials.Certificate(str(cred_file))
        firebase_admin.initialize_app(cred, {"projectId": project_id} if project_id else None)
        logger.info(f"Firebase Admin initialized from file for project: {project_id}")

# --- Simple user container ---
class FirebaseUser:
    def __init__(self, uid: str, name: str,email: Optional[str], claims: dict):
        self.uid = uid
        self.name = name
        self.email = email
        self.claims = claims

# --- Dependency to verify ID token ---
async def get_current_user(
    authorization: Optional[str] = Header(default=None),
) -> FirebaseUser:
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Missing Bearer token",
        )

    id_token = authorization.split(" ", 1)[1]
    try:
        decoded = auth.verify_id_token(id_token, check_revoked=True)
        return FirebaseUser(
            uid=decoded["uid"],
            name=decoded.get("name"),
            email=decoded.get("email"),
            claims=decoded,
        )
    except auth.RevokedIdTokenError:
        logger.warning("Token revoked")
        raise HTTPException(status_code=401, detail="Token revoked")
    except auth.ExpiredIdTokenError:
        logger.warning("Token expired")
        raise HTTPException(status_code=401, detail="Token expired")
    except Exception as e:
        logger.exception("verify_id_token failed")
        raise HTTPException(status_code=401, detail="Invalid or expired token")
