import json
import uuid
import requests

BASE_URL = "http://localhost:8080/api/v1"
EVENT_ID = 1
SEAT_ID = 100  # Pick an available seat for testing


def run_e2e_flow():
    session = requests.Session()
    print("=" * 60)
    print("🚀 STARTING FULL END-TO-END TICKETING TRANSACTION FLOW")
    print("=" * 60)

    # -------------------------------------------------------------
    # STEP 1: User Registration & Authentication
    # -------------------------------------------------------------
    unique_id = uuid.uuid4().hex[:6]
    email = f"e2e_user_{unique_id}@example.com"
    password = "Password123!"

    register_payload = {
        "email": email,
        "password": password,
        "displayName": f"E2E Tester {unique_id}",
        "role": "CUSTOMER"
    }

    print(f"\n[1/4] Registering User ({email})...")
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

    # -------------------------------------------------------------
    # STEP 2: Atomic Seat Hold (Redis Lua)
    # -------------------------------------------------------------
    hold_payload = {
        "eventId": EVENT_ID,
        "seatIds": [SEAT_ID]
    }

    print(f"\n[2/4] Holding Seat #{SEAT_ID} for Event #{EVENT_ID}...")
    resp = session.post(f"{BASE_URL}/holds", json=hold_payload, headers=headers)
    if resp.status_code not in (200, 201):
        print(f"❌ Seat hold failed ({resp.status_code}): {resp.text}")
        return

    hold_data = resp.json()
    hold_reference = hold_data.get("holdReference") or hold_data.get("id")
    print(f"✅ Seat hold acquired! Hold Reference: {hold_reference}")

    # -------------------------------------------------------------
    # STEP 3: Create Booking (Order Checkout)
    # -------------------------------------------------------------
    booking_payload = {
        "holdReference": hold_reference
    }

    print(f"\n[3/4] Creating Booking from Hold Reference...")
    resp = session.post(f"{BASE_URL}/bookings", json=booking_payload, headers=headers)
    if resp.status_code not in (200, 201):
        print(f"❌ Booking creation failed ({resp.status_code}): {resp.text}")
        return

    booking_data = resp.json()
    booking_reference = booking_data.get("bookingReference") or booking_data.get("reference")
    print(f"✅ Booking created! Booking Reference: {booking_reference}")

    # -------------------------------------------------------------
    # STEP 4: Confirm Payment & Trigger Outbox Event
    # -------------------------------------------------------------
    payment_payload = {
        "bookingReference": booking_reference,
        "paymentMethod": "CREDIT_CARD"
    }

    print(f"\n[4/4] Confirming Payment for Booking...")
    resp = session.post(f"{BASE_URL}/payments", json=payment_payload, headers=headers)
    if resp.status_code not in (200, 201):
        print(f"❌ Payment confirmation failed ({resp.status_code}): {resp.text}")
        return

    payment_data = resp.json()
    print("✅ Payment confirmed successfully!")
    print(f"   Payment Response: {json.dumps(payment_data, indent=2)}")

    print("\n" + "=" * 60)
    print("🎉 FULL E2E FLOW COMPLETED SUCCESSFULLY!")
    print("Check Spring Boot console logs to verify the Outbox publisher")
    print("relayed the message to RabbitMQ and the consumer processed it.")
    print("=" * 60)


if __name__ == "__main__":
    run_e2e_flow()