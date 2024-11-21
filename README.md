# Zig Zag

Zig Zag is a multi-platform location-based social media platform.

---

## Table of Contents

1. [Overview](#overview)
2. [iOS](#ios)
3. [Android](#android)
4. [Backend](#backend)  
   - [Dependencies Required](#dependencies-required)  
   - [How to Run](#how-to-run)  

---

## Overview

Zig Zag is a social media platform designed to connect users through location-based interactions, available on multiple platforms.

---

## iOS

_Information about the iOS platform goes here._

---

## Android

_Information about the Android platform goes here._

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
