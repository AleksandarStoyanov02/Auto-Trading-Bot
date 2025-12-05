[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](https://www.java.com/en/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?logo=postgresql&logoColor=white)
![JDBC](https://img.shields.io/badge/JDBC-Raw%20SQL-BB4F9C?logo=oracle&logoColor=white)
[![Gradle](https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
![TA4J](https://img.shields.io/badge/TA4J-0.19-2196F3?logo=java&logoColor=white)
![React](https://img.shields.io/badge/React-20232A?logo=react&logoColor=61DAFB)
![Vite](https://img.shields.io/badge/Vite-646CFF?logo=vite&logoColor=white)

# 🤖 Auto Trading Bot Simulation Service

## Project Overview

This project is a full-stack web application designed to simulate an autonomous cryptocurrency trading bot. The goal is to develop a robust service capable of analyzing market data and executing trades in two distinct environments: a **Training Mode** (historical backtesting) and a **Trading Mode** (simulated live execution). The application tracks financial history and portfolio performance, visualized on a dynamic frontend dashboard.

**Core Goal:** Develop a web application that simulates an automated crypto trading bot.

## Technical Stack

| Component | Technology | Reasoning |
| :--- | :--- | :--- |
| **Backend** | Java 21, **Spring Boot 3** | Robust, enterprise-grade architecture. |
| **Data Access** | **Spring JDBC Template** | Required to meet the "No ORM" constraint (raw SQL). |
| **Trading Logic** | **TA4J** (Technical Analysis for Java) | Used for calculating indicators (e.g., RSI) during strategy execution. |
| **Database** | PostgreSQL (via Docker) | Reliable relational database storage. |
| **Frontend** | React, Vite, Basic CSS | Modern, fast development environment. |
| **Data Source** | **Binance REST API** | Used for fetching historical (klines) and live price data. |



## Setup & Requirements

### 1. Prerequisites

You must have the following tools installed:

* **Java Development Kit (JDK):** Version 21 or higher.
* **Docker & Docker Compose:** Required to run the PostgreSQL database.
* **Node.js & npm:** (npm >= 10.x) Required for frontend setup and execution.

### 2. Local Setup: Cloning & Secrets

1.  **Clone the Repository:**
    ```bash
    git clone git@github.com:yourusername/auto-trading-bot.git
    cd auto-trading-bot
    ```

2.  **Configure Secrets (`.env` file):**
    The project uses an external `.env` file for database credentials (as per best practice). Create a file named **`.env`** in the **root directory** and populate it with the default values used by `docker-compose.yml`:
    ```bash
    # .env
    DB_USER=trader
    DB_PASSWORD=password
    DB_NAME=trading_data
    ```

### 3. Database Setup (PostgreSQL)

This starts the database container and creates the volumes for data persistence.

```bash
# Start the PostgreSQL container in detached mode
docker-compose up -d

# Verify the container is running
docker ps
```

### 4. Execution Steps
A. **Backend Start (Spring Boot)**
Navigate to the backend/ directory:

```bash

cd backend

```
Run the application using the Gradle wrapper:

```bash

./gradlew bootRun

```
The application should start on port 8080 and connect to the database.

B. **Frontend Start (React/Vite)**
Open a new terminal tab and navigate to the frontend/ directory:

```bash

cd frontend

```

Install Node dependencies (if not done already):
```bash

npm install

```

Start the Vite development server:

```bash

npm run dev

```

The dashboard will open automatically (usually on port 5173), using the configured proxy to communicate with the backend.

# Project Documentation
For detailed architectural diagrams, design rationale, and future improvements (including the Reflection document), please refer to the dedicated project space:

[GitBook Documentation](https://aleksandar-stoyanov.gitbook.io/auto-trading-bot-documentation)
