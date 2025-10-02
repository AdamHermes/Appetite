import requests

# Paste your token and recipe ID here
TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjkyZTg4M2NjNDY2M2E2MzMyYWRhNmJjMWU0N2YzZmY1ZTRjOGI1ZDciLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL3NlY3VyZXRva2VuLmdvb2dsZS5jb20vYW5kcm9pZHByb2plY3QtZGNkM2UiLCJhdWQiOiJhbmRyb2lkcHJvamVjdC1kY2QzZSIsImF1dGhfdGltZSI6MTc1NjM5MjY4NSwidXNlcl9pZCI6IlRadEtiWEkxWXNQdmNoWFlaTFEwMnNyUlFrSDMiLCJzdWIiOiJUWnRLYlhJMVlzUHZjaFhZWkxRMDJzclJRa0gzIiwiaWF0IjoxNzU2MzkyNjg2LCJleHAiOjE3NTYzOTYyODYsImVtYWlsIjoiY29uY2FvQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwiZmlyZWJhc2UiOnsiaWRlbnRpdGllcyI6eyJlbWFpbCI6WyJjb25jYW9AZ21haWwuY29tIl19LCJzaWduX2luX3Byb3ZpZGVyIjoicGFzc3dvcmQifX0.h7yCHVxVXVAEESoYzT5zyjfyVU-HLsFkskycK1LZbpRhHLbBFCvJe1u84fFQIRz4LFvOo8KYg22q50PUdI8R1L6T3wElram9X5Zt8wUgSsbedXdtITA024czt_8OcOcpX-w9ANoiIKDjAjh8ZNWCTacGAYvgJDxbJ2LLu-j1Y33XfAJ91XTnJZY90foYS_tkDO03s9zS-E-PVD59iJeOAQW042a9rm3F8f1a9QeqYqmZHlI0rYn_kH4MlGKmYlBIOkvvqcBmDkk3C4npVgD-dAqGdlFTAyHHaUsID1hbgW_gTptnMdMj46yormj2vdbK52Nd-0ff8oGeCNcDge7XSA"
RECIPE_ID =  "52765"
url = f"http://127.0.0.1:8000/api/v1/recipes/{RECIPE_ID}/comments"
headers = {
    "Authorization": f"Bearer {TOKEN}",
    "Content-Type": "application/json"
}

data = {
    "text": "This is a test comment from a script!"
}

response = requests.post(url, headers=headers, json=data)
print(url)
print("Status:", response.status_code)
print("Response:", response.text)