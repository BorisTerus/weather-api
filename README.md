## Prerequisites

Java 21, Docker

## Setup & Run

1. Install dependencies:
    ```bash
    mvn clean install -DskipTests
    ```
2. Start redis container:

   ```bash
   docker compose up -d
   ```

3. Run application:
    ```bash
   mvn spring-boot:run
     ```

## Run Tests

In terminal:
```
mvn test
 ```