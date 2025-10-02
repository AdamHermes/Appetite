import os
import torch
import clip
from contextlib import asynccontextmanager
from loguru import logger
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from firebase_admin import firestore
import whisper


from app.routers import (
    recipes, auth, debug, comments, user, searchSimRecipe, 
    voice_agent, searchIngredients, follow, ws_voice_agent) #, feed
from app.services.searchSimRecipe import build_faiss_index

@asynccontextmanager
async def lifespan(app: FastAPI):
    # --- Startup ---
    logger.info("Choose device...")
    app.state.device = "cuda" if torch.cuda.is_available() else "cpu"

    # Load CLIP
    logger.info("Loading CLIP model...")
    model, preprocess = clip.load("ViT-B/32", device=app.state.device)
    app.state.model = model
    app.state.preprocess = preprocess
    logger.info("CLIP model ready")

    # Load Whisper STT
    # logger.info("Loading Whisper STT model...")
    # app.state.whisper = whisper.load_model("large-v3-turbo", device=app.state.device)
    # logger.info("Whisper STT ready")

    # Firestore
    logger.info("Connecting Firestore...")
    app.state.db = firestore.client()
    logger.info("Firestore ready")

    # Build FAISS
    logger.info("Fetching and Building feature index...")
    index, ids = build_faiss_index(app)
    app.state.faiss_index = index
    app.state.id_mapping = ids
    logger.info("Feature index ready")

    yield   # Where the app runs

    # --- Shutdown ---
    logger.info("Shutting down app")


def create_app():
    app = FastAPI(title="Appetite App", version="1.0.0", lifespan=lifespan)

    allowed = os.getenv("ALLOWED_ORIGINS", "http://localhost:5173").split(",")
    app.add_middleware(
        CORSMiddleware,
        allow_origins=[o.strip() for o in allowed],
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    app.include_router(auth.router)
    app.include_router(recipes.router)
    app.include_router(searchSimRecipe.router)
    app.include_router(debug.router)
    app.include_router(user.router)
    app.include_router(searchIngredients.router)
    app.include_router(follow.router)
    # app.include_router(feed.router)
    app.include_router(comments.router)
    app.include_router(voice_agent.router)
    app.include_router(ws_voice_agent.router)
    

    return app


app = create_app()

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000, reload=True)
