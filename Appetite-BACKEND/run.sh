#!/bin/bash
export PATH="/Users/hainguyen/Package:$PATH"
uvicorn app.main:app --host 0.0.0.0 --port 8000