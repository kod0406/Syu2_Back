# 0. 프로젝트 소개  
**프로젝트명** : **와따잇(WTE)**

📃 *주문을 넘어서, 상점 운영자와 고객 모두에게 가치 있는 경험을 제공하는 플랫폼* <br><br>
<img width="1387" height="780" alt="Image" src="https://github.com/user-attachments/assets/5726a7e0-ebaf-42a7-afbc-8f3171510661" />


- 위치 기반 매점 검색과 실시간 리뷰 시스템을 통해 고객이 주변에서 최고의 선택을 할 수 있도록 돕습니다.
- 사장님들에게는 단순한 주문 도구를 넘어, 비즈니스 성장을 위한 다양한 운영 지원 기능을 제공합니다.
- 고객의 긍정적인 리뷰가 또 다른 고객을 유입시키고, 이는 매장의 성장으로 이어지는 선순환 구조를 만드는 것이 본 프로젝트의 핵심입니다.

<br>

## 🌟 팀원 구성  
| ![김재현](https://github.com/kod0406.png) | ![류재열](https://github.com/fbwoduf112.png?size=100) | ![이남현](https://github.com/hyun3138.png?size=100) |
| :------: |  :------: | :------: |
| [**김재현**](https://github.com/kod0406)<br/>팀장 | [**류재열**](https://github.com/fbwoduf112)<br/>팀원 | [**이남현**](https://github.com/hyun3138)<br/>팀원 |

<br>

## 1. 프로젝트 기술 스택
| 구분              | 주요 기술 스택                                                                       |
| :---------------- |:-------------------------------------------------------------------------------|
| **⚙️ Back-end**   | `Java` `Spring Boot` `JPA` `MySQL` `Redis` `AWS S3` `JWT` `OAuth 2.0`          |
| **🎨 Front-end**  | `React` `TypeScript` `Axios` `Tailwind CSS`                                    |
| **🚀 DevOps**     | `Docker` `GitHub Actions` `Nginx` `PM2` `AWS RDS` `AWS S3`                     |
| **🤝 협업 도구**  | `GitHub` `Notion` `Swagger` `Postman`                                          |
| **🔗 외부 API**   | `Kakao SDK` `Naver SDK` `Google Gemini` `OpenWeatherMap API` `GeoLocation API` |

# 2. WTE 프로젝트 실행 가이드

>WTE 프로젝트를 로컬 환경에서 설정하고 실행하기 위한 가이드입니다.


## 📝 목차

1.  [시스템 요구사항](#-시스템-요구사항)
2.  [실행 방법](#-실행-방법)
    -   [Back-end](#1-back-end-실행)
    -   [Front-end](#2-front-end-실행)
3.  [데이터베이스 접속](#-데이터베이스-접속-mysql-workbench)
4.  [API 문서 확인](#-api-문서-swagger-ui)
5.  [환경 변수 설정](#️-환경-변수-설정-applicationproperties)

<br>

## 💻 시스템 요구사항

프로젝트 실행을 위해 아래 환경을 구성해야 합니다.

*   **JDK**: `17` 이상
*   **Gradle**: `7.x` 이상
*   **npm**: `10.x` 이상

<br>

## 🏃‍♂️ 실행 방법

### 1. Back-end 실행

#### Git에서 프로젝트 가져오기

```bash
# 1. 프로젝트를 클론합니다.
git clone https://github.com/[Your-GitHub-ID]/Syu2_Back.git

# 2. 프로젝트 디렉토리로 이동합니다.
cd Syu2_Back
```

#### 환경 설정

1.  `src/main/resources/` 경로에 `application.properties` 파일을 생성합니다.
2.  아래의 [환경 변수 설정](#️-환경-변수-설정-applicationproperties) 섹션을 참고하여 본인의 로컬 환경에 맞게 파일 내용을 채워넣습니다.

#### 애플리케이션 실행 (2가지 방법)

**방법 A: Gradle 명령어 사용**

```bash
# 1. 프로젝트 빌드 (Windows)
./gradlew.bat clean build

# 1. 프로젝트 빌드 (Mac/Linux)
./gradlew clean build

# 2. 애플리케이션 실행
./gradlew bootRun
```

**방법 B: JAR 파일 직접 실행**

```bash
# 빌드된 JAR 파일을 직접 실행합니다.
java -jar build/libs/WTE-project-0.0.1-SNAPSHOT.jar
```

> ℹ️ **서버 접속**
>
> 백엔드 서버는 기본적으로 `http://localhost:8080` 에서 실행됩니다.

### 2. Front-end 실행

#### Git에서 프로젝트 가져오기

```bash
# 1. 프로젝트를 클론합니다.
git clone https://github.com/[Your-GitHub-ID]/Syu2_Front.git

# 2. 프로젝트 디렉토리로 이동합니다.
cd Syu2_Front
```

#### 개발 서버 실행

```bash
# 1. React 앱 디렉토리로 이동합니다.
cd src/my-app

# 2. 필요한 라이브러리를 설치합니다.
npm install

# 3. 개발 서버를 실행합니다.
npm start
```

> ℹ️ **화면 접속**
>
> 실행 후 브라우저에서 `http://localhost:3000` 주소로 접속하세요.

<br>

## 🗄️ 데이터베이스 접속 (MySQL Workbench)

MySQL Workbench를 사용하여 원격 RDS 또는 로컬 DB에 접속하고 관리하는 방법입니다.

#### 1. 새 연결 생성

MySQL Workbench 홈 화면에서 `+` 버튼을 눌러 새 연결을 생성합니다.

#### 2. 연결 정보 입력

아래 표를 참고하여 접속 정보를 입력하세요.

| 항목                | 값                               | 비고                           |
| ------------------- | -------------------------------- | ------------------------------ |
| **Connection Name** | `WTE_RDS`                        | 원하는 이름으로 자유롭게 설정     |
| **Connection Method**| `Standard (TCP/IP)`              | 기본값                         |
| **Hostname**        | `[호스트명 입력]`                | 예: `your-rds-endpoint.com`    |
| **Port**            | `3306`                           | 기본값                         |
| **Username**        | `[사용자명 입력]`                | 예: `root` 또는 `admin`        |
| **Password**        | `Store in Keychain...` 버튼 클릭 후 입력 | `[비밀번호 입력]`              |

#### 3. 연결 테스트 및 저장

1.  **Test Connection** 버튼을 클릭하여 연결을 테스트합니다.
2.  비밀번호를 입력하라는 창이 뜨면 `[비밀번호 입력]` 값을 넣고 `OK`를 누릅니다.
3.  "Successfully made the MySQL connection" 메시지가 보이면 `OK`를 눌러 연결을 저장합니다.
4.  생성된 연결을 더블클릭하여 접속 후, `[DB이름 입력]` 스키마를 사용합니다.

<br>

## 📖 API 문서 (Swagger UI)

백엔드 애플리케이션이 실행 중일 때, 아래 주소로 접속하여 API 명세를 확인하고 테스트할 수 있습니다.

*   [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
*   [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

<br>

## ⚙️ 환경 변수 설정 (`application.properties`)

백엔드 프로젝트의 `src/main/resources/` 경로에 `application.properties` 파일을 생성하고, 아래 예시를 바탕으로 자신의 키와 정보를 입력해야 합니다.

<details>
<summary><strong>클릭하여 전체 환경 변수 예시 보기</strong></summary>

```properties
# ====================================
# 서버 기본 설정
# ====================================
# 서버 포트
env.server.port=8080

# Jackson 설정 (Lazy Loading 관련 오류 방지)
spring.jackson.serialization.FAIL_ON_EMPTY_BEANS=false
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true

# ====================================
# 데이터베이스 (MySQL)
# ====================================
spring.datasource.url=jdbc:mysql://[호스트명]:3306/[DB이름]?useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=[DB 사용자명]
spring.datasource.password=[DB 비밀번호]

# ====================================
# Redis
# ====================================
spring.data.redis.host=[Redis 호스트]
spring.data.redis.port=6379
spring.data.redis.password=[Redis 비밀번호]

# ====================================
# JWT (JSON Web Token)
# ====================================
jwt.secret=[나만의 JWT 시크릿 키]

# ====================================
# OAuth 2.0 (소셜 로그인)
# ====================================
# 카카오
kakao.client_id=[카카오 REST API 키]
kakao.redirect_uri=http://localhost:8080/OAuth2/login/kakao

# 네이버
naver.client_id=[네이버 애플리케이션 클라이언트 ID]
naver.client_secret=[네이버 애플리케이션 클라이언트 시크릿]
naver.redirect_uri=http://localhost:8080/login/naver

# ====================================
# 외부 서비스 API 키
# ====================================
# 카카오페이
kakaopay.secretKey=[카카오페이 Secret Key (dev)]
kakaopay.cid=TC0ONETIME

# 네이버 클라우드 (SENS 등)
naver.cloud.AccessKey=[네이버 클라우드 Access Key]
naver.cloud.SecretKey=[네이버 클라우드 Secret Key]

# AWS S3 (파일 스토리지)
cloud.aws.credentials.accessKey=[AWS IAM Access Key]
cloud.aws.credentials.secretKey=[AWS IAM Secret Key]
cloud.aws.s3.bucketName=[S3 버킷 이름]
cloud.aws.region.static=ap-northeast-2

# 날씨 API (OpenWeatherMap)
weather.api.key=[OpenWeatherMap API 키]

# Gemini API (AI)
gemini.api.key=[Google Gemini API 키]
gemini.api.url=https://generativelanguage.googleapis.com/v1beta/models/[모델명]:generateContent

# ====================================
# 스케줄러 및 캐시 설정
# ====================================
# 추천 스케줄러 활성화
recommendation.scheduler.enabled=true
# 추천 스케줄러 크론 표현식
recommendation.scheduler.cron=0 0 * * * ?
# 캐시 유지 시간(초)
recommendation.cache.duration=3600
# 캐시 키 생성 단위 (분 단위)
recommendation.cache.key-unit-minutes=60
# 최근 체크 단위 (분 단위)
recommendation.cache.recent-check-minutes=5
# 쿠폰 정리 스케줄러 크론 표현식
coupon.cleanup.cron=0 0 0,12 * * ?

# ====================================
# SMTP (메일 발송)
# ====================================
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=[발송용 Gmail 계정]
spring.mail.password=[Gmail 앱 비밀번호]

# ====================================
# 리뷰 감정 분석 설정
# ====================================
# 키워드
sentiment.positive.basic=맛있,좋,추천,훌륭,완벽,최고,만족,괜찮,신선,깔끔,부드럽,촉촉,바삭,고소,달콤,진짜
sentiment.positive.strong=개맛,존맛,대박,짱,레전드,갓,핵맛,꿀맛,JMT,인정,감동,놀라,우와,와
sentiment.positive.revisit=또 오고 싶,또 가고 싶,재방문,단골,자주 갈,꼭 가세요,또 올게,다시 올게
sentiment.positive.service=친절,빠르,깨끗,분위기 좋,가성비,합리적,저렴,혜자
sentiment.negative.basic=별로,아쉽,실망,나쁘,최악,불만,그냥,soso,애매,아니,글쎄
sentiment.negative.strong=끔찍,혐오,역겨,토나,쓰레기,돈아까,후회,짜증,화나,빡쳐
sentiment.negative.revisit=다시는,두 번 다시,절대 안,안 갈,못 갈,가지 마세요
sentiment.negative.taste=맛없,짜,싱거,느끼,비려,퍽퍽,딱딱,눅눅,비싸,바가지
sentiment.negation.patterns=안,않,못,절대,전혀,별로,그다지,다시는,두 번 다시

# 임계값 및 가중치
sentiment.threshold.positive=0.3
sentiment.threshold.negative=-0.3
sentiment.weight.basic=1.0
sentiment.weight.strong=2.0
sentiment.weight.special=2.0
sentiment.weight.negation.strong=1.0
sentiment.weight.negation.basic=0.5
sentiment.negation.search.range=5
```
</details>

