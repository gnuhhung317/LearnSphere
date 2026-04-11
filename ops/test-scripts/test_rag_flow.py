import requests
import time
import os

# Configuration
BASE_URL = "http://localhost:8079/api/v1"
USERNAME = "duchung02nd@gmail.com"  # Replace with valid user
PASSWORD = "duchung02nd@gmail.com" # Replace with valid password
TEST_FILE_PATH = "test_document.txt"
ROOM_ID = 1 # Replace with valid room ID where bot is present

def login():
    print(f"Logging in as {USERNAME}...")
    # Adjust login endpoint based on actual Auth Service implementation
    # Assuming standard OAuth2/OpenID or custom login
    url = f"{BASE_URL}/auth/login"
    payload = {
        "email": USERNAME,
        "password": PASSWORD
    }
    try:
        response = requests.post(url, json=payload)
        response.raise_for_status()
        token = response.json().get("accessToken")
        print("Login successful.")
        return token
    except Exception as e:
        print(f"Login failed: {e}")
        # Return mock token for dev environment if auth is disabled/mocked
        return "mock-token"

def upload_file(token):
    print("Uploading file...")
    url = f"{BASE_URL}/media/upload"
    headers = {"Authorization": f"Bearer {token}"}
    
    # Create a dummy file if not exists
    if not os.path.exists(TEST_FILE_PATH):
        with open(TEST_FILE_PATH, "w") as f:
            f.write("StudyHub is a comprehensive platform for students to collaborate, share resources, and learn together using AI-powered tools.")
            
    files = {"file": open(TEST_FILE_PATH, "rb")}
    
    try:
        response = requests.post(url, headers=headers, files=files)
        response.raise_for_status()
        file_id = response.json().get("fileId")
        print(f"File uploaded. ID: {file_id}")
        return file_id
    except Exception as e:
        print(f"Upload failed: {e}")
        return None

def test_rag_query(token, room_id):
    print("Testing RAG query...")
    # Using Chat Service to send message with @bot
    url = f"{BASE_URL}/rooms/{room_id}/channels/1/messages" # Assuming channel 1
    headers = {"Authorization": f"Bearer {token}"}
    
    query = "@bot What is StudyHub?"
    payload = {
        "content": query
    }
    
    try:
        response = requests.post(url, json=payload, headers=headers)
        response.raise_for_status()
        print("Query sent. Waiting for bot response...")
        
        # Poll for response (simplification)
        # In real test, we would subscribe to WebSocket or poll message history
        time.sleep(5) 
        
        history_url = f"{BASE_URL}/rooms/{room_id}/channels/1/messages"
        history_resp = requests.get(history_url, headers=headers)
        messages = history_resp.json().get("content", [])
        
        for msg in messages:
            sender = msg.get("sender", {}).get("username")
            content = msg.get("content")
            if sender == "ai-bot" or sender == "bot":
                print(f"Bot Response: {content}")
                if "platform" in content.lower():
                    print("SUCCESS: RAG verification passed!")
                    return True
        
        print("WARNING: Bot response not found or seemingly incorrect.")
        return False
        
    except Exception as e:
        print(f"RAG Test failed: {e}")
        return False

if __name__ == "__main__":
    token = login()
    if token:
        file_id = upload_file(token)
        if file_id:
            # Wait for ingestion (mock wait)
            print("Waiting for file ingestion...")
            time.sleep(3)
            test_rag_query(token, ROOM_ID)
