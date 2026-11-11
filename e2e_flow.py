import json
import random
import uuid
import requests

BASE_URL = "http://localhost:8080/api/v1"
EVENT_ID = 1


def run_e2e_flow():
    session = requests.Session()
    seat_id = random.randint(101, 1500)

    print("=" * 60)
    print("🚀 STARTING FULL END-TO-END TICKETING TRANSACTION FLOW")
    print("=" * 60)

    # 1. Register User & Obtain JWT
    unique_id = uuid.uuid4().hex[:6]
    email = f"e2e_user_{unique_id}@example.com"
    password = "Password123!"

    register_payload = {
        "email": email,
        "password": password,
        "displayName": f"E2E Tester {unique_id}",
        "role": "CUSTOMER"
    }

    print(f"\n[1/3] Registering User ({email})...")
    resp = session.post(f"{BASE_URL}/auth/register", json=register_payload)
    if resp.status_code not in (200, 201):
        print(f"❌ Registration failed ({resp.status_code}): {resp.text}")
        return

    data = resp.json()
    token = data.get("accessToken") or data.get("token") or data.get("jwt")
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    print("✅ Authentication successful. JWT token obtained.")

    # 2. Atomic Seat Hold (Redis Lua)
    hold_payload = {
        "eventId": EVENT_ID,
        "seatIds": [seat_id]
    }

    print(f"\n[2/3] Holding Seat #{seat_id} for Event #{EVENT_ID}...")
    resp = session.post(f"{BASE_URL}/holds", json=hold_payload, headers=headers)
    if resp.status_code not in (200, 201):
        print(f"❌ Seat hold failed ({resp.status_code}): {resp.text}")
        return

    hold_data = resp.json()
    hold_reference = hold_data.get("holdReference") or hold_data.get("id")
    print(f"✅ Seat hold acquired! Hold Reference: {hold_reference}")

    # 3. Create Booking & Process Payment
    booking_payload = {
        "holdReference": hold_reference,
        "paymentMethod": "CREDIT_CARD"
    }

    print(f"\n[3/3] Creating Booking & Confirming Payment...")
    resp = session.post(f"{BASE_URL}/bookings", json=booking_payload, headers=headers)
    if resp.status_code not in (200, 201):
        print(f"❌ Booking failed ({resp.status_code}): {resp.text}")
        return

    booking_data = resp.json()
    print("✅ Booking created & payment processed successfully!")
    print(f"   Response Payload: {json.dumps(booking_data, indent=2)}")

    print("\n" + "=" * 60)
    print("🎉 FULL E2E FLOW COMPLETED SUCCESSFULLY!")
    print("=" * 60)


if __name__ == "__main__":
    run_e2e_flow()