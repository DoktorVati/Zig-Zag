# Zig Zag

Zig Zag is a multi-platform dual native location-based social media platform.

---

## Table of Contents

1. [Overview](#overview)
2. [iOS](#ios)
3. [Android](#android)
5. [Backend](#backend)  
   - [Dependencies Required](#dependencies-required)  
   - [How to Run](#how-to-run)  

---

## Overview

Zig Zag is a social media platform designed to connect users through location-based interactions, available on multiple platforms.

---

## Android
The Android platform of Zig Zag is built in Android Studio using Java and Extensible Markup Language (XML). 
In order to run the application, you can either 

1. Download the application directly from the Google Play Store at (https://play.google.com/store/apps/details?id=com.InhibiousStudios.zigzag).

   or
   
2. I. Download [Android Studio](https://developer.android.com/studio).                                                                                                                                             
   II.  Clone the ZigZag repository from Git.                                                                                                                                                                       
   III. Open the Android/ZigZag file inside Android Studio.

---
   
## iOS

The iOS platform of Zig Zag was built in XCode using swift and swiftUI. To run the application follow these steps:

1. Download XCode from the app store on a compatible MAC
2. Clone the github repository into a file on your system
3. Open XCode and select the ZigZag folder from the Apple folder of your cloned repository

---

## Backend

The Zig Zag backend is a Node.js application that uses a PostgreSQL database with the PostGIS extension. It is containerized, which eliminates the need to install Node.js, PostgreSQL, or PostGIS separately.

### Dependencies Required

To run the backend, ensure the following is installed:

- [Docker](https://docs.docker.com/engine/install/)

### How to Run

1. Install [Docker](https://docs.docker.com/engine/install/).
2. Clone the Zig Zag repository from Git.
3. Create a `.env` file in the `backend` directory with the following fields:
    ```env
   # PostgreSQL Configuration
   DB_NAME=
   DB_USER=
   DB_PASSWORD=
   DB_HOST=localhost
   DB_PORT=5432
   ```
4.	Navigate to the backend directory and run the following command to build and start the backend services:

    ```bash 
    docker compose up --build -d
    ```
    This command initializes both the Node.js server and the PostgreSQL database with the PostGIS extension as containers.
