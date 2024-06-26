# 인프라 포팅메뉴얼

---

# NH 드림 포팅 메뉴얼

1. 주요 인프라
2. 설치
3. 연결
4. 빌드 및 배포

---

# 1. 주요 인프라

### **OS**

- Amazon EC2 Unbuntu 20.04 LTS ( 메모리 : 16GB, 디스크 용량 : 320GB )

### **Storage**

- Amazon S3 5GB( Freetier )

### **Container**

- Docker Engine v26.1.0
- Docker Compose v2.26.1

---

## Back-End

### WAS

- **java** : openjdk:21
- **FrameWork** : Spring boot 3.2.5
- **ORM** : Spring Data JPA 3.2.5
- **Auth**: Spring Security 3.2.5
- **JWT** : 0.12.3

### BlockChain

- web3j : 4.11.3

### DB

- **RDBMS** : MySQL : 8.0.36
- **DBMS**: 7.2.4

---

## Front-End

### JavaScript

- **npm** : node:latest
- **vite** : 5.2.0
- **react** : 18.2.0
- **react zustand** : 4.5.2

### CSS

- **DaisyUI** : 4.10.3
- **Tailwind** : 3.4.3
- **SweetAlert2** : 11.10.8

### BlockChain

- web3 : 4.8.0

---

## Block Chain

### **Private Network**

- Ganache CLI v6.12.2 (ganache-core: 2.13.2)

### Contract Deploy Tool

- remix

---

## 기타

### CI/CD

- Jenkins 2.445
- GitLab
- MatterMost

### 이슈관리

- Jira

### 형상관리

- Git (GitLab)

### 커뮤니케이션

- MatterMost, Notion

### 디자인

- Figma

---

## 디렉토리 구조

```yaml
├── README.md
├── admin
│   └── nhdream
│       ├── Dockerfile
│       ├── blockchain
│       ├── index.html
│       ├── jsconfig.json
│       ├── package-lock.json
│       ├── package.json
│       ├── postcss.config.js
│       ├── public
│       ├── src
│       ├── tailwind.config.js
│       └── vite.config.js
├── backend
│   ├── nhdream
│   │   ├── Dockerfile
│   │   ├── build
│   │   ├── build.gradle
│   │   ├── gradle
│   │   ├── gradlew
│   │   ├── gradlew.bat
│   │   ├── settings.gradle
│   │   └── src
│   └── nhdream@tmp
│       └── secretFiles
├── blockchain
│   ├── DRDC.sol
│   ├── Loan.sol
│   ├── NHDC.sol
│   ├── ReDeposit.sol
│   └── Saving.sol
├── docker-compose.yml
├── frontend
│   ├── nh-dream
│   │   ├── Dockerfile
│   │   ├── README.md
│   │   ├── index.html
│   │   ├── jsconfig.json
│   │   ├── manifest.json
│   │   ├── package-lock.json
│   │   ├── package.json
│   │   ├── postcss.config.js
│   │   ├── public
│   │   ├── src
│   │   ├── tailwind.config.js
│   │   └── vite.config.js
│   └── nh-dream@tmp
│       └── secretFiles
└── package-lock.json

```

---

## 사용하는 포트

`22` : ssh

`80, 443` : nginx

`9090` : jenkins

`3209` : mysql

`6379` : redis

`8545` : ganache-cli

`1322` : 관리자 페이지

- 사용하는 포트 열어주기

```bash
sudo ufw allow {포트번호}
```

- 적용하기

```bash
sudo ufw enable
```

---

# 2. 설치

## 1. Docker 설치

- EC2 업데이트 내역 확인 및 업그레이드 후 도커 설치

```bash
> sudo apt update
> sudo apt upgrade
> sudo apt-get install apt-transport-https ca-certificates curl gnupg-agent software-properties-common
> curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
> sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
> sudo apt-get update
> sudo apt-get install docker-ce docker-ce-cli [containerd.io](http://containerd.io/)
> sudo usermod -aG docker ubuntu
> sudo newgrp docker
```

- 설치 후 Docker 실행 상태 ‘`active(running)`’ 확인

```bash
sudo systemctl status docker
```

- **각 컨테이너는 docker-compose.yml 파일로 관리하기 때문에 .yml 파일은 각 파일 이름으로**

**디렉토리를 만들어서 진행합니다.**

## 2. Jenkins 컨테이너 설치

- Jenkins 이미지 다운로드

```bash
docker pull jenkins/jenkins:jdk21
```

- Jenkins docker-compose.jenkins.yml 작성

```yaml
services:
  jenkins:
    container_name: jenkins
    image: jenkins/jenkins:jdk21
    restart: unless-stopped
    user: root
    ports:
      - 9090:8080
    volumes:
      - /home/ubuntu/jenkins:/var/jenkins_home
      - /var/run/docker.sock:/var/run/docker.sock
```

- Jenkins 컨테이너 빌드

```bash
docker compose -f ./jenkinsCompose/docker-compose.jenkins.yml up -d --build
```

## 3. FrontEnd (Nginx)

[ 디렉토리 구조 및 디렉토리 명이 맞지 않으면 동작하지 않습니다. ]

### 3-1 FrontEnd

- FrontEnd Dockerfile 작성

```yaml
FROM node:lts-alpine as build-stage-front

WORKDIR /homepage/front

COPY frontend/nh-dream/package*.json .

RUN npm install

COPY frontend/nh-dream .

RUN npm run build

FROM node:lts-alpine as build-stage-admin

WORKDIR /homepage/admin

COPY admin/nhdream/package*.json .

RUN npm install

COPY admin/nhdream .

RUN npm run build 

FROM nginx:stable-alpine as production-stage

COPY --from=build-stage-front ./homepage/front/dist /usr/share/nginx/html/front

COPY --from=build-stage-admin ./homepage/admin/dist /usr/share/nginx/html/admin

CMD ["nginx", "-g", "daemon off;"]
```

[ 해당 도커 파일로 먼저 빌드하므로 admin 프로젝트까지 같이 빌드 해줍니다. ]

### 3-2 Admin

- Admin Dockerfile 작성

```yaml
FROM node:lts-alpine as build-stage

WORKDIR /homepage

COPY package*.json ./

RUN npm install

COPY . .

RUN npm run build

FROM nginx:stable-alpine as production-stage

COPY --from=build-stage ./homepage/dist /usr/share/nginx/html/

CMD ["nginx", "-g", "daemon off;"]
```

## 4. BackEnd

- Dockerfile 작성

```yaml
FROM openjdk:21

WORKDIR /app

ARG JAR_FILE=build/libs/nhdream-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} nhdream.jar

ENTRYPOINT ["java", "-jar", "nhdream.jar"]
```

- application-key.yml 작성 후 Jenkins Credential로 등록 및 로컬 resources 폴더에 넣기

```yaml
db:
  environment: server

local:
  db:
    driver: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost/nhdream?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: root
    password: {root 계정 비밀번호}

server:
  db:
    driver: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://k10s209.p.ssafy.io:3209/nhdream?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: root
    password: {root 계정 비밀번호}

s3:
  access-key: {s3 access 키}
  secret-key: {s3 secret 키}

redis:
  host: k10s209.p.ssafy.io
  port: 6379
  password: {redis 접근 비밀번호}

# mail smtp
mail:
  host: {호스트 주소}
  port: {지정되는 포트번호}
  username: {SMTP에 사용할 이메일}
  password: {비밀번호}

jwt:
  secret: {솔트에 넣을 암호 값}

openApi:
  serviceKey: {공공API secret 키}

NHDC:
  privateKey: "5d2f2a9e70b5be81fd1d5a552952d6dee41d6f94fc070d56127af9c4ae1dcdec"
  address: "0xF8628AF4F2E0DD5a27DE304D5A703aBd8FccB4cd "
  contractAddress: "0x08B35Bc9647123212d83295f53f944807c5024EE"
  interestWallet: "0x60b9aedAAd6DF40d75CeF59bb312d2bad1E9217D "

saving:
  contractAddress: "0x5616E82DCAAD5d74952694B22ED3aDD176e242c1"

redeposit:
  contractAddress: "0x168004EC648eb35c9B04aEe0C2d1A8D599588F33"

DRDC:
  contractAddress: "0xeD864b7B92783014712a2ab6C573f8a29B7429fA"

loan:
  contractAddress: "0x3628Be09244205Deb3952e9A86576036Dc1A967f"
```

- docker-compose.yml 작성

```yaml
services:
  nh_frontend:
    container_name: nhfrontend
    build:
      context: ./
      dockerfile: frontend/nh-dream/Dockerfile
    ports:
      - "80:80"                     
      - "443:443"
      - "1322:1322"
    volumes:
      - /etc/letsencrypt:/etc/letsencrypt
      - /var/lib/letsencrypt:/var/lib/letsencrypt
      - /home/ubuntu/conf:/etc/nginx/conf.d
    networks:
      - nh_network

  nh_backend:
    container_name: nhbackend
    build:
      context: ./backend/nhdream
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    networks:
      - nh_network
    environment:
      - TZ=Asia/Seoul

networks:
  nh_network:
    driver: bridge

```

- include 할 nginx.conf 파일 작성 (volume 마운트 되어있음)

```yaml
server {

        client_max_body_size 30M;

        listen 80;

        server_name k10s209.p.ssafy.io;

        if ($host = k10s209.p.ssafy.io) {
                return 301 https://$host$request_uri;
        }
}

server {

        client_max_body_size 30M;

        listen 1322;

        location / {
                root /usr/share/nginx/html/admin;
                index index.html index.htm;
                try_files $uri $uri/ /index.html;
        }

        location /api {
                proxy_pass http://nhbackend:8080;
        }
}

server {

        client_max_body_size 30M;

        listen 443 ssl;

        ssl_certificate /etc/letsencrypt/live/k10s209.p.ssafy.io/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/k10s209.p.ssafy.io/privkey.pem;

        location / {
                root /usr/share/nginx/html/front;
                index index.html index.htm;
                try_files $uri $uri/ /index.html;
        }

        location /api {
                proxy_pass http://nhbackend:8080;
        }

        location /api/sse {
                proxy_set_header Connection '';
                proxy_set_header Content_Type 'text/event-stream';
                proxy_set_header Cache-Control 'no-cache';
                proxy_set_header X-Accel-Buffering 'no';
                proxy_buffering off;
                proxy_read_timeout 86400s;
                proxy_http_version 1.1;
                chunked_transfer_encoding on;
                proxy_pass http://nhbackend:8080;
        }
}
```

## 5. MySQL 컨테이너 설치

- MySQL 이미지 다운로드

```bash
docker pull mysql:latest
```

- docker-compose.mysql.yml 작성
    - docker compose 버전이 올라가면서 `version:` 옵션 사라짐

```yaml
services:
  database:
    container_name: mysql
    image: mysql:latest
    restart: unless-stopped
    environment:
      MYSQL_DATABASE: nhdream
      MYSQL_ROOT_HOST: '%'
      MYSQL_ROOT_PASSWORD: {데이터 베이스의 root 계정 비밀 번호}
      TZ: 'Asia/Seoul'
    ports:
      - "3209:3306"
    volumes:
      - /home/ubuntu/mysql/conf.d:/etc/mysql/conf.d
    command:
      - "mysqld"
      - "--character-set-server=utf8mb4"
      - "--collation-server=utf8mb4_unicode_ci"
      - "--max_connections=500"
```

- MySQL 컨테이너 빌드

```bash
docker compose ./mysqlCompose/docker-compose.mysql.yml up -d --build
```

## 6. Redis 컨테이너 설치

- Redis 이미지 다운로드

```bash
docker pull redis:latest
```

- docker-compose.redis.yml 작성

```yaml
services:
  redis:
    container_name: redis
    image: redis:latest
    restart: unless-stopped
    hostname: redis
    command:
      --requirepass nhdream
    ports:
      - "6379:6379"
    labels:
      - "name=redis"
      - "mode=standalone"
```

- Redis 컨테이너 빌드

```bash
docker compose -f ./redisCompose/docker-compose.redis.yml up -d --build
```

## 7. Ganache-cli 컨테이너 설치

- Dockerfile 작성

```docker
FROM node:latest

USER root

WORKDIR /app

EXPOSE 8545

RUN npm install -g ganache-cli@latest

CMD ["ganache-cli", "-h", "0.0.0.0", "-a", "8", "-g", "0"]
```

[ account 8개, 가스비 0원 옵션 추가 ]

- docker-compose.ganache.yml 작성

```yaml
services:
  ganache:
    container_name: ganache
    restart: unless-stopped
    build:
      context: ./
      dockerfile: Dockerfile
    ports:
      - 8545:8545
    environment:
      - TZ=Asia/Seoul
    volumes:
      - /ganache_data:/ganache_data
```

- Redis 컨테이너 빌드

```bash
docker compose -f ./ganacheCompose/docker-compose.ganache.yml up -d --build
```

---

## 3. 연결

## Jenkins 내부 설정

- GitLab 플러그인 설정

![Jenkins내부설정1](https://github.com/timber3/CICD_test/assets/75405129/8f90f227-3638-455a-a9bd-4adfea4b96f6)

- develop 브랜치에 Web Hook Trigger 설정

![Jenkins내부설정2](https://github.com/timber3/CICD_test/assets/75405129/91063df2-6e3c-4609-9a65-300deed00768)

- Jenkins Credential 설정 필요 (환경 설정 보안)

![Jenkins내부설정3](https://github.com/timber3/CICD_test/assets/75405129/a6a9bd61-d2c9-4fb3-8511-6e6a965a05fb)

```bash
1. GitLab Respository Webhook Token
2. GitLab User Token
3. NH_BackEnd application-key.yml file
4. NH_FrontEnd .env file
```

- pipeline 작성

```bash
pipeline {
    
    agent any
    
    stages {
        
        stage('git clone') {
            steps {
                echo "========= git clone ========="
                echo pwd
                git branch: 'develop', credentialsId: 'timberToken', url: 'https://lab.ssafy.com/s10-final/S10P31S209.git'
                // sh "sed -i 's/{docker-compose.yml에서 암호화된 JASYPT_KEY}/{암호화 전 키}/g' docker-compose.yml"
                
            }
        }
        
        stage('BE Build') {
            steps {
                echo "========= env injection ========="
                echo pwd
                dir('frontend/nh-dream') {
                    withCredentials([file(credentialsId: 'nh_env', variable: 'key')]) {
                        sh 'cp ${key} .env'
                    }    
                }
                
                echo "========= BE Build ========="
                echo pwd
                dir('backend/nhdream') {
                    // application key 주입
                    withCredentials([file(credentialsId: 'nh_application_key', variable: 'key')]) {
                        sh 'cp ${key} src/main/resources/application-key.yml'
                    }
                    sh "chmod +x gradlew"
                    sh "./gradlew clean"
                    sh "./gradlew build"
                }
            }
        }
        
        stage('Deploy') {
            steps {
                echo "========= Deploy ========="
                echo pwd
                
                sh "docker stop nhbackend || true"
                sh "docker rm nhbackend || true"
                
                sh "docker stop nhfrontend || true"
                sh "docker rm nhfrontend || true"
            
                
                // sh "docker compose build --no-cache"
                sh "docker compose up -d --build"
            }
        }
        
        stage('clean overlay') {
            steps {
                echo "========= clean overlay ========="
                sh 'docker system prune --volumes --filter "label!=keep"'
            }
        }
    }
    post {
        success {
        	script {
                def Author_ID = sh(script: "git show -s --pretty=%an", returnStdout: true).trim()
                def Author_Name = sh(script: "git show -s --pretty=%ae", returnStdout: true).trim()
                
                mattermostSend (color: 'good', 
                message: "빌드 성공 ^_^ v : ${env.JOB_NAME} #${env.BUILD_NUMBER} by ${Author_ID}(${Author_Name})\n(<${env.BUILD_URL}|Details>)", 
                endpoint: 'https://meeting.ssafy.com/hooks/7ectsgq843nfpd11dha1py3d8r', 
                channel: 's209-cicd'
                )
            }
        }
        failure {
        	script {
                def Author_ID = sh(script: "git show -s --pretty=%an", returnStdout: true).trim()
                def Author_Name = sh(script: "git show -s --pretty=%ae", returnStdout: true).trim()
                
                mattermostSend (color: 'danger', 
                message: "빌드 실패 ㅜ_ㅜ : ${env.JOB_NAME} #${env.BUILD_NUMBER} by ${Author_ID}(${Author_Name})\n(<${env.BUILD_URL}|Details>)", 
                endpoint: 'https://meeting.ssafy.com/hooks/7ectsgq843nfpd11dha1py3d8r', 
                channel: 's209-cicd'
                )
            }
        }
    }
}
```

- 추가적으로 할 설정

```bash
1. Jenkins 시간대 설정
2. 다크모드 플러그인 설치
```

### **(선택) Jenkins Plugin 받을 미러서버 변경**

[ Jenkins Plugins 설치가 안되면 해당 설정 추가 필요 ]

- Jenkins 데이터가 있는 디렉토리에 업데이트 데이터 센터 변경

```bash
sudo mkdir ./jenkins/update-center-rootCAs
```

- CA 파일 다운로드

```bash
sudo wget https://cdn.jsdelivr.net/gh/lework/jenkins-update-center/rootCA/update-center.crt -O ./jenkins/update-center-rootCAs/update-center.crt
```

- Jenkins plugins 다운로드 할 때 사용할 데이터 미러사이트 변경

```bash
sudo sed -i 's#https://updates.jenkins.io/update-center.json#https://raw.githubusercontent.com/lework/jenkins-update-center/master/updates/tencent/update-center.json#' ./jenkins/hudson.model.UpdateCenter.xml
```

- Jenkins 컨테이너 재시작 필요

```bash
sudo docker restart jenkins
```

- 기본 설치 이외 추가 할 Jenkins Plugins

```bash
Pipeline Graph View
GitLab
```

[ Jenkins Plugins 설치가 안되면 위 설정 추가 필요 ]

---

## 4. 빌드 및 배포

- 컨테이너 상태 확인

```bash
docker ps -a
```

![Build 사진](https://github.com/timber3/CICD_test/assets/75405129/eee28081-9246-463e-b662-63d0444f9c4c)

[ 각 컨테이너의 STATUS 가 UP 인지 확인 ]
