# Gemini Bank Account Opening API

## Overview

## Overview
The **Gemini Bank Account Opening API** allows customers to initiate, pause, resume, and submit their account opening requests. This API provides real-time validation of customer information during the onboarding process, ensuring all mandatory details are submitted before the request proceeds.

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
- Use the `/api/customers/validate` endpoint used by frontend application to do field validation.
- Ensure all mandatory fields are provided when starting a request.

## API Endpoints

### 1. **Start a New Account Opening Request**

#### `POST /api/customers/start`

Initiates a new account opening request and returns a unique request ID.

**Request Body:**
- Requires mandatory details such as name, address, date of birth, ID document, and account type.
- **Responses**:
    - **200 OK**: Account opening request started successfully.
    - **400 Bad Request**: Invalid input.
    - **422 Unprocessable Entity**: Validation errors.
    - **500 Internal Server Error**: Server error.
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
- Pauses a customer account opening request.
    - **Parameters**: `requestId` (The unique ID of the account opening request)
    - **Responses**:
        - **200 OK**: Request paused successfully.
        - **400 Bad Request**: Invalid input.
        - **404 Not Found**: Request not found.
        - **410 Gone**: Request has expired.
        - **500 Internal Server Error**: Server error.

#### `PUT /api/customers/{requestId}/resume`
Resumes a previously paused account opening request.
- **Parameters**: `requestId` (The unique ID of the account opening request)
- **Responses**:
    - **200 OK**: Request resumed successfully.
    - **400 Bad Request**: Invalid input.
    - **404 Not Found**: Request not found.
    - **410 Gone**: Request expired.
    - **500 Internal Server Error**: Server error.
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
Retrieves the current state of a request by its request ID.
- **Parameters**: `requestId` (The unique ID of the account opening request)
- **Responses**:
    - **200 OK**: Request details retrieved successfully.
    - **404 Not Found**: Request not found.
    - **500 Internal Server Error**: Server error.

### `POST /api/customers/validate`
- Validates a specific field in the customer's information (e.g., name, address, etc.).
- **Request Body**: Contains `field` (name of the field to validate) and `value` (value to validate).
- **Responses**:
    - **200 OK**: Field is valid.
    - **400 Bad Request**: Field is invalid due to validation errors.
    - **500 Internal Server Error**: Server error.

## Running the Application

### 1. Running Locally

To run the application locally, make sure you have the following installed:
- Java 21
- Maven
- Docker

Clone the repository:
```bash
 git clone https://github.com/achavanga/gemini-account-opening-api.git
```
Navigate to the project directory:
```bash
 cd account-opening-api
```
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