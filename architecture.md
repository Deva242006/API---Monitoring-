# Sentinel Architecture & System Design Documentation

## 1. High-Level System Architecture

Sentinel is built as a feature-based modular monolith in **Java 21** with **Spring Boot 3.x** and **MySQL 8.0**, paired with a **React + TypeScript + Vite + Tailwind CSS** enterprise dashboard. Real-time observability events are streamed using **Spring WebSocket (STOMP over SockJS)**.

```mermaid
flowchart TD
    subgraph Client["Frontend Client (React / Vite / Tailwind)"]
        UI[SaaS Dashboard UI]
        AxiosClient[Axios HTTP Client]
        StompClient[STOMP WebSocket Client]
    end

    subgraph ReverseProxy["Nginx Web Server"]
        Nginx[Nginx Reverse Proxy & Static Host]
    end

    subgraph Backend["Spring Boot 3.2.5 (Java 21)"]
        SecFilter[Spring Security & JWT Filter]
        REST[REST Controllers /api/v1/*]
        WSEndpoint[STOMP Message Broker /ws]
        
        subgraph CoreServices["Domain Services Layer"]
            AuthSvc[Auth Service & Token Rotation]
            ProjSvc[Project Management Service]
            ApiSvc[Monitored API Service]
            SSRF[SSRF UrlValidator Guard]
            Engine[Monitoring Engine Scheduler]
            Pool[ThreadPoolTaskExecutor Concurrency Guard]
            HealthSvc[Health Check Record Service]
            IncSvc[Incident Lifecycle Manager]
            NotifSvc[Notification & Mail Service]
            AnalyticsSvc[Analytics & Uptime Calculator]
            AuditSvc[Audit Logging Service]
        end
    end

    subgraph Persistence["Storage & Broker"]
        MySQL[(MySQL 8.0 Relational DB)]
        Flyway[Flyway Migrations Engine]
        SMTP[External SMTP Mail Server]
    end

    subgraph ExternalTargets["Monitored Targets"]
        ExtAPI1[Payment API]
        ExtAPI2[Auth Service API]
        ExtAPI3[Order Microservice]
    end

    Client -->|HTTPS /api/v1| Nginx
    Client -->|WSS /ws| Nginx
    Nginx -->|Port 8080| SecFilter
    SecFilter --> REST
    SecFilter --> WSEndpoint

    REST --> CoreServices
    Engine --> Pool
    Pool -->|Non-blocking HTTP GET/POST/PUT/DELETE| ExternalTargets
    Pool --> HealthSvc
    HealthSvc --> IncSvc
    IncSvc --> NotifSvc
    NotifSvc --> SMTP
    NotifSvc -->|Real-time Push| WSEndpoint
    WSEndpoint -.->|Live In-App Events| StompClient

    CoreServices --> MySQL
    Flyway -.->|Versioned Migrations| MySQL
```

---

## 2. Entity-Relationship (ER) Diagram

```mermaid
erDiagram
    USERS ||--o{ REFRESH_TOKENS : owns
    USERS ||--o{ PROJECTS : owns
    USERS ||--o{ PROJECT_MEMBERS : joins
    PROJECTS ||--o{ PROJECT_MEMBERS : contains
    PROJECTS ||--o{ MONITORED_APIS : contains
    MONITORED_APIS ||--o{ HEALTH_CHECKS : records
    MONITORED_APIS ||--o{ INCIDENTS : triggers
    INCIDENTS ||--o{ INCIDENT_COMMENTS : contains
    USERS ||--o{ INCIDENT_COMMENTS : authors
    USERS ||--o{ NOTIFICATIONS : receives
    USERS ||--o{ AUDIT_LOGS : generates

    USERS {
        bigint id PK
        string email UK
        string password_hash
        string full_name
        enum role "ADMIN, DEVELOPER, VIEWER"
        boolean active
        datetime last_login_at
        datetime created_at
    }

    REFRESH_TOKENS {
        bigint id PK
        string token_hash UK
        bigint user_id FK
        datetime expires_at
        boolean revoked
        datetime created_at
    }

    PROJECTS {
        bigint id PK
        string name
        text description
        bigint owner_id FK
        boolean active
        datetime created_at
    }

    PROJECT_MEMBERS {
        bigint id PK
        bigint project_id FK
        bigint user_id FK
        datetime joined_at
    }

    MONITORED_APIS {
        bigint id PK
        string name
        string endpoint_url
        enum http_method "GET, POST, PUT, DELETE"
        int expected_status_code
        int timeout_ms
        int monitoring_interval_seconds
        bigint project_id FK
        boolean active
        enum current_status "HEALTHY, DEGRADED, DOWN, UNKNOWN"
        int consecutive_failures
        datetime last_checked_at
        bigint last_response_time_ms
    }

    HEALTH_CHECKS {
        bigint id PK
        bigint api_id FK
        enum status "HEALTHY, DEGRADED, DOWN, UNKNOWN"
        bigint response_time_ms
        int status_code
        text error_message
        datetime checked_at
    }

    INCIDENTS {
        bigint id PK
        string title
        text description
        bigint api_id FK
        enum severity "LOW, MEDIUM, HIGH, CRITICAL"
        enum status "OPEN, ACKNOWLEDGED, IN_PROGRESS, RESOLVED"
        bigint assigned_to FK
        bigint created_by FK
        datetime resolved_at
        boolean auto_generated
        datetime created_at
    }

    INCIDENT_COMMENTS {
        bigint id PK
        bigint incident_id FK
        bigint user_id FK
        text content
        datetime created_at
    }

    NOTIFICATIONS {
        bigint id PK
        bigint user_id FK
        string title
        text message
        enum type
        bigint reference_id
        boolean is_read
        datetime created_at
    }

    AUDIT_LOGS {
        bigint id PK
        bigint user_id FK
        string action
        string entity_type
        bigint entity_id
        text details
        string ip_address
        datetime created_at
    }
```

---

## 3. Incident Lifecycle State Transition

```mermaid
stateDiagram-v2
    [*] --> OPEN: Consecutive Failures >= 3
    OPEN --> ACKNOWLEDGED: Developer Acknowledges Ticket
    ACKNOWLEDGED --> IN_PROGRESS: Investigation Begun
    IN_PROGRESS --> RESOLVED: API Recovers or Fixed by Engineer
    OPEN --> RESOLVED: Automated Recovery Detection
    RESOLVED --> [*]
```

---

## 4. SSRF Defense Guard Architecture

Before any endpoint URL is stored or probed, it passes through `UrlValidator`:
1. **Scheme Validation:** Only `http://` and `https://` are permitted. Schemes like `file://`, `ftp://`, `gopher://` are rejected.
2. **Loopback & Localhost Rejection:** URLs targeting `localhost`, `127.0.0.1`, `::1`, `0.0.0.0`, `*.localhost` are blocked.
3. **Cloud Metadata Endpoint Defense:** Endpoints targeting `169.254.169.254`, `metadata.google.internal`, or `*.internal` are rejected.
4. **DNS Resolution Check:** Resolves destination IP via `InetAddress.getByName()` to detect and block RFC-1918 private subnets (`10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`), link-local, and multicast addresses.

---

## 5. Non-Blocking Synthetic Probe Model

To prevent thread exhaustion and latency cascading:
1. Probes are executed outside of database transactions (`@Transactional`).
2. Concurrent checks run on a bounded `ThreadPoolTaskExecutor` (10 core, 20 max threads, bounded queue).
3. Per-API in-memory lock table (`ConcurrentHashMap<Long, Boolean>`) guarantees that a slow endpoint never triggers overlapping concurrent probes for the same API.
4. Probe results are written to `health_checks` and evaluated asynchronously for incident creation and WebSocket dispatches.

