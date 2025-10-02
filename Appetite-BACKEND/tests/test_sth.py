import json

with open("app/serviceAccount.json", "r") as f:
    sa = f.read()

print(json.dumps({"FIREBASE_SERVICE_ACCOUNT_JSON": sa}))
