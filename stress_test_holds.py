import asyncio
import json
import time
import uuid
import aiohttp

BASE_URL = "http://localhost:8080/api/v1"
CONCURRENT_USERS = 100
TARGET_EVENT_ID = 1
TARGET_SEAT_ID = 42  # All 100 users compete for this seat


async def register_user(
        session: aiohttp.ClientSession, semaphore: asyncio.Semaphore, user_index: int
) -> str:
    """Registers a virtual user with throttled concurrency to avoid BCrypt CPU spikes."""
    async with semaphore:
        unique_id = uuid.uuid4().hex[:8]
        email = f"user_{user_index}_{unique_id}@example.com"
        password = "Password123!"

        payload = {
            "email": email,
            "password": password,
            "displayName": f"User {user_index} Tester",
            "role": "CUSTOMER"
        }
        headers = {"Content-Type": "application/json"}

        async with session.post(f"{BASE_URL}/auth/register", json=payload, headers=headers) as resp:
            body = await resp.text()
            if resp.status not in (200, 201):
                raise RuntimeError(
                    f"Registration failed for User #{user_index} (HTTP {resp.status}): {body}"
                )

            data = json.loads(body)
            # Retrieve token regardless of whether the DTO field is named accessToken, jwt, or token
            token = data.get("accessToken") or data.get("token") or data.get("jwt")
            if not token:
                raise KeyError(
                    f"Token field not found in response JSON keys: {list(data.keys())}. Body: {body}"
                )
            return token


async def attempt_seat_hold(
        session: aiohttp.ClientSession, token: str, barrier: asyncio.Barrier, user_id: int
) -> dict:
    """Synchronizes at barrier, then fires hold requests simultaneously."""
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    payload = {
        "eventId": TARGET_EVENT_ID,
        "seatIds": [TARGET_SEAT_ID]
    }

    # Barrier ensures all 100 requests release at the exact same millisecond
    await barrier.wait()

    start_time = time.perf_counter()
    try:
        async with session.post(f"{BASE_URL}/holds", json=payload, headers=headers) as resp:
            latency_ms = (time.perf_counter() - start_time) * 1000
            return {
                "user_id": user_id,
                "status": resp.status,
                "latency_ms": latency_ms,
                "body": await resp.text()
            }
    except Exception as exc:
        latency_ms = (time.perf_counter() - start_time) * 1000
        return {
            "user_id": user_id,
            "status": 500,
            "latency_ms": latency_ms,
            "error": str(exc)
        }


async def run_stress_test():
    print(f"--- Preparing Stress Test: {CONCURRENT_USERS} Concurrent Users ---")

    # Throttle user registration to 5 parallel requests to protect CPU during BCrypt hashing
    registration_semaphore = asyncio.Semaphore(5)
    connector = aiohttp.TCPConnector(limit=CONCURRENT_USERS)

    async with aiohttp.ClientSession(connector=connector) as session:
        print("Authenticating 100 virtual users (throttled setup)...")
        try:
            tokens = await asyncio.gather(
                *[register_user(session, registration_semaphore, i) for i in range(CONCURRENT_USERS)]
            )
        except Exception as e:
            print(f"\n[ERROR] Authentication setup failed: {e}")
            return

        print(f"Successfully authenticated {len(tokens)} virtual users.\n")

        # Barrier releases all 100 users at once
        barrier = asyncio.Barrier(CONCURRENT_USERS)

        print(f"Firing {CONCURRENT_USERS} simultaneous hold requests for Event #{TARGET_EVENT_ID}, Seat #{TARGET_SEAT_ID}...")
        start_time = time.perf_counter()

        tasks = [
            attempt_seat_hold(session, tokens[i], barrier, i)
            for i in range(CONCURRENT_USERS)
        ]
        results = await asyncio.gather(*tasks)

        total_time_ms = (time.perf_counter() - start_time) * 1000

    # Output Analysis
    successes = [r for r in results if r["status"] in (200, 201)]
    failures = [r for r in results if r["status"] not in (200, 201)]
    latencies = [r["latency_ms"] for r in results]

    avg_latency = sum(latencies) / len(latencies)
    max_latency = max(latencies)
    min_latency = min(latencies)

    print("\n================ TEST RESULTS ================")
    print(f"Total Requests Executed  : {len(results)}")
    print(f"Total Execution Window   : {total_time_ms:.2f} ms")
    print(f"Successful Holds (201)   : {len(successes)}")
    print(f"Failed / Rejected Holds  : {len(failures)}")
    print(f"Min Latency              : {min_latency:.2f} ms")
    print(f"Max Latency              : {max_latency:.2f} ms")
    print(f"Average Latency          : {avg_latency:.2f} ms")
    print("==============================================")

    if len(successes) == 1:
        print("\nPASSED: Redis Lua script successfully prevented race conditions! Exactly 1 hold granted.")
    elif len(successes) == 0:
        print("\nFAILED: 0 holds succeeded. Verify Event #1 and Seat #42 exist in database seed data.")
    else:
        print(f"\nCRITICAL FAILURE: Race condition detected! {len(successes)} holds granted for the same seat.")


if __name__ == "__main__":
    asyncio.run(run_stress_test())