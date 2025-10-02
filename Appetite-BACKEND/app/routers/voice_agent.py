from fastapi import APIRouter, UploadFile, File, Request, Form
from app.services.voice_agent import (
    voice_agent_inference as svc_voice_agent_inference
)

router = APIRouter(prefix="/api/v1/voice-agent", tags=["voice-agent"])


@router.post("/{recipe_id}")
async def voice_agent(
    request: Request,
    recipe_id: str,
    current_step: int = Form(...),
    file: UploadFile = File(...)
):
    return await svc_voice_agent_inference(request, recipe_id, current_step, file)