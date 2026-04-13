# Herculix Backend v2 Deployment (EC2 + Mongo Atlas + S3 + LaunchDarkly)

This guide deploys the backend to EC2 using MongoDB Atlas, S3, and LaunchDarkly.

## 1) Prerequisites
- EC2 instance (Ubuntu 22.04 recommended)
- Java 17 installed
- MongoDB Atlas connection string
- AWS S3 credentials
- LaunchDarkly SDK key

## 2) Security Group
Open:
- 22 (SSH)
- 8080 (API) or 80/443 if you use Nginx

## 3) Install Java 17 (Ubuntu)
```bash
sudo apt update
sudo apt install -y openjdk-17-jre
java -version
```

## 4) Upload the JAR
Build locally:
```bash
./mvnw clean package -DskipTests
```
Copy to EC2:
```bash
scp target/*.jar ubuntu@<EC2_PUBLIC_IP>:/opt/herculix/herculix.jar
```

## 5) Create environment file
```bash
sudo mkdir -p /opt/herculix
sudo tee /opt/herculix/.env >/dev/null <<'EOF'
SPRING_PROFILES_ACTIVE=ec2
SERVER_PORT=8080

MONGODB_URI=mongodb+srv://<user>:<pass>@<cluster>/<db>?retryWrites=true&w=majority
MONGODB_DATABASE=herculix

AWS_S3_ENABLED=true
AWS_ACCESS_KEY=...
AWS_SECRET_KEY=...
AWS_S3_BUCKET=herculix-media-prod
AWS_S3_PREFIX=prod
AWS_REGION=ap-southeast-2

LAUNCHDARKLY_ENABLED=true
LAUNCHDARKLY_SDK_KEY=...
LAUNCHDARKLY_FLAG_KEY=verified-trainers

MEDIA_SYNC_ON_STARTUP=false

EMAIL_USERNAME=...
EMAIL_PASSWORD=...
EOF
```

## 6) Create systemd service
```bash
sudo tee /etc/systemd/system/herculix.service >/dev/null <<'EOF'
[Unit]
Description=Herculix Backend v2
After=network.target

[Service]
WorkingDirectory=/opt/herculix
EnvironmentFile=/opt/herculix/.env
ExecStart=/usr/bin/java -jar /opt/herculix/herculix.jar
Restart=always
RestartSec=5
User=ubuntu

[Install]
WantedBy=multi-user.target
EOF
```

Enable and start:
```bash
sudo systemctl daemon-reload
sudo systemctl enable herculix
sudo systemctl start herculix
sudo systemctl status herculix
```

## 7) Verify
```bash
curl http://<EC2_PUBLIC_IP>:8080/api/v1/actuator/health
```

## 8) Optional: Nginx reverse proxy
If you want HTTPS + port 80/443, add Nginx and point to `localhost:8080`.

## 9) FE config
Set in the frontend environment:
```
EXPO_PUBLIC_API_BASE_URL=http://<EC2_PUBLIC_IP>:8080/api/v1
```

## 10) Rollback
Keep the previous jar and switch back:
```bash
sudo systemctl stop herculix
sudo mv /opt/herculix/herculix.jar /opt/herculix/herculix.jar.v2
sudo mv /opt/herculix/herculix.jar.v1 /opt/herculix/herculix.jar
sudo systemctl start herculix
```
