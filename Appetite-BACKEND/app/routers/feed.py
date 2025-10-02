# # app/routers/feed.py
# # Usage:
#     # GET /api/v1/feed/feedback
#     # GET /api/v1/feed/

# from typing import Optional, Dict, Any, List, Tuple
# from fastapi import APIRouter, Depends, HTTPException, Query
# from pydantic import BaseModel, Field
# import base64, json, math, time

# from firebase_admin import firestore
# import firebase_admin 
# from app.auth import get_current_user, FirebaseUser

# # --- Firestore init (safe even if already initialized elsewhere) ---
# if not firebase_admin._apps:
#     firebase_admin.initialize_app()

# router = APIRouter(prefix="/api/v1/feed", tags=["feed"])
# db = firestore.client()

# # ---------------------------
# # Utilities: opaque cursor
# # ---------------------------
# def _encode_cursor(idx: int) -> str:
#     return base64.urlsafe_b64encode(json.dumps({"i": idx}).encode()).decode()

# def _decode_cursor(token: str) -> int:
#     try:
#         return int(json.loads(base64.urlsafe_b64decode(token.encode()).decode())["i"])
#     except Exception:
#         raise HTTPException(status_code=400, detail="Invalid page_token.")

# # ---------------------------
# # Feedback model (reuse for /feedback)
# # ---------------------------
# class FeedbackIn(BaseModel):
#     recipe_id: str = Field(..., description="Target recipe id")
#     action: str = Field(..., pattern="^(like|dislike|hide|save)$")

# @router.post("/feedback")
# def give_feedback(
#     fb: FeedbackIn,
#     user: FirebaseUser = Depends(get_current_user),
# ):
#     db.collection("feedback").document(user.uid).collection("recipes").document(fb.recipe_id).set({
#         "action": fb.action,
#         "ts": firestore.SERVER_TIMESTAMP,
#         "uid": user.uid,
#     })
#     return {"ok": True}

# # ---------------------------
# # Simple personalization feed
# # ---------------------------
# def _norm(x: float, lo: float, hi: float) -> float:
#     if hi <= lo: return 0.0
#     return max(0.0, min(1.0, (x - lo) / (hi - lo)))

# def _recency_boost(created_ts: float) -> float:
#     """
#     Exponential decay: ~0.0 after ~14 days.
#     created_ts: seconds since epoch
#     """
#     now = time.time()
#     age_days = max(0.0, (now - created_ts) / 86400.0)
#     # Half-life ~3 days -> recent gets a nice bump
#     return math.exp(-math.log(2) * (age_days / 3.0))

# def _tag_affinity(recipe_tags: List[str], prefs: Dict[str, float]) -> float:
#     if not recipe_tags or not prefs: return 0.0
#     return sum(prefs.get(t, 0.0) for t in recipe_tags) / max(1, len(recipe_tags))

# def _build_user_signals(uid: str, like_limit: int = 50):
#     """
#     Signals:
#       - liked tags (from last N likes)
#       - followed creators
#       - hidden recipe IDs
#     """
#     # Followed creators
#     followed = {d.id for d in db.collection("follows").document(uid).collection("users").stream()}

#     # Hidden recipes
#     hidden = set()
#     fb_q = (db.collection("feedback").document(uid).collection("recipes")
#               .where("action", "in", ["hide"])
#               .limit(200))
#     for s in fb_q.stream():
#         hidden.add(s.id)

#     # Recent likes -> tag prefs
#     likes = []
#     like_q = (db.collection("feedback").document(uid).collection("recipes")
#                 .where("action", "==", "like")
#                 .order_by("ts", direction=firestore.Query.DESCENDING)
#                 .limit(like_limit))
#     for s in like_q.stream():
#         likes.append(s.id)

#     tag_counts: Dict[str, int] = {}
#     if likes:
#         # fetch liked recipe docs (fan-out but small N)
#         for rid in likes:
#             snap = db.collection("recipes").document(rid).get()
#             if not snap.exists: continue
#             d = snap.to_dict() or {}
#             for t in (d.get("tags") or []):
#                 tag_counts[t] = tag_counts.get(t, 0) + 1

#     # Normalize tag prefs to [0,1]
#     if tag_counts:
#         mx = max(tag_counts.values())
#         tag_prefs = {k: v / mx for k, v in tag_counts.items()}
#     else:
#         tag_prefs = {}

#     return {
#         "followed": followed,
#         "hidden": hidden,
#         "tag_prefs": tag_prefs,
#     }

# def _personal_score(recipe: Dict[str, Any], signals: Dict[str, Any]) -> float:
#     hot = float(recipe.get("hotScore") or 0.0)
#     created_at = recipe.get("createdAt")
#     created_ts = 0.0
#     # Firestore timestamp to seconds
#     if hasattr(created_at, "timestamp"):
#         created_ts = created_at.timestamp()
#     elif isinstance(created_at, (int, float)):
#         created_ts = float(created_at)

#     # Components
#     s_hot = _norm(hot, 0.0, 1000.0)                 # normalize global popularity
#     s_tag = _tag_affinity(recipe.get("tags") or [], signals["tag_prefs"])
#     s_rec = _recency_boost(created_ts)
#     s_follow = 1.0 if recipe.get("contributorId") in signals["followed"] else 0.0
#     s_hidden_penalty = -0.6 if recipe.get("id") in signals["hidden"] else 0.0
#     s_own_penalty = -0.3 if recipe.get("contributorId") == recipe.get("viewerUid") else 0.0

#     # Weights (tweakable)
#     return (
#         0.9 * s_hot +
#         1.2 * s_tag +
#         0.8 * s_rec +
#         0.5 * s_follow +
#         s_hidden_penalty +
#         s_own_penalty
#     )

# @router.get("/")
# def get_feed(
#     limit: int = Query(20, ge=1, le=100),
#     page_token: Optional[str] = Query(None, description="Opaque index into personalized list"),
#     candidate_pool: int = Query(200, ge=50, le=500, description="How many to consider before scoring"),
#     user: FirebaseUser = Depends(get_current_user),
#     variant: str = Query("hybrid", description="popular|fresh|hybrid"),
# ) -> Dict[str, Any]:
#     """
#     Personalized feed (demo-grade):
#       - Pull a candidate pool ordered by stored fields (hotScore or createdAt)
#       - Score per-user in app (tag affinity, follow boost, recency, global hotScore)
#       - Return top-N with cursor pagination over the sorted personalized list
#     """
#     # 1) User signals
#     signals = _build_user_signals(user.uid)

#     # 2) Candidate pool (cheap Firestore query)
#     col = db.collection("recipes")
#     if variant == "fresh":
#         q = col.order_by("createdAt", direction=firestore.Query.DESCENDING).order_by("__name__")
#     else:
#         # popular or hybrid: use hotScore as base
#         q = col.order_by("hotScore", direction=firestore.Query.DESCENDING).order_by("__name__")

#     snaps = list(q.limit(candidate_pool).stream())

#     # 3) Score in memory
#     items: List[Dict[str, Any]] = []
#     for s in snaps:
#         d = s.to_dict() or {}
#         d["id"] = s.id
#         d["viewerUid"] = user.uid
#         d["_score"] = _personal_score(d, signals)
#         items.append(d)

#     # 4) Sort by personalized score (desc), then tie-break by hotScore and id
#     items.sort(key=lambda r: (r.get("_score", 0.0), float(r.get("hotScore") or 0.0), r["id"]), reverse=True)

#     # 5) Cursor pagination over personalized order
#     start_idx = _decode_cursor(page_token) if page_token else 0
#     page = items[start_idx:start_idx + limit]
#     next_token = _encode_cursor(start_idx + limit) if (start_idx + limit) < len(items) else None

#     # Strip internal fields
#     for r in page:
#         r.pop("viewerUid", None)
#         r.pop("_score", None)

#     return {"items": page, "nextPageToken": next_token}
