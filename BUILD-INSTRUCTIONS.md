# Herculix Backend - Build Instructions

## ✅ Build and Run Instructions

### 1. **Build without Tests** (Recommended for first build)
```bash
./mvnw clean install -DskipTests
```

### 2. **Run the Application**

#### Option A: Using Docker (Recommended)
```bash
# Start all services with Docker Compose
docker-compose up -d

# The application will be available at:
# http://localhost:8080/api/v1/swagger-ui.html
```

#### Option B: Manual with Local Databases
```bash
# 1. Start MongoDB
brew services start mongodb-community

# 2. Start Redis
brew services start redis

# 3. Run the application
./mvnw spring-boot:run
```

### 3. **Test the Application**
```bash
# Check health endpoint
curl http://localhost:8080/api/v1/actuator/health

# Access Swagger UI
open http://localhost:8080/api/v1/swagger-ui.html
```

## 🔧 Troubleshooting

### Issue: Test Failures
If tests fail due to external dependencies (MongoDB, Redis), you can:

1. **Skip tests during build:**
```bash
./mvnw clean install -DskipTests
```

2. **Run with test containers (requires Docker):**
```bash
# Make sure Docker is running
docker info

# Run tests
./mvnw test
```

### Issue: Database Connection Errors
Make sure all required services are running:

```bash
# Check MongoDB
mongosh --eval "db.runCommand({ ping: 1 })"

# Check Redis
redis-cli ping
```

### Issue: Port Already in Use
If port 8080 is already in use:

```bash
# Run on different port
SERVER_PORT=8081 ./mvnw spring-boot:run
```

## 📦 Creating JAR for Production
```bash
# Build production JAR
./mvnw clean package -DskipTests -P production

# Run JAR file
java -jar target/Herculix-0.0.1-SNAPSHOT.jar
```

## 🐳 Docker Build
```bash
# Build Docker image
docker build -t trainerhub-backend .

# Run Docker container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  trainerhub-backend
```

## 🧪 Running Specific Tests

### Unit Tests Only
```bash
./mvnw test -Dtest="*ServiceTest"
```

### Integration Tests with TestContainers
```bash
# Requires Docker to be running
./mvnw verify
```

### Skip All Tests
```bash
./mvnw clean install -DskipTests
```

## 📝 Environment Variables

Create `.env` file from template:
```bash
cp .env.example .env
```

Required variables:
- `STRIPE_API_KEY` - Your Stripe secret key
- `EMAIL_USERNAME` - Gmail address for sending emails
- `EMAIL_PASSWORD` - Gmail app-specific password
- `AWS_ACCESS_KEY` - AWS access key for S3 uploads
- `AWS_SECRET_KEY` - AWS secret key for S3 uploads
- `AWS_S3_BUCKET` - S3 bucket name
- `AWS_S3_PREFIX` - S3 prefix (dev or prod)
- `LAUNCHDARKLY_ENABLED` - Enable LaunchDarkly (true/false)
- `LAUNCHDARKLY_SDK_KEY` - LaunchDarkly server-side SDK key
- `LAUNCHDARKLY_FLAG_KEY` - LaunchDarkly flag key (default: verified-trainers)
- `MEDIA_SYNC_ON_STARTUP` - Sync trainer media from S3 (true/false)

## 🚀 Quick Start (Minimal Setup)

For the fastest setup without external dependencies:

```bash
# 1. Build without tests
./mvnw clean install -DskipTests

# 2. Start with Docker Compose
docker-compose up -d

# 3. Verify it's running
curl http://localhost:8080/api/v1/actuator/health
```

The application will automatically create database schemas on startup.
