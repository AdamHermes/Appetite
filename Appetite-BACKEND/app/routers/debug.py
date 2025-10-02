# app/debug.py
from fastapi import APIRouter
import firebase_admin

router = APIRouter(prefix="/api/v1/debug", tags=["debug"])

@router.get("/firebase")
def firebase_info():
    app = firebase_admin.get_app()  # raises if not initialized
    # Most SDKs store project in app.options["projectId"]
    project_id = getattr(app, "project_id", None) or app.options.get("projectId")
    return {
        "initialized": True,
        "app_name": app.name,
        "project_id": project_id,
    }