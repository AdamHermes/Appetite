# from google.cloud import firestore

# def get_client():
#     return firestore.Client()

# def save_recipe(uid: str, data: dict) -> str:
#     db = get_client()
#     doc = db.collection("users").document(uid).collection("recipes").document()
#     doc.set(data)
#     return doc.id

import os
import firebase_admin
from firebase_admin import firestore, credentials

# Initialize Firebase Admin once, prefer service account if provided
if not firebase_admin._apps:
    cred_path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")
    if cred_path and os.path.isfile(cred_path):
        firebase_admin.initialize_app(credentials.Certificate(cred_path))
    else:
        firebase_admin.initialize_app()

db = firestore.client()
