#!/bin/bash

# CollabBoard Server Launcher for Linux
# This script ensures the RMI server uses the correct network IP

echo "=== CollabBoard Server (Linux) ==="

# Get the server IP address
SERVER_IP=""

if [ $# -eq 1 ]; then
    SERVER_IP=$1
    echo "Using provided IP: $SERVER_IP"
else
    echo "Auto-detecting network IP..."
    # Try to detect the main network interface IP
    SERVER_IP=$(ip route get 8.8.8.8 | sed -n '/src/{s/.*src *\([^ ]*\).*/\1/p;q}' 2>/dev/null)
    
    if [ -z "$SERVER_IP" ]; then
        # Fallback method
        SERVER_IP=$(hostname -I | awk '{print $1}' 2>/dev/null)
    fi
    
    if [ -z "$SERVER_IP" ] || [ "$SERVER_IP" = "127.0.0.1" ] || [ "$SERVER_IP" = "127.0.1.1" ]; then
        echo "Could not auto-detect network IP. Please provide it manually:"
        echo "Usage: $0 <server-ip>"
        echo "Example: $0 192.168.100.5"
        exit 1
    fi
    
    echo "Auto-detected IP: $SERVER_IP"
fi

# Validate IP format
if [[ ! $SERVER_IP =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
    echo "Invalid IP address format: $SERVER_IP"
    echo "Please provide a valid IPv4 address"
    exit 1
fi

echo "Starting RMI Server with IP: $SERVER_IP"
echo "Clients should connect to: $SERVER_IP:1099"
echo "Press Ctrl+C to stop the server"
echo ""

# Set RMI hostname and run server
export JAVA_OPTS="-Djava.rmi.server.hostname=$SERVER_IP -Djava.rmi.server.useLocalHostname=false"

# Check if compiled
if [ ! -d "bin" ]; then
    echo "Compiling project..."
    make compile || {
        echo "Compilation failed. Please run 'make compile' first."
        exit 1
    }
fi

# Run server
java $JAVA_OPTS -cp bin com.collabboard.server.RMIServer $SERVER_IP
