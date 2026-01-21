```
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── ntd
│   │   │           └── unsaid
│   │   │               ├── application
│   │   │               │   ├── dto
│   │   │               │   ├── listener
│   │   │               │   ├── mapper
│   │   │               │   └── service
│   │   │               ├── config
│   │   │               │   ├── CloudinaryConfig.java
│   │   │               │   └── SecurityConfig.java
│   │   │               ├── domain
│   │   │               │   ├── entity
│   │   │               │   ├── enums
│   │   │               │   ├── event
│   │   │               │   └── repository
│   │   │               ├── exception
│   │   │               │   ├── AppException.java
│   │   │               │   └── GlobalExceptionHandler.java
│   │   │               ├── infrastructure
│   │   │               │   ├── messaging
│   │   │               │   ├── search
│   │   │               │   └── storage
│   │   │               ├── presentation
│   │   │               │   ├── controller
│   │   │               │   └── websocket
│   │   │               ├── security
│   │   │               │   ├── CustomJwtDecoder.java
│   │   │               │   ├── JwtAuthenticationEntryPoint.java
│   │   │               │   └── JwtCookieAuthenticationFilter.java
│   │   │               ├── utils
│   │   │               └── UnsaidBackendApplication.java
│   │   └── resources
│   │       ├── static
│   │       ├── templates
│   │       └── application.yaml
│   └── test
│       └── java
│           └── com
│               └── ntd
│                   └── unsaid
│                       └── UnsaidBackendApplicationTests.java
```
```
Folder Structure of Unsaid Backend Application
1. Layered Architecture: Clear separation between domain, application, infrastructure, and presentation layers
2. Domain Layer: Contains entities, enums, and repositories - the core business model
3. Application Layer: DTOs, mappers, and service interfaces/implementations for business logic
3. Infrastructure Layer: External integrations (Redis, Elasticsearch, Cloudinary, messaging)
4. Presentation Layer: Controllers and WebSocket handlers for API endpoints
5. Security: Dedicated package for JWT and authentication logic
6. Exception Handling: Centralized error handling with custom exceptions
7. Database Migrations: Versioned SQL scripts for schema management
```