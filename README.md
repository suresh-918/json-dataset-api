# JSON Dataset API

A REST API built using Spring Boot that allows users to insert arbitrary JSON records into datasets and query them using dynamic grouping and sorting.

## Live Deployment

- **Live API:** https://json-dataset-api-737i.onrender.com
- **Swagger UI:** https://json-dataset-api-737i.onrender.com/swagger-ui/index.html
- **GitHub Repository:** https://github.com/suresh-918/json-dataset-api

## Technology Stack

- Java 17
- Spring Boot
- Spring Data JPA
- Hibernate
- MySQL
- Maven
- Springdoc OpenAPI / Swagger UI
- Docker
- Render (application deployment)
- Aiven MySQL (database hosting)

## Features

- Insert arbitrary JSON records into a named dataset.
- Query records from a dataset.
- Dynamically group records using the `groupBy` parameter.
- Dynamically sort records using the `sortBy` parameter.
- Support ascending and descending sorting through the `order` parameter.
- Interactive API documentation using Swagger UI.
- Environment-based database configuration.

## API Endpoints

### 1. Insert a JSON Record

**HTTP Method:** `POST`

**Endpoint:**

```text
/api/dataset/{datasetName}/record
```

**Example request:**

```http
POST /api/dataset/employees/record
Content-Type: application/json
```

**Request body:**

```json
{
  "name": "Suresh",
  "department": "Engineering",
  "salary": 50000
}
```

**Description:**

Inserts a JSON record into the specified dataset. The `datasetName` path variable identifies the dataset.

The API returns a response containing the result of the insertion.

### 2. Query Dataset Records

**HTTP Method:** `GET`

**Endpoint:**

```text
/api/dataset/{datasetName}/query
```

**Query parameters:**

| Parameter | Description |
|---|---|
| `groupBy` | Field used to group records. |
| `sortBy` | Field used to sort records. |
| `order` | Sorting direction, such as `asc` or `desc`, according to the API implementation. |

**Example request:**

```http
GET /api/dataset/employees/query?groupBy=department&sortBy=salary&order=desc
```

**Description:**

Retrieves records from the specified dataset, with optional grouping and sorting based on the supplied query parameters.

Refer to Swagger UI for the available request parameters and response structures.

## Database Configuration

The application uses Aiven MySQL as its database.

**JDBC URL:**

```text
jdbc:mysql://freightfox-mysql-nagasureshchakali-6b27.f.aivencloud.com:13853/freightfoxdb?sslMode=REQUIRED
```

Database configuration is provided through environment variables:

| Environment Variable | Description |
|---|---|
| `DB_URL` | JDBC connection URL for the Aiven MySQL database. |
| `DB_USERNAME` | Aiven database username. |
| `DB_PASSWORD` | Aiven database password. |

The database URL above is configured for an SSL connection.

**Security note:** Database credentials should be configured through environment variables and must not be committed to the Git repository.

## Local Setup

### Prerequisites

- Java 17
- Maven
- Access to a MySQL database

### 1. Clone the repository

```bash
git clone https://github.com/suresh-918/json-dataset-api.git
cd json-dataset-api
```

### 2. Configure environment variables

Configure the following environment variables in your terminal or IDE run configuration:

```text
DB_URL=jdbc:mysql://freightfox-mysql-nagasureshchakali-6b27.f.aivencloud.com:13853/freightfoxdb?sslMode=REQUIRED
DB_USERNAME=<your-aiven-username>
DB_PASSWORD=<your-aiven-password>
```

Replace the username and password placeholders with your own credentials. Do not commit actual credentials to GitHub.

### 3. Build the application

```bash
mvn clean package
```

### 4. Run the application

Use the generated JAR file from the `target` directory:

```bash
java -jar target/<generated-jar-name>.jar
```

Alternatively, run the application from your IDE.

### 5. Open Swagger UI

After the application starts, open:

```text
http://localhost:8080/swagger-ui/index.html
```

## Deployment

The application is deployed on Render using Docker, with Aiven MySQL providing the database.

The deployment uses environment variables for database credentials and connection settings.

- **Production API:** https://json-dataset-api-737i.onrender.com
- **Production Swagger UI:** https://json-dataset-api-737i.onrender.com/swagger-ui/index.html

## Testing

Use Swagger UI to test the API endpoints:

1. Insert JSON records into a dataset.
2. Query the dataset without optional parameters.
3. Query records using `groupBy`.
4. Query records using `sortBy`.
5. Test ascending and descending sorting.
6. Verify that the API returns the expected responses.

## Project Repository

GitHub: https://github.com/suresh-918/json-dataset-api