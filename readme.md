# Demo

<table>
  <tr>
    <td><img src="screenshot/login.jpg" alt="Login page" width="250"/></td>
    <td><img src="screenshot/register.jpg" alt="Register page" width="250"/></td>
    <td><img src="screenshot/empty-wifi.jpg" alt="Another page" width="250"/></td>
  </tr>
  <tr>
    <td><img src="screenshot/wifis.jpg" alt="Login page" width="250"/></td>
    <td><img src="screenshot/filter.jpg" alt="Register page" width="250"/></td>
    <td><img src="screenshot/distance-detector.jpg" alt="Another page" width="250"/></td>
  </tr>
    <tr>
    <td><img src="screenshot/profile.jpg" alt="Login page" width="250"/></td>
    <td><img src="screenshot/setting.jpg" alt="Another page" width="250"/></td>
    <td><img src="screenshot/heatmap.jpg" alt="Register page" width="250"/></td>
  </tr>
</table>

# WiFi Signal Strength

- [Navigation](#navigation)
  - [Features](#features)
  - [Usage](#usage)
  - [Screenshots](#screenshots)
  - [Installation](#prerequisites)

## Features:

- Scan WiFi networks and display their signal strength
- Emit beeping sounds to indicate signal strength
- Filter WiFi networks by signal strength and other parameters
- Display a heat map of WiFi networks in your area
- Allow users to customize the app settings and preferences
- User authentication and authorization features
- Record WiFi network data in the app's database and display it in user history
- Backend server written in Node.js using Postgresql
- Frontend admin panel for managing WiFi networks, users, and statistics

## Usage:

1. Open the app and allow it to access your device location
2. The app will start scanning for nearby WiFi networks
3. As you move around, the app will emit beeping sounds to indicate the signal strength of the WiFi networks in your area
4. Use the filters to narrow down the results and view a heat map of WiFi networks
5. Customize the app settings and preferences in the user settings menu
6. View your scan history to see previously scanned WiFi networks

## Screenshots:

## Getting Started

### Prerequisites

- Android Studio SDK
- [Server](https://github.com/isaacshafaei/Wireless-Signal-Heatmap-Backend)

### Installing

- Get the latest snapshot

```bash
git clone https://github.com/isaacshafaei/Wireless-Signal-Heatmap-Frontend.git
```

- Change directory

```bash
cd wifi-signal-strength-android
```

- Wait for the Gradle build to complete and the project to fully load in Android Studio
- Change the Base_URL in the config folder of the backend server to your own
- Launch the application

_Backend - https://github.com/isaacshafaei/Wireless-Signal-Heatmap-Backend_

**Important Note**: If you want to run this application on your phone without setting up localhost and PostgreSQL locally, you can deploy the backend on Render.com and connect the frontend to the deployed backend.
