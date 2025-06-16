# Media Sharing Spring

Media Sharing Spring is a Java-based project built with Spring Boot that enables sharing of media content (pictures and videos) via a REST API.

## Installation

1. Clone the repository: ` https://github.com/Kruzk02/Media-Sharing-Spring.git `.
2. Navigate to the project directory: `cd Media-Sharing-Spring `.
3. Start the services using Docker Compose: `docker compose up -d`
4. Access the API at <http://localhost:8080>.
   
## Usage

Once the application is running, you can use tools like Postman or curl to interact with the API endpoints. Here's an example:
1. **Create a new user**:
  Send a POST request to `/api/users/register` with the following JSON payload:
   ```json
      {
      "username": "myname",
      "email": "myemail@example.com"
      "password": "password123",
      }
   ```
2. **Authenticate the user**:
 Send a POST request to /api/users/login with the following JSON payload to receive a JWT token:
   ```json
      {
      "username": "myname",
      "password": "password123"
      }
   ```
3. Use the generated JWT token to access protected endpoints.

# Configuration

- Redis configuration: Configure redis connection properties in `KafkaConfig.java`.
- Kafka configuration: Configure kafka connection properties in `RedisConnectionConfig.java`.
- Security configuration: Customize security settings in `SecurityConfig.java`.

# Environment Variables 
Copy the `.example.env` to `.env` and fill in the required values:
```bash
cp .example.env .env
```
