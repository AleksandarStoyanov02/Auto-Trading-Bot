# 🤖 Auto Trading Bot Simulation Service

## Project Overview

This project is a full-stack web application designed to simulate an autonomous cryptocurrency trading bot. The goal is to develop a robust service capable of analyzing market data and executing trades in two distinct environments: a **Training Mode** (historical backtesting) and a **Trading Mode** (simulated live execution). The application tracks financial history and portfolio performance, visualized on a dynamic frontend dashboard.

**Core Goal:** Develop a web application that simulates an automated crypto trading bot.

---

## 🚀 Technical Stack

| Component | Technology | Reasoning |
| :--- | :--- | :--- |
| **Backend** | Java 21, **Spring Boot 3** | Robust, enterprise-grade architecture. |
| **Data Access** | **Spring JDBC Template** | Required to meet the "No ORM" constraint (raw SQL). |
| **Trading Logic** | **TA4J** (Technical Analysis for Java) | Used for calculating indicators (e.g., RSI) during strategy execution. |
| **Database** | PostgreSQL (via Docker) | Reliable relational database storage. |
| **Frontend** | React, Vite, Basic CSS | Modern, fast development environment. |
| **Data Source** | **Binance REST API** | Used for fetching historical (klines) and live price data. |

---

## 🛠️ Setup & Requirements

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

**Manual Schema Execution (Required for Raw SQL):**Since we are not using Flyway, you must execute the schema script manually once after the container is running.

Bash

Plain textANTLR4BashCC#CSSCoffeeScriptCMakeDartDjangoDockerEJSErlangGitGoGraphQLGroovyHTMLJavaJavaScriptJSONJSXKotlinLaTeXLessLuaMakefileMarkdownMATLABMarkupObjective-CPerlPHPPowerShell.propertiesProtocol BuffersPythonRRubySass (Sass)Sass (Scss)SchemeSQLShellSwiftSVGTSXTypeScriptWebAssemblyYAMLXML`   # Execute the schema creation script using psql inside the container  cat backend/src/main/resources/schema.sql | docker exec -i trading_db psql -U trader -d trading_data   `

4\. Execution Steps
-------------------

### A. Backend Start (Spring Boot)

1.  Bashcd backend
    
2.  Bash./gradlew bootRun_The application should start on port_ _**8080**_ _and connect to the database._
    

### B. Frontend Start (React/Vite)

1.  Bashcd frontend
    
2.  Bashnpm install
    
3.  Bashnpm run dev_The dashboard will open automatically (usually on port 5173), using the configured proxy to communicate with the backend._
    

🔗 Project Documentation
------------------------

For detailed architectural diagrams, design rationale, and future improvements (including the Reflection document), please refer to the dedicated project space:

[https://ecommercedev.atlassian.net/wiki/spaces/ATB/overview](https://ecommercedev.atlassian.net/wiki/spaces/ATB/overview)
