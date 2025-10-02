import requests

# Replace these with your actual values
BASE_URL = "http://127.0.0.1:8000"
TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjkyZTg4M2NjNDY2M2E2MzMyYWRhNmJjMWU0N2YzZmY1ZTRjOGI1ZDciLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoic2RzZHNkMiIsImlzcyI6Imh0dHBzOi8vc2VjdXJldG9rZW4uZ29vZ2xlLmNvbS9hbmRyb2lkcHJvamVjdC1kY2QzZSIsImF1ZCI6ImFuZHJvaWRwcm9qZWN0LWRjZDNlIiwiYXV0aF90aW1lIjoxNzU2NjE0MTg4LCJ1c2VyX2lkIjoiZkxLM3c3ZXRqbVB3aXVQMGUyVG5PNHZCc1FYMiIsInN1YiI6ImZMSzN3N2V0am1Qd2l1UDBlMlRuTzR2QnNRWDIiLCJpYXQiOjE3NTY2MTQxODksImV4cCI6MTc1NjYxNzc4OSwiZW1haWwiOiJoZWxsb0BnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsImZpcmViYXNlIjp7ImlkZW50aXRpZXMiOnsiZW1haWwiOlsiaGVsbG9AZ21haWwuY29tIl19LCJzaWduX2luX3Byb3ZpZGVyIjoicGFzc3dvcmQifX0.V5b1qLPUE3QlPKAmBjnGHP4CH1uqDDEwJLH1SS8QJ2Ay0Ui16VnkqG61vkXfaTSuzXK6G0NXtR8Gyb_3qRSLhPeXQ3weT75hrBbvvQPPrlPFPl19fNDcvblYIKgeMFTyV3EC4TenVEPNHx0F3_KtddQsAF6htifcOlETa9Kk8bJ_ffR7xdZWKJMTZxs13jmvrx3XGwp0G9Eag-f9B6Pu1ay9ZFhsCCE_-iap-PYs0Cjpx4UU7W98MGydbqJpIiHMPT_HSbXh-t5FK2lxCrzlNSaIrduPkAa5KXiu0piEz00LxhGLFiSBAB3vVK0ptP1HiZxT6bGpmYwK68MfqBCFYQ"

headers = {
    "Authorization": f"Bearer {TOKEN}",
    "Content-Type": "application/json"
}

def test_create_profile():
    data = {
        "name": "Test User",
        "email": "testuser@example.com"
    }
    response = requests.post(f"{BASE_URL}/api/v1/user/me", headers=headers, json=data)
    print("Create profile status:", response.status_code)
    print("Response:", response.json())

def test_update_profile():
    data = {
        "profileUrl": "https://example.com/photo.jpg",
        "bio": "This is a test bio."
    }
    response = requests.patch(f"{BASE_URL}/api/v1/user/me", headers=headers, json=data)
    print("Update profile status:", response.status_code)
    print("Response:", response.json())

if __name__ == "__main__":
    test_update_profile()