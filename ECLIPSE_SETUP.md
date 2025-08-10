# 🌟 Eclipse IDE Setup Guide for CollabBoard

## 📋 Prerequisites

1. **Eclipse IDE** (any recent version)
2. **Java JDK 11 or higher** installed
3. **CollabBoard project files** (which you already have!)

## 🚀 Import Project into Eclipse

### Step 1: Open Eclipse
1. Launch Eclipse IDE
2. Choose/create a workspace (e.g., `D:\workspace`)

### Step 2: Import the Project
1. **File** → **Import...**
2. Select **General** → **Existing Projects into Workspace**
3. Click **Next**
4. **Browse** to your project folder: `D:\Project\CollabBoard`
5. Make sure **"CollabBoard"** is checked
6. Click **Finish**

### Step 3: Configure Java Build Path (if needed)
1. Right-click project → **Properties**
2. Go to **Java Build Path**
3. **Libraries** tab → Make sure you have **JRE System Library [JavaSE-11]**
4. If not, click **Add Library...** → **JRE System Library** → **Next** → Choose **JavaSE-11**

## ▶️ Running the Application

### Option 1: Using Launch Configurations (Recommended)

I've created ready-to-use launch configurations for you:

#### Start the Server:
1. **Run** → **Run Configurations...**
2. Select **Java Application** → **"CollabBoard Server"**
3. Click **Run**

#### Start the Client:
1. **Run** → **Run Configurations...**
2. Select **Java Application** → **"CollabBoard Client"**
3. Click **Run**

#### For Multi-Device Testing:
1. Use **"CollabBoard Client (Remote)"** configuration
2. Edit the **Program Arguments** to change IP address

### Option 2: Manual Run
1. Navigate to `src/main/java/com/collabboard/server/RMIServer.java`
2. Right-click → **Run As** → **Java Application**
3. Then navigate to `src/main/java/com/collabboard/client/RMIClient.java`
4. Right-click → **Run As** → **Java Application**

## 🔧 Project Structure in Eclipse

```
CollabBoard/
├── src/main/java/
│   └── com.collabboard/
│       ├── server/          # Server implementations
│       ├── client/          # Client implementation  
│       ├── interfaces/      # RMI service interfaces
│       ├── models/          # Data transfer objects
│       ├── gui/             # Swing GUI components
│       └── utils/           # Utility classes
├── src/main/resources/
│   └── config.properties    # Configuration file
├── files/                   # File storage (auto-created)
├── whiteboards/            # Saved whiteboards (auto-created)
└── bin/                    # Compiled classes (Eclipse managed)
```

## 🎯 Testing in Eclipse

### Single Machine Testing:
1. **Run** "CollabBoard Server"
2. **Run** "CollabBoard Client" 
3. **Run** another "CollabBoard Client" instance
4. Test drawing, chat, and file sharing

### Multi-Device Testing:
1. **Server**: Run "CollabBoard Server" on one computer
2. **Find Server IP**: 
   - Windows: `ipconfig`
   - Mac/Linux: `ifconfig`
3. **Client**: Edit "CollabBoard Client (Remote)" launch config
   - **Run Configurations** → **Arguments** tab
   - Change **Program Arguments** to server IP (e.g., `192.168.100.28`)
4. **Run** the remote client configuration

## 🐛 Troubleshooting

### "ClassNotFoundException" or Build Errors:
1. **Project** → **Clean...** → Select CollabBoard → **Clean**
2. **Project** → **Refresh** (F5)
3. Check **Java Build Path** has correct JRE

### "Port 1099 already in use":
1. **Stop** any running server instances in Eclipse
2. **Window** → **Show View** → **Console**
3. Click red **Terminate** button for any running processes
4. Restart the server

### GUI doesn't appear:
1. Make sure you're running the **Client**, not just the Server
2. Check **Console** for error messages
3. Verify server is running first

### Files not saving/loading:
1. Check **Console** for permission errors
2. Make sure Eclipse has write permissions to project folder
3. The `files/` and `whiteboards/` directories will be created automatically

## 🎨 Development Tips

### Debugging:
1. Set **breakpoints** by clicking left margin of code
2. **Right-click** → **Debug As** → **Java Application**
3. Use **Debug perspective** for stepping through code

### Code Navigation:
- **Ctrl+Click** on any class/method to jump to definition
- **Ctrl+Shift+R** to open any file quickly
- **Ctrl+O** to see outline of current class

### Viewing Multiple Clients:
1. **Window** → **New Window** to open second Eclipse window
2. Run different client instances in each window
3. Arrange windows side-by-side for testing

## 🌍 Multi-Device Deployment

### For Classroom/Office Use:
1. **Teacher/Presenter Computer**: Run "CollabBoard Server"
2. **Student/Participant Computers**: 
   - Import same project in their Eclipse
   - Use "CollabBoard Client (Remote)" with teacher's IP
   - Everyone can collaborate in real-time!

### Configuration Files:
- **src/main/resources/config.properties**: Modify server settings
- **Launch Configurations**: Edit for different server IPs

## ✅ You're All Set!

Your CollabBoard project is now ready to run in Eclipse with:
- ✅ **Pre-configured Eclipse project files**
- ✅ **Ready-to-use launch configurations**
- ✅ **Proper Java build path setup**
- ✅ **Multi-device testing support**
- ✅ **Professional IDE development environment**

Just import the project and start collaborating! 🎉
