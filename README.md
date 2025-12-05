🤖 Auto Trading Bot Simulation Service
Project Overview
This project is a full-stack web application designed to simulate an autonomous cryptocurrency trading bot. The goal is to develop a robust service capable of analyzing market data and executing trades in two distinct environments: a Training Mode (historical backtesting) and a Trading Mode (simulated live execution). The application tracks financial history and portfolio performance, visualized on a dynamic frontend dashboard.

Core Goal: Develop a web application that simulates an automated crypto trading bot.
🚀 Technical StackComponentTechnologyReasoningBackendJava 21, Spring Boot 3Robust, enterprise-grade architecture.Data AccessSpring JDBC TemplateRequired to meet the "No ORM" constraint (raw SQL).Trading LogicTA4J (Technical Analysis for Java)Used for calculating indicators (e.g., RSI) during strategy execution.DatabasePostgreSQL (via Docker)Reliable relational database storage.FrontendReact, Vite, Basic CSSModern, fast development environment.Data SourceBinance REST APIUsed for fetching historical (klines) and live price data.🛠️ Setup & Requirements1. PrerequisitesYou must have the following tools installed:Java Development Kit (JDK): Version 21 or higher.Docker & Docker Compose: Required to run the PostgreSQL database.Node.js & npm: (npm >= 10.x) Required for frontend setup and execution.2. Local Setup: Cloning & SecretsClone the Repository:Bashgit clone git@github.com:yourusername/auto-trading-bot.git
cd auto-trading-bot
Configure Secrets (.env file):The project uses an external .env file for database credentials (as per best practice). Create a file named .env in the root directory and populate it with the default values used by docker-compose.yml:Bash# .env
DB_USER=trader
DB_PASSWORD=password
DB_NAME=trading_data
3. Database Setup (PostgreSQL)This starts the database container and creates the volumes for data persistence.Bash# Start the PostgreSQL container in detached mode
docker-compose up -d

# Verify the container is running
docker ps
Manual Schema Execution (Required for Raw SQL):Since we are not using Flyway, you must execute the schema script manually once.Bash# Execute the schema creation script using psql inside the container
cat backend/src/main/resources/schema.sql | docker exec -i trading_db psql -U trader -d trading_data
4. Execution StepsA. Backend Start (Spring Boot)Navigate to the backend/ directory and run the application using the Gradle wrapper:Bashcd backend
# Build and run the Spring Boot application
./gradlew bootRun
The application should start on port 8080 and connect to the database.B. Frontend Start (React/Vite)Open a new terminal tab and start the frontend development server:Bashcd frontend
# Install Node dependencies (if not done already)
npm install
# Start the Vite development server
npm run dev
The dashboard will open automatically (usually on port 5173).🔗 Confluence DocumentationFor detailed architectural diagrams, design rationale, and future improvements, please refer to the dedicated project space:https://ecommercedev.atlassian.net/wiki/spaces/ATB/overview
