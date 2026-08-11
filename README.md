# 🏨 Microservices Hotel Booking Platform

A cloud-native hotel booking platform built with **Spring Boot**, **Kafka**, **Docker**, **Kubernetes**, **Terraform**, and **AWS**.

---

## 🚀 Quick Start

### Prerequisites
- Docker Desktop

### Run locally

```bash
git clone [https://github.com/aliazamx21/microservices-hotelbooking-platform.git](https://github.com/aliazamx21/microservices-hotelbooking-platform.git)
cd microservices-hotelbooking-platform
docker-compose up -d

🛠 Tech StackCategoryTechnologiesBackendJava 17, Spring Boot, Spring Cloud Gateway, EurekaSecuritySpring Security, JWTDatabaseMySQL, Spring Data JPAMessagingApache Kafka, OpenFeignCloudAWS (EKS, RDS, S3, VPC), TerraformDevOpsDocker, Docker Compose, Kubernetes, GitHub ActionsObservabilityELK Stack, Zipkin, Micrometer, Prometheus, Grafana🏗 Microservices StructureAll microservices are containerized under a single Docker Hub repository (aliazamx21/hotelbooking-platform) using service-specific tags:ServiceDocker TagResponsibilityapi-gateway:api-gatewayAPI Gateway & routing (Port 6969)authservice:authserviceAuthentication & JWT (Port 8086)propertyservice:propertyserviceProperties, Rooms & S3 photos (Port 1116)bookingservice:bookingserviceReservation & room availability management (Port 1112)paymentservice:paymentserviceStripe payment processing (Port 1115)🔄 Architecture FlowUser authenticates via authservice and receives a JWT token.Gateway checks JWT token and forwards requests downstream with injected headers (X-Logged-In-User, X-User-Role).Eureka provides internal service discovery.bookingservice interacts with propertyservice via OpenFeign.Successful Stripe payments update booking statuses asynchronously and trigger events via Kafka.Zipkin traces requests across services, while ELK centralizes logs.

