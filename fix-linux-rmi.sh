#!/bin/bash

# CollabBoard Linux Fix Script
# This script fixes the RMI hostname issue on Linux systems

echo "=== CollabBoard Linux RMI Fix ==="
echo ""

# Get the target IP address
TARGET_IP="192.168.100.5"

if [ $# -eq 1 ]; then
    TARGET_IP=$1
fi

echo "Target server IP: $TARGET_IP"
echo ""

# Step 1: Fix /etc/hosts to prevent 127.0.1.1 issues
echo "1. Checking /etc/hosts configuration..."
HOSTNAME=$(hostname)
echo "   Current hostname: $HOSTNAME"

# Check if hostname maps to 127.0.1.1
HOSTS_CHECK=$(grep "127.0.1.1.*$HOSTNAME" /etc/hosts 2>/dev/null)
if [ ! -z "$HOSTS_CHECK" ]; then
    echo "   WARNING: Found hostname mapping to 127.0.1.1 in /etc/hosts"
    echo "   This causes RMI to advertise wrong IP address"
    echo ""
    echo "   To fix this, you can either:"
    echo "   A) Comment out the 127.0.1.1 line in /etc/hosts:"
    echo "      sudo sed -i 's/^127.0.1.1/#127.0.1.1/' /etc/hosts"
    echo ""
    echo "   B) Or always specify IP when starting server:"
    echo "      ./start-server-linux.sh $TARGET_IP"
    echo ""
else
    echo "   ✅ /etc/hosts looks good"
fi

# Step 2: Check network configuration
echo "2. Network configuration check..."
echo "   Available network interfaces:"
ip addr show | grep -E "inet.*192\.168|inet.*10\.|inet.*172\." | awk '{print "   " $2 " (" $NF ")"}'

# Check if target IP is available
TARGET_AVAILABLE=$(ip addr show | grep "inet $TARGET_IP")
if [ ! -z "$TARGET_AVAILABLE" ]; then
    echo "   ✅ Target IP $TARGET_IP is available on this machine"
else
    echo "   ⚠️  Target IP $TARGET_IP not found on this machine"
    echo "       Make sure you're using the correct IP address"
fi

# Step 3: Compile the project
echo ""
echo "3. Compiling project..."
if [ ! -d "bin" ]; then
    mkdir -p bin
fi

# Find and compile Java files
find src/main/java -name "*.java" -print0 | xargs -0 javac -d bin -cp src/main/java 2>/dev/null

if [ $? -eq 0 ]; then
    echo "   ✅ Compilation successful"
else
    echo "   ❌ Compilation failed - trying alternative method..."
    # Alternative compilation
    javac -d bin -cp src/main/java src/main/java/com/collabboard/server/*.java
    javac -d bin -cp src/main/java src/main/java/com/collabboard/client/*.java
    javac -d bin -cp src/main/java src/main/java/com/collabboard/models/*.java
    javac -d bin -cp src/main/java src/main/java/com/collabboard/interfaces/*.java
    javac -d bin -cp src/main/java src/main/java/com/collabboard/gui/*.java
    javac -d bin -cp src/main/java src/main/java/com/collabboard/utils/*.java
fi

# Step 4: Test RMI hostname setting
echo ""
echo "4. Testing RMI hostname configuration..."

# Create test script
cat > test-hostname.java << 'EOF'
import java.net.*;

public class TestHostname {
    public static void main(String[] args) {
        try {
            System.out.println("=== Hostname Detection Test ===");
            
            // Test 1: InetAddress.getLocalHost()
            InetAddress localHost = InetAddress.getLocalHost();
            System.out.println("InetAddress.getLocalHost(): " + localHost.getHostAddress());
            
            // Test 2: Network interfaces
            System.out.println("\nNetwork interfaces:");
            NetworkInterface.getNetworkInterfaces().asIterator().forEachRemaining(ni -> {
                try {
                    if (!ni.isLoopback() && ni.isUp()) {
                        ni.getInetAddresses().asIterator().forEachRemaining(addr -> {
                            if (!addr.isLoopbackAddress() && !addr.isLinkLocalAddress() && 
                                addr instanceof Inet4Address) {
                                System.out.println("  " + ni.getName() + ": " + addr.getHostAddress());
                            }
                        });
                    }
                } catch (Exception e) {
                    // Ignore
                }
            });
            
            // Test 3: With system property
            if (args.length > 0) {
                System.setProperty("java.rmi.server.hostname", args[0]);
                System.out.println("\nRMI hostname set to: " + args[0]);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
EOF

javac test-hostname.java 2>/dev/null
if [ $? -eq 0 ]; then
    echo "   Running hostname detection test..."
    java TestHostname $TARGET_IP
    rm -f test-hostname.java TestHostname.class
else
    echo "   Could not compile test - skipping hostname test"
fi

# Step 5: Provide run commands
echo ""
echo "5. Ready to run! Use these commands:"
echo ""
echo "   Start server (recommended):"
echo "   ./start-server-linux.sh $TARGET_IP"
echo ""
echo "   Or manually:"
echo "   java -Djava.rmi.server.hostname=$TARGET_IP -cp bin com.collabboard.server.RMIServer $TARGET_IP"
echo ""
echo "   Start client:"
echo "   java -cp bin com.collabboard.client.RMIClient $TARGET_IP"
echo ""

# Step 6: Make scripts executable
chmod +x start-server-linux.sh 2>/dev/null
chmod +x run-linux.sh 2>/dev/null

echo "6. Scripts made executable"
echo ""
echo "=== Fix completed! ==="
echo ""
echo "If you still have connection issues:"
echo "1. Check firewall: sudo ufw allow 1099"
echo "2. Verify server starts with correct IP in the console output"
echo "3. Test connection: telnet $TARGET_IP 1099"
