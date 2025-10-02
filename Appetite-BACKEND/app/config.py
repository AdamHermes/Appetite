import os
from functools import lru_cache
from pydantic import BaseModel
from typing import Optional

class Settings(BaseModel):
    project_id: Optional[str] = os.getenv("FIREBASE_PROJECT_ID")
    allowed_origins: list[str] = os.getenv(
        "ALLOWED_ORIGINS",
        "http://localhost:5173,http://127.0.0.1:5173"
    ).split(",")

    # Full JSON string for Railway deployments
    firebase_service_account_json: Optional[str] = os.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")

    # Local dev: path to serviceAccount.json
    google_creds_path: Optional[str] = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")

    # Optional: storage bucket (useful for Firebase Storage)
    firebase_storage_bucket: Optional[str] = os.getenv("FIREBASE_STORAGE_BUCKET")

@lru_cache
def get_settings() -> Settings:
    return Settings()
