# app/routers/ws_voice_agent.py
from fastapi import APIRouter, WebSocket, WebSocketDisconnect
from loguru import logger
from pathlib import Path
from collections import deque
import io, wave, time
import numpy as np  # optional: for RMS debug

from app.services.gemini_api import gemini_classify_from_audio
from app.services.recipes import (
    get_next_step, get_previous_step, get_step, get_step_count 
)

from app.services.voice_agent import (
    is_voice_present_pcm, gemini_answer
)

router = APIRouter(prefix="/ws/v1/voice-agent", tags=["voice-agent-ws"])

SAMPLE_RATE = 16000
CHANNELS = 1
BYTES_PER_SAMPLE = 2
FRAME_BYTES = CHANNELS * BYTES_PER_SAMPLE            # 2 bytes/sample (PCM16 mono)
MAX_WINDOW_BYTES = SAMPLE_RATE * FRAME_BYTES * 5     # exactly 10 seconds
INFER_INTERVAL_MS = 5000                             # infer/save every 10s (non-overlapping)

DEBUG_DIR = Path("audio_debug")
DEBUG_DIR.mkdir(parents=True, exist_ok=True)


@router.websocket("/{recipe_id}")
async def voice_agent_ws(websocket: WebSocket, recipe_id: str):
    await websocket.accept()
    logger.info(f"WebSocket connected for recipe_id={recipe_id}")

    buffer = bytearray()
    current_step = 0

    try:
        while True:
            chunk = await websocket.receive_bytes()  # raw PCM16 mono @ 16kHz
            if not chunk:
                continue

            # Ensure frame alignment
            if len(chunk) % FRAME_BYTES != 0:
                chunk = chunk[:len(chunk) - (len(chunk) % FRAME_BYTES)]

            buffer.extend(chunk)

            # If we have at least 5 seconds worth of PCM
            if len(buffer) >= MAX_WINDOW_BYTES:
                # Take the first 5s (non-overlapping)
                five_sec_pcm = buffer[:MAX_WINDOW_BYTES]

                # Remove it from the buffer (so we only keep leftover for next round)
                buffer = buffer[MAX_WINDOW_BYTES:]

                # --- Debug RMS ---
                try:
                    arr = np.frombuffer(five_sec_pcm, dtype=np.int16).astype(np.float32) / 32768.0
                    rms = float(np.sqrt(np.mean(arr**2)))
                    logger.info(f"[WS] New 5s chunk RMS={rms:.4f}, bytes={len(five_sec_pcm)}")
                except Exception:
                    pass

                # Save debug 5s WAV
                # ts = int(time.time() * 1000)
                # debug_wav_path = DEBUG_DIR / f"{recipe_id}_{current_step}_{ts}.wav"
                # with open(debug_wav_path, "wb") as f:
                #     f.write(pcm_to_wav_bytes(five_sec_pcm, SAMPLE_RATE))
                # logger.info(f"Saved 5s debug WAV → {debug_wav_path}")

                # Run intent classification
                wav_bytes = pcm_to_wav_bytes(five_sec_pcm, SAMPLE_RATE)
                
                # if not await is_voice_present_pcm(wav_bytes): 
                #     tmp = {
                #     "intent": "noise",
                #     "transcript": "",
                #     "text_response": "",
                #     "current_step": current_step
                #     }
                #     logger.info(f"[skip] no voice")
                #     await websocket.send_json(tmp)
                
                result = await gemini_classify_from_audio(wav_bytes)

                transcript = result.get("transcript", "")
                intent = result.get("intent", "noise")
                num_step = get_step_count(recipe_id)

                if intent == "next":
                    response_text = get_next_step(recipe_id, current_step)
                    current_step = min(num_step - 1, current_step + 1)
                elif intent == "previous":
                    response_text = get_previous_step(recipe_id, current_step)
                    current_step = max(0, current_step - 1)
                elif intent == "repeat":
                    response_text = get_step(recipe_id, current_step)
                elif intent == "question":
                    response_text = await gemini_answer(recipe_id, transcript, current_step)
                else:
                    response_text = ""

                res = {
                    "intent": intent,
                    "transcript": transcript,
                    "text_response": response_text,
                    "current_step": current_step
                }

                print(res)

                await websocket.send_json(res)

    except WebSocketDisconnect:
        logger.info(f"Client disconnected recipe_id={recipe_id}")
    except Exception as e:
        logger.error(f"WebSocket error: {e}")


    except WebSocketDisconnect:
        logger.info(f"Client disconnected recipe_id={recipe_id}")
    except Exception as e:
        logger.error(f"WebSocket error: {e}")


def pcm_to_wav_bytes(pcm: bytes, sample_rate: int) -> bytes:
    out = io.BytesIO()
    with wave.open(out, "wb") as wf:
        wf.setnchannels(CHANNELS)
        wf.setsampwidth(BYTES_PER_SAMPLE)  # 16-bit little-endian
        wf.setframerate(sample_rate)
        wf.writeframes(pcm)
    return out.getvalue()
