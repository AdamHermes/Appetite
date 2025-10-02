import requests

BASE_URL = "http://127.0.0.1:8000"
RECIPE_ID = "00bsstSBoHQEgdU01Vfu"
ID_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImVmMjQ4ZjQyZjc0YWUwZjk0OTIwYWY5YTlhMDEzMTdlZjJkMzVmZTEiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vYW5kcm9pZHByb2plY3QtZGNkM2UiLCJhdWQiOiJhbmRyb2lkcHJvamVjdC1kY2QzZSIsImF1dGhfdGltZSI6MTc1Njc5OTA1MywidXNlcl9pZCI6IlRadEtiWEkxWXNQdmNoWFlaTFEwMnNyUlFrSDMiLCJzdWIiOiJUWnRLYlhJMVlzUHZjaFhZWkxRMDJzclJRa0gzIiwiaWF0IjoxNzU2Nzk5MDU0LCJleHAiOjE3NTY4MDI2NTQsImVtYWlsIjoiY29uY2FvQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJlbWFpbCI6WyJjb25jYW9AZ21haWwuY29tIl19LCJzaWduX2luX3Byb3ZpZGVyIjoicGFzc3dvcmQifX0.J-k-cZo3p2m_babp8mXJiKls-_riz-A7t2-BW2B_jY66robWV8cAYhDEijNvt8a3feSEGSY9NlDZDvA4Is8Kzk7BDzivgvI0tz61mAoD1UydhLbcGrspTNiDN7DHDC_aW3UhE8QSjRGADVkRybQO0svQmcxO4K2exsWXf6SEd2tGFo_dGkEjLVPQ6dU4XfhVGSdyA-qMnrgcd6Fk98CkuOF8_YP4Dj8pAfAmsH3NNt8rlJkzeWYJQRVr4lsnmpKj_JYm-T_yF49nD1cs5eUdjv2H3K68pfvYbuLPIaPaOlD1EnSmQXVCrOMTHPHajNdZ51hsH8_78AenTQANCBpSKg"

headers = {
    "Authorization": f"Bearer {ID_TOKEN}",
    "Content-Type": "application/json"
}

def test_rate_recipe(value: int):
    url = f"{BASE_URL}/api/v1/recipes/{RECIPE_ID}/rate"
    data = {"value": value}
    response = requests.post(url, headers=headers, json=data)
    print("Status:", response.status_code)
    print("Response:", response.json())

def test_get_my_rating():
    url = f"{BASE_URL}/api/v1/recipes/{RECIPE_ID}/my-rating"
    response = requests.get(url, headers=headers)
    print("Status:", response.status_code)
    print("Response:", response.json())

if __name__ == "__main__":
    # Test with a rating value (1-5)
    # test_rate_recipe(4)
    test_get_my_rating()