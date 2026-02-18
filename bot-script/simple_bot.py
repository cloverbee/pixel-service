#!/usr/bin/env python3
import requests
import random
import time
import sys
import signal

API_URL = "http://localhost:8080/api"
USER_ID = sys.argv[1] if len(sys.argv) > 1 else "test-bot-1"
MY_COLOR = sys.argv[2] if len(sys.argv) > 2 else "#FF0000"
START_X = int(sys.argv[3]) if len(sys.argv) > 3 else -1
START_Y = int(sys.argv[4]) if len(sys.argv) > 4 else -1

# Global flag for graceful shutdown
shutdown_requested = False


def signal_handler(sig, frame):
    """Handle Ctrl+C gracefully."""
    global shutdown_requested
    print(f"\n\nBot {USER_ID} received shutdown signal. Stopping gracefully...")
    shutdown_requested = True


# Register signal handler
signal.signal(signal.SIGINT, signal_handler)
signal.signal(signal.SIGTERM, signal_handler)


def get_game_status():
    """Check if the game is active."""
    try:
        response = requests.get(f"{API_URL}/game/status")
        return response.json()
    except Exception as e:
        print(f"Failed to get game status: {e}")
        return None


def paint_pixel(x, y):
    """Attempt to paint a pixel."""
    try:
        response = requests.post(
            f"{API_URL}/pixels",
            json={"x": x, "y": y, "color": MY_COLOR, "userId": USER_ID},
            timeout=1,
        )

        if response.status_code == 202:
            return True
        elif response.status_code == 429:
            # Rate limited - slow down
            time.sleep(0.1)
            return False
        elif response.status_code == 403:
            # Game not active
            return False
        else:
            print(f"Error: {response.status_code} - {response.text}")
            return False
    except Exception as e:
        print(f"Request failed: {e}")
        return False


def main():
    global shutdown_requested
    print(f"Bot starting for team: {USER_ID}")
    print(f"Color: {MY_COLOR}")

    # Wait for game to start
    print("Waiting for game to start...")
    while not shutdown_requested:
        status = get_game_status()
        if status and status.get("state") == "ACTIVE":
            print(
                f"Game started! Ending in {status.get('remainingSeconds')} seconds"
            )
            break
        if shutdown_requested:
            return
        time.sleep(2)

    # Main game loop
    request_count = 0
    success_count = 0

    while not shutdown_requested:
        # Check if game is still active
        if request_count % 100 == 0:  # Check every 100 requests
            status = get_game_status()
            if status and status.get("state") != "ACTIVE":
                print(f"Game ended! State: {status.get('state')}")
                break
            if shutdown_requested:
                break
            print(
                f"Painted {success_count} pixels. {status.get('remainingSeconds')}s remaining"
            )

        # Generate coordinates
        if START_X != -1 and START_Y != -1:
            # Bias towards start position using Gaussian distribution
            # Sigma=20 gives good spread across 50x50 board
            gx = int(random.gauss(START_X, 15))
            gy = int(random.gauss(START_Y, 15))
            
            # Clamp to board boundaries
            x = max(0, min(49, gx))
            y = max(0, min(49, gy))
        else:
            # Pure random
            x = random.randint(0, 49)
            y = random.randint(0, 49)

        if paint_pixel(x, y):
            success_count += 1

        request_count += 1
        time.sleep(0.01)  # 100 requests per second

    print(f"\nBot finished!")
    print(f"Total requests: {request_count}")
    print(f"Successful paints: {success_count}")

    # Show final results
    if not shutdown_requested:
        try:
            response = requests.get(f"{API_URL}/game/results")
            results = response.json()
            print(f"\nWinner: {results.get('winner')}")
            print("Final standings:")
            for team in results.get("results", []):
                print(
                    f"  {team['teamId']}: {team['territoryPixels']} pixels (total paints: {team['totalPaints']})"
                )
        except Exception as e:
            print(f"Could not fetch results: {e}")


if __name__ == "__main__":
    main()

