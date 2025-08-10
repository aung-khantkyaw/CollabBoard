# CollabBoard - Distributed Whiteboard with Group Chat

A Java RMI-based collaborative whiteboard application with real-time group chat, file sharing, and audio communication capabilities.

## 🌍 Multi-Device Collaboration Ready!

**✅ Connect from multiple devices simultaneously**
- Windows, Mac, and Linux support
- Real-time synchronization across all devices  
- Easy network deployment
- Just share the server IP - users connect instantly!

## Features

### 🎨 Whiteboard
- Real-time collaborative drawing
- Multiple drawing tools (pen, shapes, text)
- Color selection and stroke width adjustment
- Undo/Redo functionality
- Save/Load whiteboard sessions
- Clear whiteboard

### 💬 Group Chat
- Real-time text messaging
- User presence indicators
- Typing notifications
- Message history
- System notifications

### 📁 File Sharing
- Upload and share files
- Support for multiple file types
- File download functionality
- File metadata display

### 🔊 Audio Communication
- Voice chat functionality
- Audio session management
- Mute/unmute controls
- Multiple user audio support

## Architecture

The project follows a client-server architecture using Java RMI:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client 1      │    │   RMI Server    │    │   Client 2      │
│                 │    │                 │    │                 │
│ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │
│ │Whiteboard UI│ │◄──►│ │Whiteboard   │ │◄──►│ │Whiteboard UI│ │
│ │             │ │    │ │Service      │ │    │ │             │ │
│ └─────────────┘ │    │ └─────────────┘ │    │ └─────────────┘ │
│ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │
│ │Chat UI      │ │◄──►│ │Chat Service │ │◄──►│ │Chat UI      │ │
│ │             │ │    │ │             │ │    │ │             │ │
│ └─────────────┘ │    │ └─────────────┘ │    │ └─────────────┘ │
│ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │
│ │File Manager │ │◄──►│ │File Service │ │◄──►│ │File Manager │ │
│ └─────────────┘ │    │ └─────────────┘ │    │ └─────────────┘ │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Requirements

- Java 11 or higher
- Gradle (for building)
- Network connectivity between server and clients

## Project Structure

```
CollabBoard/
├── src/main/java/com/collabboard/
│   ├── server/           # Server implementations
│   ├── client/           # Client implementations
│   ├── interfaces/       # RMI interfaces
│   ├── models/          # Data models
│   ├── gui/             # User interface components
│   └── utils/           # Utility classes
├── src/main/resources/   # Configuration files
├── build.gradle         # Build configuration
└── README.md           # This file
```

## Quick Start

### 1. Clone and Build

```bash
git clone <repository-url>
cd CollabBoard
gradle build
```

### 2. Start the Server

```bash
# Start RMI Registry (in separate terminal)
gradle startRegistry

# Start CollabBoard Server
gradle runServer
```

### 3. Start Clients (Multi-Device Support)

```bash
# Start client on same machine
gradle runClient

# Connect from different devices to server IP
gradle runClient -Dserver.host=192.168.100.28

# Example: Connect from multiple devices
# Device 1 (Windows): gradle runClient -Dserver.host=192.168.100.28
# Device 2 (Mac): gradle runClient -Dserver.host=192.168.100.28  
# Device 3 (Linux): gradle runClient -Dserver.host=192.168.100.28
```

**🌐 Multi-Device Collaboration:** 
- ✅ Multiple users on different computers
- ✅ Cross-platform support (Windows, Mac, Linux)
- ✅ Real-time synchronization across all devices
- ✅ Each device sees live updates from others

## Detailed Setup Instructions

### Server Setup

1. **Build the project:**
   ```bash
   gradle build
   gradle createDirectories
   ```

2. **Start RMI Registry:**
   ```bash
   # Windows
   start rmiregistry 1099
   
   # Linux/Mac
   rmiregistry 1099 &
   ```

3. **Start the server:**
   ```bash
   gradle runServer
   ```

   Or manually:
   ```bash
   java -cp build/classes/java/main com.collabboard.server.RMIServer
   ```

### Client Setup

1. **Start client application:**
   ```bash
   gradle runClient
   ```

   Or manually:
   ```bash
   java -cp build/classes/java/main com.collabboard.client.RMIClient
   ```

2. **Connect to remote server:**
   ```bash
   gradle runClient -Dserver.host=<server-ip-address>
   ```

## Configuration

Edit `src/main/resources/config.properties` to customize settings:

```properties
# Server Configuration
server.host=localhost
server.port=1099

# File Configuration
file.max.size=52428800          # 50MB
file.storage.directory=./files

# Chat Configuration
chat.max.message.length=1000
chat.history.limit=100

# Whiteboard Configuration
whiteboard.max.actions=10000
whiteboard.save.directory=./whiteboards
```

## 🌍 Multi-Device Deployment Guide

### Network Setup for Multiple Devices

#### 1. Server Machine Setup
The server should be accessible from other devices on the network:

```bash
# Edit config.properties to allow external connections
# Change from localhost to actual IP address
server.host=192.168.100.28    # Replace with your server's IP
server.port=1099
```

#### 2. Firewall Configuration
Ensure port 1099 is open on the server machine:

```bash
# Windows Firewall
netsh advfirewall firewall add rule name="RMI Registry" dir=in action=allow protocol=TCP localport=1099

# Linux (ufw)
sudo ufw allow 1099/tcp

# Mac (built-in firewall - allow Java application)
```

#### 3. Network Discovery
Find your server's IP address:

```bash
# Windows
ipconfig

# Linux/Mac  
ifconfig
# or
ip addr show
```

#### 4. Client Connection Examples

**From Windows Client:**
```bash
gradle runClient -Dserver.host=192.168.100.28
```

**From Mac Client:**
```bash
gradle runClient -Dserver.host=192.168.100.28
```

**From Linux Client:**
```bash
gradle runClient -Dserver.host=192.168.100.28
```

### Real-World Deployment Scenarios

#### 🏢 **Office/Classroom Setup**
- **Server**: Teacher's/presenter's computer (192.168.100.28)
- **Clients**: Student computers (Windows/Mac/Linux mixed)
- **Usage**: Collaborative drawing, shared presentations

#### 🏠 **Home Network**
- **Server**: Desktop computer in home office  
- **Clients**: Laptops, family computers
- **Usage**: Family collaboration, remote work sessions

#### 🌐 **Internet Deployment**
- **Server**: Cloud instance (AWS/Google Cloud/Azure)
- **Clients**: Any device with internet connection
- **Usage**: Global team collaboration

### Connection Verification

Test connectivity before running the full application:

```bash
# Test if server is reachable
telnet 192.168.100.28 1099

# Or use ping
ping 192.168.100.28
```

## Usage Guide

### Whiteboard Operations

1. **Drawing:** Select a tool and draw on the canvas
2. **Colors:** Choose from the color palette
3. **Stroke Width:** Adjust line thickness
4. **Undo/Redo:** Use toolbar buttons
5. **Clear:** Clear the entire whiteboard
6. **Save/Load:** Save sessions for later use

### Chat Operations

1. **Send Messages:** Type in the chat input and press Enter
2. **File Sharing:** Use the file upload button
3. **User List:** View online users in the sidebar
4. **Audio:** Click audio button to join voice chat

### File Sharing

1. **Upload:** Click the upload button and select files
2. **Download:** Click on shared files to download
3. **Supported Formats:** txt, pdf, doc, docx, images, audio, video, archives

## Network Configuration

### Firewall Settings

Ensure the following ports are open:

- **Port 1099:** RMI Registry
- **Dynamic Ports:** RMI communication (configurable)

### Multiple Network Interfaces

If running on a machine with multiple network interfaces, set:

```bash
java -Djava.rmi.server.hostname=<your-ip-address> ...
```

## Troubleshooting

### Common Issues

1. **"Connection refused"**
   - Ensure RMI Registry is running
   - Check server is started
   - Verify network connectivity

2. **"ClassNotFoundException"**
   - Ensure all JAR files are in classpath
   - Check Java versions match

3. **Slow performance**
   - Reduce whiteboard max actions
   - Limit file sizes
   - Check network bandwidth

### Debug Mode

Enable debug logging:

```bash
java -Djava.rmi.server.logCalls=true ...
```

## Development

### Building JAR Files

```bash
# Build server JAR
gradle serverJar

# Build client JAR
gradle clientJar

# Build both
gradle buildJars
```

### Running from JAR

```bash
# Server
java -jar build/libs/collabboard-server-1.0.0.jar

# Client
java -jar build/libs/collabboard-client-1.0.0.jar
```

### Testing

```bash
gradle test
```

## Security Considerations

- RMI communication is not encrypted by default
- Implement authentication for production use
- Validate all input data
- Limit file upload sizes
- Use firewalls to restrict access

## Performance Tips

1. **Server:**
   - Increase JVM heap size for many users
   - Monitor memory usage
   - Regular cleanup of inactive users

2. **Client:**
   - Limit drawing action frequency
   - Optimize GUI updates
   - Handle network disconnections gracefully

3. **Network:**
   - Use local network for best performance
   - Consider compression for large files
   - Monitor bandwidth usage

## License

This project is available under the MIT License.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## Support

For issues and questions:
- Check the troubleshooting section
- Review the configuration settings
- Submit an issue on the repository
