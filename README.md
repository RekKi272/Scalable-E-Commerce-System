# HMK Eyewear – Scalable E-Commerce System

HMK Eyewear is a **scalable e-commerce backend system** built using **Spring Boot microservices architecture**, designed for high scalability, security, and real-world deployment.  
The system is fully containerized with **Docker & Docker Compose** and integrates **asynchronous messaging**, **secure authentication**, and **online payment processing**.

---

## Project Objectives

- Design a real-world e-commerce backend using microservices
- Apply clean architecture and best practices in Spring Boot
- Implement secure authentication & authorization with JWT
- Enable asynchronous communication between services
- Integrate online payment using VNPay
- Deploy the system using Docker & Docker Compose

---

## System Architecture Overview

The system follows a **distributed microservices architecture** with centralized routing and service discovery.

### Core Components:
- **API Gateway** – Centralized entry point for all client requests
- **Service Registry (Eureka)** – Service discovery & load balancing
- **Authentication Service** – Centralized authentication & token management
- **RabbitMQ** – Asynchronous inter-service communication
- **Docker** – Containerized deployment

---

## Microservices Description

| Service | Description |
|-------|------------|
| **api-gateway** | Routes all incoming requests to appropriate microservices |
| **auth-service** | Handles authentication, JWT token generation & validation |
| **user-service** | User account & profile management |
| **product-service** | Product management |
| **inventory-service** | Inventory & stock management |
| **cart-service** | Shopping cart operations |
| **order-service** | Order creation and order lifecycle management |
| **payment-service** | VNPay payment processing & transaction validation |
| **blog-service** | Blog & content management |
| **file-service** | File upload and storage service |
| **common-dto** | Shared DTOs for inter-service communication |
| **service_registry** | Eureka Server for service discovery |

---

## Inter-Service Communication

- **Synchronous communication:** RESTful APIs (Spring Web)
- **Asynchronous communication:** RabbitMQ (event-driven architecture)

---

## Authentication & Authorization

- JWT-based authentication
- Stateless security configuration
- Spring Security 6+
- Role-based access control
- Token validation handled via API Gateway

---

## Payment Integration (VNPay)

- VNPay Sandbox integration
- Secure payment URL generation
- IPN & Callback handling
- HMAC SHA-512 signature validation
- Payment response verification
- Proper timezone handling (GMT+7)

---

## Docker & Deployment

The entire system is containerized and orchestrated using **Docker Compose**.

### Included:
- Dockerfile for each microservice
- Central `docker-compose.yml`
- Environment configuration via `.env`

### Run the system:
```bash
docker-compose up --build
```

### Stop the system:
```bash
docker-compose down
```

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
├── inventory-service/
├── cart-service/
├── order-service/
├── payment-service/
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
