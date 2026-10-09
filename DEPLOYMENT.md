# AnnPath Platform — Production Deployment Guide

**Version**: 1.0.0 Enterprise  
**Target Environment**: Java EE 7 / Servlet 3.1 Container (GlassFish 4.1.1 / Payara / Tomcat 8.5+)  
**Database**: MySQL 8.0+ (InnoDB, UTF-8 MB4)  
**JDK**: Oracle JDK 8 / OpenJDK 8 (8u301+)  

---

> [!IMPORTANT]
> **Manual Deployment Notice**: As per project architectural policy, automated CI/CD deployments are intentionally decoupled. System administrators and DevOps engineers must perform the deployment steps manually following the verified procedures detailed below.

---

## 1. System Architecture & Prerequisites

### 1.1 Minimum Hardware Requirements
| Component | Minimum Specification | Recommended Production |
| :--- | :--- | :--- |
| **CPU** | 2 vCPU / 2.4 GHz | 4–8 vCPU |
| **RAM** | 4 GB | 8–16 GB (JVM Heap: 4–8 GB) |
| **Disk Space** | 20 GB SSD | 100+ GB NVMe (for document & photo uploads) |
| **Network** | 100 Mbps | 1 Gbps with TLS 1.3 reverse proxy |

### 1.2 Software Stack Prerequisites
- **Operating System**: Linux (Ubuntu 20.04/22.04 LTS, RHEL/AlmaLinux 8/9) or Windows Server 2019/2022.
- **Java SE Development Kit**: JDK 1.8.0_301 or higher (`JAVA_HOME` pointing to JDK).
- **Relational Database**: MySQL Server 8.0.x with InnoDB storage engine.
- **Application Server**: GlassFish Server Open Source Edition 4.1.1 (or Payara Server 5.x / Apache Tomcat 8.5/9.0).
- **Reverse Proxy**: NGINX 1.18+ or Apache HTTP Server 2.4+ (for SSL offloading & static caching).

---

## 2. Database Provisioning & Schema Initialization

### 2.1 MySQL Server Configuration
Edit your MySQL server configuration (`my.cnf` on Linux or `my.ini` on Windows):
```ini
[mysqld]
default_time_zone = '+00:00'
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci
max_connections = 200
innodb_buffer_pool_size = 2G
innodb_file_per_table = 1
innodb_flush_log_at_trx_commit = 1
sql_mode = "STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION"
```
Restart MySQL to apply configuration.

### 2.2 Dedicated Production Database & User Provisioning
Execute the following commands in the MySQL client as `root`:
```sql
-- 1. Create UTF-8 Multilingual Database
CREATE DATABASE IF NOT EXISTS `annpath`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- 2. Create Restricted Production Application User
CREATE USER 'annpath_app'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';

-- 3. Grant Application Permissions
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE TEMPORARY TABLES, LOCK TABLES 
    ON `annpath`.* TO 'annpath_app'@'localhost';

FLUSH PRIVILEGES;
```

### 2.3 Applying Schemas & Reference Seeds
Navigate to the project root directory and execute the schema script:
```bash
# On Linux / macOS
mysql -u annpath_app -p -h localhost annpath < database/annpath.sql

# On Windows (PowerShell)
Get-Content database\annpath.sql | mysql -u annpath_app -p annpath
```

*Note: `annpath.sql` creates all 26 tables, foreign keys, indexes, and populates baseline reference data (ISO countries, states/provinces, units of measure, food categories, and automated country verification rules).*

---

## 3. Configuration Management

Configuration parameters are externalized in `application.properties`. 

### 3.1 Production Properties File Location
Place your production properties file at:
- **Classpath fallback**: `WEB-INF/classes/application.properties` (bundled inside the WAR)
- **External override**: Pass `-Dannpath.config.dir=/etc/annpath` as a JVM argument and place `application.properties` at `/etc/annpath/application.properties`.

### 3.2 Production Values Checklist (`application.properties`)
```properties
# ====================================================================
# ANNPATH PRODUCTION ENVIRONMENT CONFIGURATION
# ====================================================================
app.env=production
app.base.url=https://annpath.org
app.timezone=UTC

# --- DATABASE CONNECTION (Self-Healing Pool & Failover) ---
db.url=jdbc:mysql://localhost:3306/annpath?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8
db.user=annpath_app
db.password=${DB_PASSWORD}
db.pool.initial=10
db.pool.max=50
db.pool.timeout.ms=30000

# --- JNDI DATASOURCE (For GlassFish container-managed pooling) ---
db.jndi.name=jdbc/AnnPathDB

# --- SECURITY & CRYPTOGRAPHIC SETTINGS ---
security.password.hash.algorithm=PBKDF2WithHmacSHA512
security.password.hash.iterations=100000
security.session.timeout.minutes=60
security.rate.limit.requests.per.minute=120
security.csrf.token.length=32

# --- OTP POLICIES ---
otp.validity.minutes=10
otp.max.attempts=3
otp.cooldown.minutes=2

# --- URGENCY & RESCUE TIMINGS ---
urgency.critical.hours=3
urgency.expiring.soon.hours=12
volunteer.response.timeout.minutes=30

# --- UPLOADS & ATTACHMENTS ---
upload.storage.path=/var/annpath/uploads
upload.max.file.size.mb=5
upload.max.request.size.mb=10
upload.allowed.extensions=pdf,png,jpg,jpeg
```

Create the upload storage directory and assign ownership to the application server service account:
```bash
sudo mkdir -p /var/annpath/uploads
sudo chown -R glassfish:glassfish /var/annpath/uploads
sudo chmod 750 /var/annpath/uploads
```

---

## 4. GlassFish 4.1.1 Deployment Procedures

### 4.1 Creating the Container JDBC Connection Pool
Using the GlassFish CLI (`asadmin`):
```bash
# 1. Ensure MySQL Connector/J 8.0+ is in GlassFish lib folder
cp /path/to/mysql-connector-java-8.0.11.jar /opt/glassfish4/glassfish/domains/domain1/lib/

# 2. Start domain
asadmin start-domain domain1

# 3. Create JDBC Connection Pool
asadmin create-jdbc-connection-pool \
    --datasourceclassname com.mysql.cj.jdbc.MysqlDataSource \
    --restype javax.sql.DataSource \
    --property portNumber=3306:serverName=localhost:databaseName=annpath:User=annpath_app:Password=${DB_PASSWORD}:useSSL=false:serverTimezone=UTC \
    AnnPathPool

# 4. Create JNDI Resource
asadmin create-jdbc-resource --connectionpoolid AnnPathPool jdbc/AnnPathDB

# 5. Ping Connection Pool
asadmin ping-connection-pool AnnPathPool
```

### 4.2 Setting Production JVM Parameters
```bash
# Heap and Garbage Collector Tuning
asadmin delete-jvm-options "-Xmx512m"
asadmin create-jvm-options "-Xms2g:-Xmx4g:-XX:MetaspaceSize=256m:-XX:MaxMetaspaceSize=512m:-Duser.timezone=UTC:-Dfile.encoding=UTF-8"
asadmin restart-domain domain1
```

### 4.3 Deploying the WAR Package
From the project directory:
```bash
# Build production artifact
ant dist

# Deploy with root context '/' or '/AnnPath'
asadmin deploy --contextroot / --name AnnPath dist/AnnPath.war
```

### 4.4 Alternative: GlassFish Web GUI Deployment
1. Open Admin Console: `http://<server-ip>:4848`
2. Navigate to **Applications** -> **Deploy...**
3. Choose Local Packaged File: `dist/AnnPath.war`
4. Application Type: **Web Application**
5. Context Root: `/` (or leave default `/AnnPath`)
6. Click **OK**.

---

## 5. Alternative: Apache Tomcat 8.5 / 9.0 Deployment

If deploying to Apache Tomcat:
1. Copy `mysql-connector-java-8.0.11.jar` to `$CATALINA_HOME/lib/`.
2. Add the Resource to `$CATALINA_HOME/conf/server.xml` (inside `<GlobalNamingResources>`):
```xml
<Resource name="jdbc/AnnPathDB"
          auth="Container"
          type="javax.sql.DataSource"
          maxTotal="50"
          maxIdle="10"
          maxWaitMillis="10000"
          username="annpath_app"
          password="${DB_PASSWORD}"
          driverClassName="com.mysql.cj.jdbc.Driver"
          url="jdbc:mysql://localhost:3306/annpath?useSSL=false&amp;serverTimezone=UTC&amp;characterEncoding=UTF-8"/>
```
3. In `$CATALINA_HOME/conf/context.xml`, add `<ResourceLink>`:
```xml
<ResourceLink name="jdbc/AnnPathDB"
              global="jdbc/AnnPathDB"
              type="javax.sql.DataSource"/>
```
4. Copy `dist/AnnPath.war` to `$CATALINA_HOME/webapps/ROOT.war` (for root URL) or `$CATALINA_HOME/webapps/AnnPath.war`.
5. Restart Tomcat: `$CATALINA_HOME/bin/shutdown.sh && $CATALINA_HOME/bin/startup.sh`.

---

## 6. NGINX Reverse Proxy & HTTPS Hardening

In production, terminate SSL/TLS at NGINX and pass requests to GlassFish/Tomcat via HTTP/1.1 with secure headers.

### 6.1 Sample `/etc/nginx/sites-available/annpath.conf`
```nginx
# Redirect HTTP to HTTPS
server {
    listen 80;
    listen [::]:80;
    server_name annpath.org www.annpath.org;
    return 301 https://$host$request_uri;
}

# Production HTTPS Server
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name annpath.org www.annpath.org;

    ssl_certificate /etc/letsencrypt/live/annpath.org/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/annpath.org/privkey.pem;

    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 1d;

    # Security Headers
    add_header X-Frame-Options "DENY" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains; preload" always;

    # Maximum file upload size (matches application.properties 10MB limit)
    client_max_body_size 12M;

    # Static Assets Caching
    location /assets/ {
        proxy_pass http://127.0.0.1:8080/assets/;
        expires 7d;
        add_header Cache-Control "public, no-transform";
    }

    # Proxy to Java EE Application Container
    location / {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket & Long Polling Support
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";

        proxy_connect_timeout 60s;
        proxy_read_timeout 120s;
        proxy_send_timeout 120s;
    }
}
```

---

## 7. Operational Maintenance & Backup Strategy

### 7.1 Automated Database Backup Script
Save as `/usr/local/bin/backup-annpath.sh`:
```bash
#!/bin/bash
set -euo pipefail

BACKUP_DIR="/var/backups/annpath"
DATE=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/annpath_db_${DATE}.sql.gz"

mkdir -p "${BACKUP_DIR}"

# Export schema + data with single-transaction consistency
mysqldump -u annpath_app -p'${DB_PASSWORD}' \
    --single-transaction \
    --quick \
    --routines \
    --triggers \
    annpath | gzip -9 > "${BACKUP_FILE}"

# Retain 30 days of daily backups
find "${BACKUP_DIR}" -name "annpath_db_*.sql.gz" -type f -mtime +30 -delete

echo "AnnPath backup successfully generated: ${BACKUP_FILE}"
```
Make executable and add to crontab (`crontab -e`):
```cron
0 2 * * * /usr/local/bin/backup-annpath.sh >> /var/log/annpath_backup.log 2>&1
```

### 7.2 Post-Deployment Verification Checklist
1. **Health Verification**: Load `https://annpath.org/home` and confirm HTTP 200 with dynamic metrics displayed.
2. **Database Connectivity**: Verify no connection errors in application server logs (`domain1/logs/server.log`).
3. **Background Daemon**: Confirm `AppContextListener` background thread logged `"AnnPath background scheduler registered successfully."` and runs the urgency scan every 60s.
4. **Security Filter Chain**: Test `POST` request without CSRF token to verify HTTP 403 response.
5. **Localization**: Test locale switching to Hindi, Spanish, and French (`/locale?lang=hi`) and confirm localized messages load from resource bundles.
6. **Digital Receipts**: Complete a test pickup handover and verify tamper-evident SHA-256 digital signature displays on `/receipt/view?receiptNumber=...`.
