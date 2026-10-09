# AnnPath — A Path From Surplus to Need

> **Production-Grade Global Food Surplus Redistribution Platform**  
> Connecting food donors, verified NGOs, and volunteer dispatch networks to move surplus food safely, rapidly, and transparently before it expires.

---

## 🌟 Key Features

- **Multi-Role Portals**: Tailored interfaces for Food Donors, Verified NGOs, and Volunteer Couriers.
- **Explainable Smart Matching Engine**: Multi-factor scoring matching food types, quantities, dietary requirements, and geographic transit radius.
- **Rescue Priority Engine (RPS)**: Real-time urgency calculation based on food shelf-life, storage condition, and community demand.
- **Rescue Autopilot & Journey Passport**: Full chain-of-custody tracking with cryptographic receipts and state transitions.
- **Multi-Channel Verification**: Dual OTP verification, secure password hashing (PBKDF2/SHA-256 with random salt), and session protection.
- **RESTful API**: Comprehensive JSON endpoints (`/api/health`, `/api/impact`, `/api/food/listings`, `/api/receipts/*`).

---

## 🛠️ Architecture & Tech Stack

- **Backend**: Native Java 8 EE (Servlets, Filters, JSPs)
- **Database**: MySQL 8.0 with JDBC Connection Pooling
- **Application Server**: GlassFish 4.1.1 / Payara / Tomcat EE
- **Frontend**: Responsive HTML5, Vanilla CSS3, JavaScript (Locked Enterprise UI)
- **Security**: CSP, HSTS, SameSite cookies, CSRF Protection, PBKDF2 Password Hashing, SHA-256 OTP hashing

---

## 🚀 Quick Start Guide

### 1. Database Setup
Import the database schema:
```bash
mysql -u root -p < database/annpath.sql
```

### 2. Configuration
Review and customize runtime properties in `src/java/application.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/annpath?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=root
db.password=your_password
```

### 3. Build & Deploy
Compile and generate the production WAR archive using Ant:
```bash
ant clean dist
```
Deploy `dist/AnnPath.war` to your GlassFish / Java EE application server.

---

## 🧪 Testing

Run the automated test runner to verify all 8 test suites:
- Security & Cryptography Suite
- Urgency & Priority Algorithmic Suite
- Concurrency & Inventory Suite
- Rescue Autopilot Suite
- Failure Recovery Engine Suite
- Explainable Matching Engine Suite
- Rescue Journey & Passport Suite
- Rescue Bottleneck Detector Suite

All suites achieve 100% pass rate.

---

## 📄 License
Licensed under the Apache License 2.0.
