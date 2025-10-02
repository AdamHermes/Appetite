import time
from pathlib import Path
from fastapi import Request, UploadFile, File
import tempfile
from loguru import logger
from app.services.gemini_api import (
    gemini_classify_intent, gemini_answer, gemini_classify_from_audio
)
from app.services.recipes import (
    get_next_step, get_previous_step, get_step, get_step_count
)
import numpy as np
from pydub import AudioSegment
import io
import webrtcvad
import wave

vad = webrtcvad.Vad(3)  # aggressiveness: 0-3 (3 = most aggressive)

DEBUG_DIR = Path("audio_debug")
DEBUG_DIR.mkdir(parents=True, exist_ok=True)  # ensure folder exists

KEYWORDS = {
    "next": ["next", "tiếp", "tiếp theo"],
    "previous": ["previous", "back", "quay lại", "trước"],
    "repeat": ["repeat", "again", "nhắc lại", "lặp lại"],
}

async def voice_agent_inference(request, recipe_id: str, current_step: int, file: UploadFile):
    elapsed_time = time.time()

    audio_bytes = await file.read()

    # --- Save uploaded file into debug folder ---
    try:
        ts = int(time.time() * 1000)
        debug_path = DEBUG_DIR / f"{recipe_id}_{current_step}_{ts}.wav"
        with open(debug_path, "wb") as f:
            f.write(audio_bytes)
        logger.info(f"Saved debug audio: {debug_path}")
    except Exception as e:
        logger.warning(f"Failed to save debug audio: {e}")

    # --- VAD check ---
    if not await is_voice_present(audio_bytes):
        logger.info("Audio is no human voice → skipping STT")
        return ""

    result = await gemini_classify_from_audio(audio_bytes)

    transcript = result.get("transcript", "")
    intent = result.get("intent", "noise")
    new_step_index = current_step
    num_step = get_step_count(recipe_id)

    if intent == "next":
        response_text = get_next_step(recipe_id, current_step)
        new_step_index = current_step + 1
        new_step_index = max(0, min(num_step - 1, new_step_index))
    elif intent == "previous":
        response_text = get_previous_step(recipe_id, current_step)
        new_step_index = current_step - 1
        new_step_index = max(0, min(num_step - 1, new_step_index))
    elif intent == "repeat":
        response_text = get_step(recipe_id, current_step)
    elif intent == "question":
        response_text = await gemini_answer(recipe_id, transcript, current_step)
    else:
        response_text = ""

    res = {
        "intent": intent, 
        "text_response": response_text,
        "new_step_index": num_step
    }
    
    logger.info(f"Final response: {res}")
    logger.info(f"Delay: {time.time() - elapsed_time}s")
    return res

VAD = webrtcvad.Vad(3)   # 0..3 (2 is a good default)
FRAME_MS = 20
SR = 16000
BYTES_PER_SAMPLE = 2
FRAME_SIZE = int(SR * BYTES_PER_SAMPLE * FRAME_MS / 1000)

ENERGY_THRESH = 0.0035   # RMS threshold (tune to your mic/environment)
MIN_SPEECH_MS = 200      # must have at least 200ms voiced
MIN_RATIO = 0.15         # ≥15% frames must be voiced
MIN_DURATION_MS = 400    # ignore very short clips

async def is_voice_present(data: bytes) -> bool:
    try:
        # Decode m4a → PCM16 16kHz mono
        seg = AudioSegment.from_file(io.BytesIO(data), format="wav")
        seg = seg.set_frame_rate(SR).set_channels(1).set_sample_width(BYTES_PER_SAMPLE)
        pcm = seg.raw_data

        duration_ms = (len(pcm) / (SR * BYTES_PER_SAMPLE)) * 1000
        if duration_ms < MIN_DURATION_MS:
            return False

        # Quick RMS gate
        arr = np.frombuffer(pcm, dtype=np.int16).astype(np.float32) / 32768.0
        energy = np.sqrt(np.mean(arr**2))
        if energy < ENERGY_THRESH:
            return False

        # Frame-by-frame VAD
        num_frames = len(pcm) // FRAME_SIZE
        voiced_frames = 0
        for i in range(0, num_frames * FRAME_SIZE, FRAME_SIZE):
            frame = pcm[i:i+FRAME_SIZE]
            if len(frame) < FRAME_SIZE:
                break
            if VAD.is_speech(frame, SR):
                voiced_frames += 1

        voiced_ms = voiced_frames * FRAME_MS
        voiced_ratio = voiced_frames / max(1, num_frames)

        detected = voiced_ms >= MIN_SPEECH_MS and voiced_ratio >= MIN_RATIO

        logger.info(
            f"VAD check → dur={duration_ms:.0f}ms, energy={energy:.4f}, "
            f"voiced={voiced_ms}ms ({voiced_ratio:.1%}) → {detected}"
        )

        return detected
    except Exception as e:
        logger.warning(f"VAD failed: {e}")
        return True  # fail-open so you don’t miss commands

async def is_voice_present_pcm(pcm: bytes) -> bool:
    try:
        duration_ms = (len(pcm) / (SR * BYTES_PER_SAMPLE)) * 1000
        if duration_ms < MIN_DURATION_MS:
            return False

        arr = np.frombuffer(pcm, dtype=np.int16).astype(np.float32) / 32768.0
        energy = np.sqrt(np.mean(arr**2))
        if energy < ENERGY_THRESH:
            return False

        num_frames = len(pcm) // FRAME_SIZE
        voiced_frames = 0
        for i in range(0, num_frames * FRAME_SIZE, FRAME_SIZE):
            frame = pcm[i:i+FRAME_SIZE]
            if len(frame) < FRAME_SIZE:
                break
            if VAD.is_speech(frame, SR):
                voiced_frames += 1

        voiced_ms = voiced_frames * FRAME_MS
        voiced_ratio = voiced_frames / max(1, num_frames)
        return voiced_ms >= MIN_SPEECH_MS and voiced_ratio >= MIN_RATIO
    except Exception as e:
        logger.warning(f"VAD failed: {e}")
        return True
