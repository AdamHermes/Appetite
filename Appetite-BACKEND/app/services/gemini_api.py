import os
import google.generativeai as genai
from loguru import logger
from PIL import Image
from io import BytesIO
import json
from app.db.firestore import db
import re

from app.services.recipes import get_step


# Configure Gemini
genai.configure(api_key=os.getenv("GEMINI_API_KEY"))

VALID_INTENTS = ["next", "previous", "repeat", "question", "noise"]

async def gemini_classify_from_audio(
    audio_bytes: bytes) -> dict:
    """
    Send audio directly to Gemini.
    Returns dict: {"transcript": "...", "intent": "..."}
    """

    prompt = """
    You are an intent classification assistant for a cooking voice agent.
    First, transcribe the speech from the audio.
    Then classify the user’s utterance into exactly one of:
    - next
    - previous
    - repeat
    - question
    - noise (anything irrelevant, unclear, or small talk)

    Return JSON only:
    {"transcript": "...", "intent": "..."}
    """

    try:
        model = genai.GenerativeModel("gemini-2.5-flash")
        response = model.generate_content(
            [prompt, {"mime_type": "audio/mp4", "data": audio_bytes}]
        )
        raw = response.text.strip()
        logger.info(f"Gemini audio classification raw response: {raw}")

        # Strip Markdown fences if present
        cleaned = re.sub(r"^```[a-zA-Z]*\s*|\s*```$", "", raw, flags=re.DOTALL).strip()

        data = json.loads(cleaned)
        intent = data.get("intent", "noise").lower()
        transcript = data.get("transcript", "")

        return {
            "transcript": transcript,
            "intent": intent
        }
    
    except Exception as e:
        logger.error(f"Gemini classify audio failed: {e}")
        return {"transcript": "", "intent": "noise"}
    

async def gemini_classify_intent(text: str) -> str:
    """
    Use Gemini to classify user text into: (next | previous | repeat | question | noise)
    """

    logger.info(f"Gemini intent classification for: {text}")

    prompt = f"""
    You are an intent classification assistant for a cooking voice agent.
    Classify the user's utterance into exactly one of these categories:
    - next
    - previous
    - repeat
    - question
    - noise (anything irrelevant, unclear, or small talk)

    User said: "{text}"

    Return only the label.
    """

    try:
        model = genai.GenerativeModel("gemini-2.5-flash")  # free-tier model
        response = model.generate_content(prompt)
        label = response.text.strip().lower()

        if label in VALID_INTENTS:
            return label
        else:
            return "noise"

    except Exception as e:
        logger.error(f"Gemini classify intent failed: {e}")
        return "noise"

async def gemini_answer(recipe_id: str, question: str, step_index: int) -> str:
    """
    Use Gemini to answer a user question about the recipe.
    Provide recipe name, ingredients, all steps, and current step for context.
    """
    try:
        # Load recipe doc
        doc = db.collection("recipes").document(recipe_id).get()
        if not doc.exists:
            return "I couldn’t find this recipe."

        recipe = doc.to_dict()
        name = recipe.get("name", "Unnamed recipe")
        category = recipe.get("category", "")
        area = recipe.get("area", "")
        ingredients = recipe.get("ingredients", [])
        steps = recipe.get("steps", [])

        # Format ingredients nicely
        ingredients_text = "\n".join(
            f"- {ing.get('ingredient')} ({ing.get('measure')})"
            for ing in ingredients
        )

        # Format steps
        steps_text = "\n".join(
            f"{i+1}. {s}" for i, s in enumerate(steps)
        )

        current_step = get_step(recipe_id, step_index)

        # Prompt for Gemini
        prompt = f"""
        You are a friendly cooking assistant.
        Base your answers mainly on the provided recipe context. 
        If the user asks about modifications or extra ingredients not in the recipe,
        you may politely explain that it is not in the original recipe,
        but you can still suggest how it could be added or adapted.

        Recipe: {name}
        Category: {category}, Area: {area}

        Ingredients:
        {ingredients_text}

        Steps:
        {steps_text}

        Current step (#{step_index+1}): {current_step}

        User’s question: "{question}"

        Answer in a natural, conversational tone that feels supportive and succinct.
        """



        model = genai.GenerativeModel("gemini-2.5-flash")
        response = model.generate_content(prompt)
        if response.candidates and response.candidates[0].content.parts:
            answer = response.candidates[0].content.parts[0].text
            return answer.strip()
        else:
            return "Sorry, I couldn’t generate an answer."

    except Exception as e:
        logger.error(f"Gemini Q&A failed: {e}")
        return "Sorry, I couldn't answer that."
    


# Choose a lightweight model for structured extraction

async def extract_ingredients_from_image(image_bytes: bytes):
    """
    Send image to Gemini and extract ingredient names.
    Returns a list like ["tomato", "egg", "olive oil"]
    """
    # Convert bytes → PIL → send as inline image
    pil_img = Image.open(BytesIO(image_bytes)).convert("RGB")

    prompt = (
        "Extract a clean JSON array of ingredients visible in this image. "
        "Return ONLY a JSON array of lowercase strings, like: [\"tomato\", \"egg\"]"
    )

    try:
        model = genai.GenerativeModel("gemini-2.5-flash")  # free-tier model
        response = model.generate_content([prompt, pil_img])
        raw_text = response.text.strip()

        # Sometimes Gemini adds ```json ... ```
        raw_text = raw_text.strip("`").replace("json", "").strip()

        ingredients = json.loads(raw_text)
        return [ing.strip().lower() for ing in ingredients if isinstance(ing, str)]

    except Exception as e:
        print("⚠️ Failed to parse Gemini response:", e)
        return []

