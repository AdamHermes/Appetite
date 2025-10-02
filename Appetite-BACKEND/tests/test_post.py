import requests
import json

BASE_URL = "http://127.0.0.1:8000/api/v1/recipes/"
ID_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImUzZWU3ZTAyOGUzODg1YTM0NWNlMDcwNTVmODQ2ODYyMjU1YTcwNDYiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vYW5kcm9pZHByb2plY3QtZGNkM2UiLCJhdWQiOiJhbmRyb2lkcHJvamVjdC1kY2QzZSIsImF1dGhfdGltZSI6MTc1NzU3OTU2OCwidXNlcl9pZCI6IkdRTGZYckRNQnFZSTV1YUVZa3cwTFN6VFZHWjIiLCJzdWIiOiJHUUxmWHJETUJxWUk1dWFFWWt3MExTelRWR1oyIiwiaWF0IjoxNzU3NTc5NTY4LCJleHAiOjE3NTc1ODMxNjgsImVtYWlsIjoiY29ubWVvQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJlbWFpbCI6WyJjb25tZW9AZ21haWwuY29tIl19LCJzaWduX2luX3Byb3ZpZGVyIjoicGFzc3dvcmQifX0.NmqTctpndZSklN60muRUKw1QUzqXcBAThcsIoBgxfdx3aMyzNdvr2WaFr-A-W5o3ixO6gflXHD0125XaVylgVNd-qgAuObiDA_Cu5Dy9zk3wVryhG0t1tEzVhq9XBf_2gw_lgBAEU0b69bg9WnyPGqKpwXO-ZCVnv8JcMH0ZOgN0Yln4Kh8WHwPvK7cReLTVVsC1YOzrmUUtd_545SwNTkuPalGQeDpo_NIEeB_K66te_jC25dkGPgaeI_laUIwMQV093IEFXlgGox8Yu9jU4VGdosXasItlH2XRDPGsgBnqgw3koDj9rrAJ-GPjxJaXIlie2fvgqCHYw8gsr_Hn1g"
IMAGE_PATH = "D:/Downloads/BeefWellington.jpg"

headers = {
    "Authorization": f"Bearer {ID_TOKEN}"
}

def test_create_recipe():
    url = BASE_URL
    body = {
        "name": "Test Recipe",
        "category": "Dessert",
        "area": "French",
        "minutes": 30,
        "tags": ["sweet", "easy"],
        "ingredients": [
            {"ingredient": "Sugar", "measure": "100g"},
            {"ingredient": "Flour", "measure": "200g"}
        ],
        "steps": ["Mix ingredients", "Bake for 30 minutes"],
        "ratingAvg": 0.0,
        "youtubeUrl": None,
        "source": "App"
    }
    with open(IMAGE_PATH, "rb") as img_file:
        files = {
            "file": ("recipe.jpg", img_file, "image/jpeg"),
            "data": (None, json.dumps(body), "application/json")
        }
        response = requests.post(url, headers=headers, files=files)
        print("Status:", response.status_code)
        try:
            print("Response:", response.json())
        except Exception:
            print("Raw response:", response.text)

if __name__ == "__main__":
    test_create_recipe()