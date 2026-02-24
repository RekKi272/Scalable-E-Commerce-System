# HMK Eyewear – Scalable E-Commerce System

HMK Eyewear is a **cloud-ready scalable e-commerce backend system** built using **Spring Boot microservices architecture**.  
The system is designed for scalability, security, and real-world deployment following clean architecture principles.

It is fully containerized using **Docker & Docker Compose**, supporting asynchronous messaging, secure JWT authentication, and VNPay payment integration.

---

## Project Objectives

- Design a production-style e-commerce backend using microservices
- Apply clean architecture & Spring Boot best practices
- Implement secure authentication & authorization using JWT
- Enable asynchronous inter-service communication
- Integrate online payment via VNPay
- Implement automated email notification service
- Containerize and deploy using Docker

---

## System Architecture Overview

The system follows a **distributed microservices architecture** with centralized routing and service discovery.

### Core Components:
- **API Gateway** – Centralized request routing & security filtering
- **Service Registry (Eureka)** – Service discovery
- **Authentication Service** – JWT & refresh token management
- **RabbitMQ** – Asynchronous event-driven communication
- **Redis** – Distributed caching & refresh token storage
- **Docker Compose** – Container orchestration

---

## Microservices Description

| Service | Description |
|----------|-------------|
| **api-gateway** | Routes incoming requests & validates JWT |
| **auth-service** | Authentication, Access Token & Refresh Token handling |
| **user-service** | User account management |
| **product-service** | Product management (Redis caching enabled) |
| **cart-service** | Shopping cart operations |
| **order-service** | Order lifecycle management |
| **payment-service** | VNPay payment integration |
| **notification-service** | Sends OTP for password reset and payment invoice emails |
| **blog-service** | Blog & content management (Redis caching enabled) |
| **file-service** | File upload & storage |
| **common-dto** | Shared DTO module |
| **service_registry** | Netflix Eureka Server |

---

## Inter-Service Communication

- **Synchronous communication:** RESTful APIs (Spring Web)
- **Asynchronous communication:** RabbitMQ (event-driven architecture)

---

## Authentication & Authorization
The system uses **JWT-based stateless authentication** with refresh token support.

### Features

- Access Token + Refresh Token mechanism
- Secure refresh token rotation
- Refresh tokens stored in **Redis**
- Spring Security 6+
- Role-based access control (RBAC)
- Token validation handled at API Gateway
- Centralized security filtering

### Authentication Flow

1. User logs in → receives Access Token + Refresh Token
2. Access Token used for authenticated requests
3. When Access Token expires → client sends Refresh Token
4. System validates Refresh Token from Redis
5. New Access Token is issued (optionally rotate refresh token)

---

## Caching Layer – Redis

Redis is used as a distributed in-memory cache to improve system performance.

### Applied To

- Product service
- Blog service
- Refresh token storage

### Benefits

- Reduced database read operations
- Faster API response time
- Lower latency under high traffic
- Better scalability
- TTL-based cache expiration strategy
- Reduced load on Firestore

---

## Payment Integration (VNPay)

- VNPay Sandbox integration
- Secure payment URL generation
- IPN & Callback handling
- HMAC SHA-512 signature validation
- Payment response verification
- Proper timezone handling (GMT+7)

---

## Notification System

The notification-service handles automated email delivery in an asynchronous manner via RabbitMQ.

### Features

- Sends OTP email when user requests password reset
- Sends payment invoice email after successful payment
- Event-driven email processing
- Integrated with SMTP provider
- Decoupled from core business services
- Scalable and non-blocking architecture

---

## Technology Stack

### Backend
- Java 17
- Spring Boot
- Spring Security
- Spring Data

### Architecture
- Microservices Architecture
- API Gateway
- Service Discovery (Netflix Eureka)

### Messaging
- RabbitMQ

### Authentication
- JWT (JSON Web Token)

### Databases
- **Firebase Firestore** – NoSQL database for core application data
- **Firebase Authentication** – User identity management
- **Supabase (PostgreSQL)** – Relational database for structured data & analytics
- **Supabase Storage** – File & asset storage

### Payment Gateway
- VNPay (Sandbox)

### DevOps & Tools
- Docker & Docker Compose
- Maven
- Lombok
- Postman
- Git & GitHub

---

## Project Structure

```text
HMKEyewear/
├── api-gateway/
├── auth-service/
├── user-service/
├── product-service/
├── cart-service/
├── order-service/
├── payment-service/
├── notification-service/
├── blog-service/
├── file-service/
├── common-dto/
├── service_registry/
│
├── docker-compose.yml
├── .env
├── pom.xml
└── README.md
```


---

# Running the Project

## Requirements

Make sure you have installed:

- Docker
- Docker Compose
- JDK 17
- Maven 3.8+

---

## Environment Configuration

Create a `.env` file in root directory:

```env
# =========================
# SPRING PROFILE
# =========================
SPRING_PROFILES_ACTIVE=local

# =========================
# JWT CONFIGURATION
# =========================
JWT_SECRET=your_jwt_secret

# =========================
# REDIS
# =========================
REDIS_HOST=redis
REDIS_PASSWORD=your_redis_password

# =========================
# RABBITMQ
# =========================
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest

# =========================
# ELASTICSEARCH
# =========================
SPRING_ELASTICSEARCH_URIS=http://elasticsearch:9200

# =========================
# GOOGLE FIREBASE CREDENTIALS
# (These are file paths inside container)
# =========================
GOOGLE_APPLICATION_CREDENTIALS=/app/firebase-user.json

# =========================
# VNPAY CONFIGURATION
# =========================
VNP_URL=
VNP_TMN_CODE=
VNP_SECRET_KEY=
VNP_RETURN_URL=
VNP_VERSION=
VNP_COMMAND=
VNP_ORDER_TYPE=

# =========================
# EMAIL (Notification Service)
# =========================
MAIL_USERNAME=
MAIL_APP_PASSWORD=

# =========================
# SUPABASE (File Service)
# =========================
SUPABASE_SECRETS_PATH=/app/supabase-secrets.json
```

## Important

Before running docker-compose, make sure you place all required secret files inside the `/secrets` directory:
```text
secrets/
├── firebase-user.json
├── firebase-blog.json
├── firebase-cart.json
├── firebase-order.json
├── firebase-product.json
└── supabase-secrets.json
```

### Notes

- These files contain sensitive credentials and must **not** be committed to Git.
- Ensure the `/secrets` directory is mounted correctly in `docker-compose.yml`.
- In production environments, use secure secret management solutions

---

From the root directory:

```bash
mvn clean install -DskipTests
```

Or if using Maven Wrapper:

```bash
./mvnw clean install -DskipTests
```

Each service will generate:

```
target/*.jar
```

Important:  
You must build JAR files before running Docker Compose.

## Run Docker Compose

After building all JAR files:

```bash
docker compose -f docker-compose.local.yml up
```

Or run in detached mode:

```bash
docker compose -f docker-compose.local.yml up -d
```

Note:  
We do **not** use `docker-compose up --build` because JAR files are already built externally.

---

## Stop the System

```bash
docker compose -f docker-compose.local.yml down
```

To remove volumes as well:

```bash
docker compose -f docker-compose.local.yml down -v
```

---

# When Code Changes

Whenever you modify the source code:

```bash
mvn clean install -DskipTests
docker compose -f docker-compose.local.yml down
docker compose -f docker-compose.local.yml up -d
```

---

# Development Mode (Optional)

If running without Docker:

```bash
mvn spring-boot:run
```

---

# Deployment Philosophy

This project follows modern production practices:

- Build phase separated from runtime phase
- Stateless microservices
- Event-driven communication
- Centralized security at API Gateway
- Redis-based distributed caching
- Dockerized infrastructure
- CI/CD ready architecture

---

# Authors

Developed by:

- Trần Minh Đức  
- Nguyễn Thanh Duy  

Supervisor: M.Sc. Mai Văn Mạnh

