# Gemini Bank Account Opening API

## Overview

This project is a microservice solution designed to modernize the account opening system for Gemini Bank. The solution is built using Java 21, Spring Boot, and adheres to Clean Code and SOLID principles. It provides a seamless customer experience by enabling real-time validation, pausing and resuming requests, and ensuring all validations occur during the onboarding phase.

The microservice is designed to handle customer registration, validation, and request management while providing APIs for frontend integration. The solution is containerized for easy deployment and includes comprehensive documentation, including OpenAPI 3.1.0 specifications and a Postman collection.

### Features:
- Start, pause, resume, and submit account opening requests.
- Real-time validation of customer fields.
####  Technical 
- **Java 21** and **Spring Boot 3.3.x** for the backend services.
- **Maven** for dependency management and build lifecycle.
- **Docker** for containerization.
- **Flyway** for database migrations.
- **PostgreSQL Database** used for database.
- **JUnit 5**, **Mockito**, **AssertJ** and **Testcontainer** for unit testing.
- **Swagger** for API documentation.
- **Logback** a reliable and flexible logging system for capturing and managing logs, which is essential for debugging, monitoring, and auditing applications.
- **OpenAPI 3.1.0** API documentation using Swagger.
- 
### Requirements:
- **Java 21**
- **Maven**
- **Docker** for containerization
- **Spring Boot 3.3.*** and associated dependencies.

### Usage Guidelines:
- Use the `/api/customers/start` endpoint to initiate a new account opening request.
- Use the `/api/customers/{requestId}/pause` endpoint to pause a request.
- Use the `/api/customers/{requestId}/resume` endpoint to resume a paused request.
- Use the `/api/customers/{requestId}` endpoint to get a customer by request id.
- Ensure all mandatory fields are provided when starting a request.

## API Endpoints

### 1. **Start a New Account Opening Request**

#### `POST /api/customers/start`

Initiates a new account opening request and returns a unique request ID.

**Request Body:**
```json
{
  "name": "John Doe",
  "address": {
    "street": "Main Street",
    "city": "Amsterdam",
    "postalCode": "1011AB",
    "country": "Netherlands"
  },
  "dateOfBirth": "1990-01-01"
}
```

#### `PUT /api/customers/{requestId}/pause`
```
v1/api/customers/{{requestId}}pause
```
Pause customer registration by request id.

#### `PUT /api/customers/{requestId}/resume`
Resume customer registration by request id.
**Request Body:**
```json
{
  "name": "John Doe",
  "address": {
    "streetName": "Main Street",
    "city": "Amsterdam",
    "postalCode": "1011 AB",
    "houseNumber": "9"
  },
  "dateOfBirth": "1990-01-01",
  "idDocument": "AB123456",
  "accountType": "SAVINGS",
  "startingBalance": 1000,
  "monthlySalary": 3000,
  "interestedInOtherProducts": true,
  "email": "john.doe@example.com"
}
```
### `GET /api/customers/{requestId}`
```
v1/api/customers/{{requestId}}
```
Get Customer by request id


## Running the Application

### 1. Running Locally

To run the application locally, make sure you have the following installed:
- Java 21
- Maven
- Docker

Once you've cloned the repository, you can build the application with:

```bash
./mvnw clean install
```
To build without running test use
```bash
./mvnw clean install -Dmaven.test.skip=true
```

To run the application use:
```bash
./mvnw spring-boot:run
```

This will start the application on `http://localhost:8080`.

### 2. Building and Running with Docker

You can also run the application in Docker containers.

#### Build and Run the Docker image:
```bash
 docker compose up --build

```
This will run the application in a Docker container and map port 8080 on your host machine to port 8080 on the container.
#### To stop and remove containers, networks, and volumes
```bash
 docker compose down

```
#### Swagger API Documentation

You can access the Swagger UI to interact with the API at:

```bash
http://localhost:8080/
```

## Testing

### Running Unit Tests

To run unit tests, use Maven:

```bash
./mvnw test
```

### Running with Maven Build

To build the project and run the tests, use:

```bash
./mvnw clean install
```
This will compile the code, run the tests, and package the application into a JAR file.

## Postman

Collection file can be found here [GeminiBank.postman_collection.json](GeminiBank.postman_collection.json)