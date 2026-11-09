# Sentinel Deployment & Operations Manual

This guide covers production and development deployment for **Sentinel – Enterprise API Monitoring and Incident Management Platform**.

---

## 1. Prerequisites

- **Docker:** Engine 24.0+ & Docker Compose v2+
- **Host System:** Linux (Ubuntu 22.04 LTS recommended), macOS, or Windows 10/11 with WSL2
- **Memory Requirements:** Minimum 4GB RAM (8GB recommended for production workloads)

---

## 2. Docker Compose Deployment (Recommended)

### Step 1: Clone the Repository & Configure Environment

```bash
git clone https://github.com/your-org/sentinel-api-monitoring.git
cd sentinel-api-monitoring

# Create environment file from template
cp .env.example .env
```

Ensure your `.env` contains valid production secrets:
```env
DB_HOST=mysql
DB_PORT=3306
DB_NAME=sentinel_db
DB_USERNAME=root
DB_PASSWORD=YourSecureProductionPassword123!

JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

MONITORING_INTERVAL=60
MONITORING_THREAD_POOL=10
MONITORING_MAX_TIMEOUT=30000
MONITORING_FAILURE_THRESHOLD=3

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=alerts@yourdomain.com
MAIL_PASSWORD=your-app-password
```

### Step 2: Build & Start Services

```bash
docker compose up --build -d
```

### Step 3: Verify Container Health

```bash
docker compose ps
```
All containers should show `healthy` status:
- `sentinel-mysql`: MySQL 8.0 with persistent storage volume
- `sentinel-backend`: Spring Boot 3 running on port 8080
- `sentinel-frontend`: Nginx serving optimized static assets and reverse-proxying API/WebSocket on port 80

### Step 4: Access the Application

- **Web Dashboard:** Open `http://localhost`
- **Default Credentials:**
  - **Email:** `admin@sentinel.io`
  - **Password:** `Admin@123`
- **Backend Actuator Health:** `http://localhost:8080/api/v1/actuator/health`

---

## 3. Local Bare-Metal Development Setup

### MySQL Database Setup
```bash
mysql -u root -p
CREATE DATABASE sentinel_db;
```

### Run Backend
```bash
cd backend
mvn clean spring-boot:run
```
Flyway will automatically execute database migrations (`V1__initial_schema.sql`) and seed the initial administrator account.

### Run Frontend
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173`. Vite proxies `/api` and `/ws` to `http://localhost:8080`.

---

## 4. Maintenance & Operations

### View Real-Time Application Logs
```bash
docker compose logs -f backend
```

### Database Backup
```bash
docker exec sentinel-mysql mysqldump -u root -p sentinel_db > backup_$(date +%Y%m%d).sql
```

### Database Restore
```bash
docker exec -i sentinel-mysql mysql -u root -p sentinel_db < backup.sql
```

