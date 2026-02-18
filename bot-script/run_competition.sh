#!/bin/bash

# Array to store bot PIDs
BOT_PIDS=()

# Function to cleanup background processes
cleanup() {
    echo ""
    echo "Stopping all bots..."
    for pid in "${BOT_PIDS[@]}"; do
        if kill -0 "$pid" 2>/dev/null; then
            echo "Stopping bot PID $pid..."
            kill "$pid" 2>/dev/null
        fi
    done
    # Wait a bit for graceful shutdown
    sleep 1
    # Force kill if still running
    for pid in "${BOT_PIDS[@]}"; do
        if kill -0 "$pid" 2>/dev/null; then
            echo "Force killing bot PID $pid..."
            kill -9 "$pid" 2>/dev/null
        fi
    done
    echo "All bots stopped."
    exit 0
}

# Trap Ctrl+C and cleanup
trap cleanup SIGINT SIGTERM

echo "Starting PixelWar Competition..."
echo "Starting 4 bots in 5 seconds..."
echo "Press Ctrl+C to stop all bots"
sleep 5

# Start 4 bots with different IDs, colors, and start positions
# Red: Top-Left (0,0) -> Cyber Red
python3 simple_bot.py "bot-red" "#FF0055" 0 0 &
BOT_PIDS+=($!)

# Blue: Top-Right (49,0) -> Electric Cyan
python3 simple_bot.py "bot-blue" "#00F0FF" 49 0 &
BOT_PIDS+=($!)

# Green: Bottom-Left (0,49) -> Neon Lime
python3 simple_bot.py "bot-green" "#39FF14" 0 49 &
BOT_PIDS+=($!)

# Yellow: Bottom-Right (49,49) -> Electric Purple
python3 simple_bot.py "bot-purple" "#CC00FF" 49 49 &
BOT_PIDS+=($!)

echo "Bots started! PIDs: ${BOT_PIDS[*]}"
echo "Press Ctrl+C to stop all"

# Wait for all background processes
wait

