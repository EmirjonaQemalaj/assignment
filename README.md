# Home assignment

## Overview
This is a Spring Boot application designed to demonstrate a robust and scalable backend. The application includes RESTful APIs, database integration, and support for external services.

## Technologies
- Java 21
- Spring Boot
- Maven
- PostgreSQL 
- Flyway
- JUnit
- Swagger

## Prerequisites
- Java 21
- Maven
- PostgreSQL

## Prerequisites
- Java 21 installed on your machine.
- Maven installed and configured in your PATH.
- PostgreSQL database set up and running.
- A preferred IDE (e.g., IntelliJ IDEA, Eclipse, or VS Code).

## Project Structure
project-name/
├── src/main/java          # Application source code
├── src/main/resources     # Configuration files
├── src/test/java          # Unit and integration tests
├── pom.xml                # Maven configuration file
└── README.md              # Project documentation


```

## Features
- User authentication and authorization.
- RESTful APIs for CRUD operations.
- PostgreSQL database integration.
- Automatic database migrations with Flyway.
- Unit and integration testing setup.

## Running the Application

### Development
- Run the backend: `mvn spring-boot:run`

### Production
- Build the backend: `mvn clean package`

## License
This project is licensed under the MIT License. See the LICENSE file for details.

### Notes
- Make sure to configure the database in `application.properties`.
