# Tunakan 
![chat_icon.png](Assets/chat_icon.png)

Tunakan is an Android chat application developed in Java using Android Studio. The project demonstrates a real-time messaging system backed by Firebase services, including authentication and database synchronization.

## Overview 
The application allows users to register, authenticate, and exchange messages in real time. Firebase is used as the backend to manage user accounts and store chat data, enabling instant updates across devices without manual refresh logic. This repository follows a standard Android project structure and is intended for learning, experimentation, or as a base for further development of chat-based Android applications.
## Features 
- User authentication using Firebase Authentication 
- Real-time messaging with Firebase Realtime Database or Firestore 
- Simple and clean chat user interface 
- Message synchronization across devices 
- Android Studio / Gradle-based build system 

## Technologies Used 
- **Language:** Java 
- **Platform:** Android 
- **Backend:** Firebase 
  - Firebase Authentication 
  - Firebase Realtime Database 
  - Firebase Cloud Messaging 
- **Build System:** Gradle 

## Project Structure
```
Tunakan/
├── app/
│ ├── src/
│ │ ├── main/
│ │ │ ├── java/ # Application source code
│ │ │ ├── res/
│ │ │ │ ├── drawable/ # Includes chat_icon
│ │ │ │ ├── layout/ # XML UI layouts
│ │ │ │ └── values/ # Colors, styles, strings
│ │ │ └── AndroidManifest.xml
│ └── build.gradle
├── gradle/
├── build.gradle
├── settings.gradle
└── README.md
```

## Setup and Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/v1rtu3x/Tunakan.git
   ```
2. Open the project in Android Studio.
3. Connect the project to Firebase:
   - Create a Firebase project in the Firebase Console
   - Add an Android app with the correct package name
   - Download google-services.json
   - Place it in the app/ directory
4. Sync Gradle and build the project.
5. Run the application on an emulator or physical Android device.

## Notes

The project may rely on older Gradle or repository configurations. If build errors occur, updating dependencies and replacing deprecated repositories (such as jcenter()) may be required.

This project does not currently include tagged releases or production-ready configuration.