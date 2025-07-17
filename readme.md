# 0. 프로젝트 소개  

> **삼육대학교 KDT 2차 프로젝트** <br/> **개발기간: 2025.05 ~ 2025.07**

**프로젝트명** : **와따잇(WTE)**

📃 *주문을 넘어서, 상점 운영자와 고객 모두에게 가치 있는 경험을 제공하는 플랫폼* <br><br>
<img width="1387" height="780" alt="Image" src="https://github.com/user-attachments/assets/5726a7e0-ebaf-42a7-afbc-8f3171510661" />

- 위치 기반 매점 검색과 실시간 리뷰 시스템을 통해 고객이 주변에서 최고의 선택을 할 수 있도록 돕습니다.
- 사장님들에게는 단순한 주문 도구를 넘어, 비즈니스 성장을 위한 다양한 운영 지원 기능을 제공합니다.
- 고객의 긍정적인 리뷰가 또 다른 고객을 유입시키고, 이는 매장의 성장으로 이어지는 선순환 구조를 만드는 것이 본 프로젝트의 핵심입니다.

<br>

# 1. 주요 기능

### 🤖 AI 기반 메뉴 추천 및 분석

데이터에 기반한 의사결정으로 매장 운영을 혁신합니다.

-   **리뷰 감성 분석 및 시각화**  
    고객 리뷰를 긍정/부정 키워드로 가중치를 매겨 분석합니다. 이를 통해 메뉴별 만족도를 직관적인 데이터로 시각화하여, 어떤 메뉴가 사랑받고 개선이 필요한지 명확하게 파악할 수 있습니다.

-   **상황 맞춤형 AI 메뉴 컨설팅**  
    분석된 리뷰 데이터와 실시간 날씨, 지역 특성 등을 AI(`Gemini`)가 종합적으로 판단합니다. 현재 상황에 가장 적합한 오늘의 추천 메뉴를 제안하고, 나아가 성공 확률이 높은 신메뉴 아이디어까지 제공하여 사장님의 든든한 비즈니스 파트너가 되어줍니다. 또한 리뷰가 좋지 않은 메뉴들을 분석하여 구체적인 개선 방안과 해결책을 제시함으로써 전체적인 매장 운영 품질 향상을 지원합니다.

### 📱 스마트 QR 메뉴판 & 리뷰 시스템

인쇄물 교체의 번거로움 없이, 고객과 실시간으로 소통하는 디지털 메뉴판을 제공합니다.

-   **사장님을 위한 원스톱 메뉴 관리**  
    회원가입 즉시 매장만의 고유 QR코드가 자동 생성됩니다. 사장님은 언제 어디서든 메뉴 정보(가격, 사진, 품절 여부 등)를 수정할 수 있으며, 변경 사항은 별도의 교체 없이 모든 고객의 메뉴판에 실시간으로 반영됩니다.

-   **고객을 위한 스마트한 주문 경험**  
    고객은 테이블의 QR코드 스캔 한 번으로 메뉴 확인, 다른 고객들의 실시간 리뷰 조회까지 한곳에서 해결할 수 있습니다. 풍부한 정보를 바탕으로 만족도 높은 주문을 할 수 있도록 돕습니다.

### 🗺️ 위치 기반 맛집 및 쿠폰 탐색

사용자 위치를 중심으로 주변의 맛집과 혜택을 한눈에 보여줍니다.

-   **내 주변 맛집과 생생한 리뷰 탐색**  
    `Geolocation API`를 활용해 현재 위치를 기반으로 가장 가까운 상점들을 지도 위에 표시합니다. 단순히 위치만 보여주는 것이 아니라, 다른 사용자들이 남긴 실시간 리뷰와 평점을 함께 제공하여 실패 없는 맛집 선택을 돕습니다.

-   **지도에서 바로 찾는 2km 내 할인 쿠폰**  
    현재 위치 기준으로 반경 2km 이내의 상점들과 상점들의 메뉴 리뷰, 해당 상점들이 제공하는 사용 가능한 할인 쿠폰을 즉시 확인할 수 있습니다. 현명하고 합리적인 소비를 위한 최고의 가이드가 되어줍니다.

    > **🤔 왜 2km 인가요?**  
    > 프로젝트를 기획할 때, '걸어서 갈 만한 맛집'의 기준을 고민했습니다. 저희가 학교 실습관에서 후문 식당 거리까지 걸어갈 때 보통 10분에서 15분 정도 걸렸는데, 이 거리가 약 1.5km 정도 되더라고요. 하지만 서비스 범위를 너무 좁게 설정하면 선택권이 제한되고, 너무 넓게 하면 걸어가기 부담스러워집니다. 성인 평균 도보 속도를 고려했을 때 2km는 25분 내외로 '점심 뭐 먹지?' 할 때 부담 없이 고려할 수 있는 '슬세권(슬리퍼 신고 갈 수 있는 상권)'의 현실적인 범위라고 판단해서 2km로 설정하게 되었습니다.
    

# 2. 페이지별 기능

> 각 페이지에서 제공하는 핵심 기능 목록입니다.

### 🏪 사장님 페이지

사장님은 매장과 메뉴, 쿠폰을 관리하고 AI 분석을 통해 매출 증대를 위한 인사이트를 얻을 수 있습니다.

| 기능                  | 설명                                                   |
|:--------------------|:-----------------------------------------------------|
| **매장 관리**           | 매장 정보(이름, 주소, 카테고리 등)를 등록하고 수정합니다.                   |
| **메뉴 관리**           | 메뉴 등록, 수정, 품절 처리 및 메뉴별 상태 변경이 가능합니다.                 |
| **쿠폰 관리**           | 타겟 고객층에게 맞춤형 쿠폰을 발행하고 사용 현황을 추적합니다.                  |
| **리뷰 관리**           | 고객 리뷰의 통계를 확인하고 AI 감성 분석 결과를 모니터링합니다.                |
| **AI 추천**           | 위치, 날씨, 메뉴 리뷰 등을 종합적으로 고려한 맞춤형 메뉴 추천 및 경영 컨설팅을 받습니다. |
| **주문/매출 통계**        | 실시간 주문 현황을 확인하고, 기간별/메뉴별 매출 통계를 분석합니다.               |
| **날씨 조회 & 추천 히스토리** | 현재 날씨 정보를 확인하고, 과거 AI 추천 히스토리를 조회하여 패턴을 분석합니다.       |

<br>

<details>
<summary><strong>사장님 페이지 주요 기능 상세보기 (클릭)</strong></summary>

#### 1. 상점 관리
- 메뉴 이미지, 이름, 가격, 설명을 입력하여 새로운 메뉴를 등록할 수 있습니다.
- 메뉴 상태를 손쉽게 변경하여, 매장에 적합한 메뉴를 자유롭게 생성할 수 있습니다.
- 'On/Off' 토글 버튼으로 실시간 재고 상황을 고객에게 즉시 알릴 수 있습니다.

| 메뉴 관리 시연 |
| :------------------: |
| ![Image](https://github.com/user-attachments/assets/c026a8a8-19f5-4105-89f5-9ee7855ad733) |

<br>

#### 2. AI 리뷰 분석 및 맞춤 추천
- 고객이 남긴 모든 리뷰를 AI가 자동으로 분석하여 긍정/부정 스코어를 부여합니다.
- 메뉴별 리뷰를 차트로 확인하여 어떤 메뉴의 만족도가 높은지 직관적으로 파악할 수 있습니다.
- 현재 날씨와 위치 특성을 고려한 AI 메뉴 추천을 받아 매출 증대 기회를 놓치지 않습니다.

|                                        AI 리뷰 분석 시연                                        |
|:-----------------------------------------------------------------------------------------:|
| ![Image](https://github.com/user-attachments/assets/a1131503-01b8-4a12-8fcb-9826894455cb) |

<br>

#### 3. 실시간 주문 및 매출 통계
- 실시간으로 들어오는 주문을 확인하고 처리 상태를 업데이트할 수 있습니다.
- 일별, 주별, 월별 매출 현황을 확인하여 비즈니스 성과를 추적합니다.
- 메뉴별 판매량과 수익률을 분석하여 전략적인 메뉴 기획이 가능합니다.

|                                          통계 대시보드                                          |
|:-----------------------------------------------------------------------------------------:|
| ![Image](https://github.com/user-attachments/assets/aa9966e4-b95c-481d-9ca7-a9e6a5c10a6b) |

</details>

<br>

### 👤 고객 페이지

고객은 위치 기반으로 맛집을 탐색하고, QR코드를 통해 간편하게 주문과 리뷰 작성을 할 수 있습니다.

| 기능           | 설명                                                |
|:-------------|:--------------------------------------------------|
| **위치 기반 검색** | 지도에서 내 주변 상점을 찾고, 다른 사용자들의 리뷰를 확인합니다.             |
| **QR 주문/결제** | 테이블의 QR코드를 스캔하여 메뉴를 확인하고, 카카오페이로 간편하게 주문 및 결제합니다. |
| **리뷰 작성**    | 주문한 메뉴에 대한 경험을 별점과 텍스트로 기록하고 공유합니다.               |
| **쿠폰/포인트**   | 보유한 쿠폰과 포인트를 확인하고, 주문 시 사용하여 할인을 받습니다.            |
| **소셜 로그인**   | 카카오, 네이버 계정을 통해 간편하게 로그인하고 서비스를 이용합니다.            |

<br>

<details>
<summary><strong>고객 페이지 주요 기능 상세보기 (클릭)</strong></summary>

#### 1. 위치 기반 맛집 탐색
- 페이지 접속 시 현재 위치를 기반으로 주변 가게들을 리스트로 표시합니다.
- 2km 내 사용 가능한 쿠폰 정보도 함께 제공하여 합리적인 선택을 도와줍니다.

|                                        위치 기반 탐색 시연                                        |
|:-----------------------------------------------------------------------------------------:|
| ![Image](https://github.com/user-attachments/assets/904cdf4a-1b8a-44d6-9b2a-50ed0df80925) |

<br>

#### 2. QR 주문 및 결제
- 테이블의 QR코드를 스캔하면 해당 가게의 메뉴판이 즉시 나타납니다.
- 포인트나 쿠폰을 사용하여 할인된 가격으로 주문할 수 있습니다.
- 장바구니에 메뉴를 담고 '카카오페이로 결제하기' 버튼을 누르면 간편하게 결제가 완료됩니다.


|                                         QR 주문 시연                                          |
|:-----------------------------------------------------------------------------------------:|
| ![Image](https://github.com/user-attachments/assets/59060dd0-8cdd-4a3c-9e9f-d349c9952aa5) |

<br>

#### 3. 리뷰 작성 및 포인트 적립
- 주문한 메뉴에 대해 별점과 텍스트 리뷰를 작성할 수 있습니다.
- 작성한 리뷰는 다른 고객들에게 도움이 되며, AI 분석을 통해 사장님의 메뉴 개선에도 기여합니다.

|                                         리뷰 작성 시연                                          |
|:-----------------------------------------------------------------------------------------:|
| ![Image](https://github.com/user-attachments/assets/ddbb7e92-0b3a-4a4e-8501-bb578556f8a8) |

</details>

<br>

<br>

# 3. 팀원 구성 및 역할

|    ![김재현](https://github.com/kod0406.png)    | ![류재열](https://github.com/fbwoduf112.png?size=100) | ![이남현](https://github.com/hyun3138.png?size=100) |
|:--------------------------------------------:|:--------------------------------------------------:|:------------------------------------------------:|
| [**김재현**](https://github.com/kod0406)<br/>팀장 |  [**류재열**](https://github.com/fbwoduf112)<br/>팀원   |  [**이남현**](https://github.com/hyun3138)<br/>팀원   |

> **💡 팀워크 철학**  
> 모든 기능의 설계, 구현, 테스트 과정에서 팀원들과 긴밀히 협업하며 진행하였습니다.

### 👨‍💼 김재현 (팀장) - Backend Lead & System Architecture
- **총괄 및 아키텍처 설계**: 프로젝트 백엔드 구조 설계를 주도하고, `Redis`를 활용한 핵심 인증 및 보안 시스템을 구축했습니다.
- **사장님 기능 개발**: 매장 관리, 메뉴 등록, 쿠폰 발행 등 상점 운영의 핵심 기능을 개발했습니다.
- **고급 기능 및 외부 API 연동**: `Gemini` AI를 활용한 리뷰 감성 분석 및 메뉴 추천 시스템을 구현했습니다.
- **시스템 안정성 및 보안 강화**: 동시 로그인 방지, `Redis` 기반 세션/토큰 관리, `axios`를 통한 토큰 자동 재발급 등 보안 및 안정성을 강화했습니다.

### 👤 류재열 - Backend & Customer Service
- **아이디어 제공 및 아키텍처 설계**: 프로젝트 핵심 아이디어를 제안하고, DB 구조 설계를 주도했으며, `Spring Security`, `JWT` 기반의 인증 시스템을 구축했습니다.
- **고객 기능 개발**: 회원가입, 상품 조회, 장바구니, 주문, 리뷰 등 고객 사이드의 주요 기능을 개발했습니다.
- **결제 및 위치 기반 서비스**: `카카오페이` 결제 시스템과 `Geolocation API`를 연동한 위치 기반 상점 탐색 기능을 구현했습니다.
- **실시간 통신 및 인증**: `WebSocket`을 이용한 실시간 주문/알림 처리 및 소셜 로그인(카카오, 네이버) 기능을 구현했습니다.

### 🎨 이남현 - Frontend & UI/UX Design
- **프론트엔드 개발**: `React`와 `TypeScript`를 사용하여 프로젝트의 모든 사용자 인터페이스(UI)를 구현했습니다.
- **UI/UX 설계 및 기획**: 와이어프레임과 프로토타입을 제작하고, 전체 서비스의 UX 플로우를 설계하여 사용성을 개선했습니다.

<br>

# 4. 기술 스택 및 아키텍처

## 🛠️ 기술 스택
| 구분               | 주요 기술 스택                                                                       |
|:-----------------|:-------------------------------------------------------------------------------|
| **⚙️ Back-end**  | `Java` `Spring Boot` `JPA` `MySQL` `Redis` `AWS S3` `JWT` `OAuth 2.0`          |
| **🎨 Front-end** | `React` `TypeScript` `Axios` `Tailwind CSS`  `Npm`                             |
| **🚀 DevOps**    | `Docker` `GitHub Actions` `Nginx` `PM2` `AWS RDS` `AWS S3`                     |
| **🤝 협업 도구**     | `GitHub` `Notion` `Swagger` `Postman`                                          |
| **🔗 외부 API**    | `Kakao SDK` `Naver SDK` `Google Gemini` `OpenWeatherMap API` `GeoLocation API` |

## 📁 프로젝트 구조
```
Syu2_Back/
├── 📂 src/main/java/com/example/demo/
│   ├── 🎁 benefit/                  # 혜택(쿠폰, 포인트) 관리
│   ├── ⚙️ config/                   # WebClient 등 설정
│   ├── 👤 customer/                 # 고객 기능
│   ├── 🌐 external/                 # 외부 API 연동
│   ├── 📍 geolocation/              # 위치 기반 서비스
│   ├── 💳 KakaoPay/                 # 카카오페이 결제
│   ├── 🛒 order/                    # 주문 관리
│   ├── 🤖 recommendation/           # 메뉴 추천 및 분석
│   ├── 📊 setting/                  # Spring Security, JWT, WebSocket 등
│   ├── 🔐 socialLogin/              # 소셜 로그인
│   ├── 🏪 store/                    # 상점 관리
│   └── 👥 user/                     # 사용자 공통
│
├── 📂 src/main/resources/
│   ├── application.properties       # 환경 설정
│   └── logback-spring.xml           # 로깅 설정
│
├── 📂 sequence/                     # 시퀀스 다이어그램
├── 📂 diagram/                      # 아키텍처 다이어그램
├── 📄 build.gradle                  # 빌드 스크립트
└── 📄 README.md                     # 프로젝트 문서
```

<br>

# 5. WTE 프로젝트 실행 가이드

>WTE 프로젝트를 로컬 환경에서 설정하고 실행하기 위한 가이드입니다.


## 📝 목차

1.  [시스템 요구사항](#시스템-요구사항)
2.  [실행 방법](#실행-방법)
    -   [Back-end](#1-back-end-실행)
    -   [Front-end](#2-front-end-실행)
3.  [데이터베이스 접속](#데이터베이스-접속-mysql-workbench)
4.  [API 문서 확인](#api-문서-swagger-ui)
5.  [환경 변수 설정](#환경-변수-설정-applicationproperties)

<br>

##  시스템 요구사항

프로젝트 실행을 위해 아래 환경을 구성해야 합니다.

### 🔧 필수 실행 환경

| 구성 요소      | 버전        | 비고              |
|:-----------|:----------|:----------------|
| **JDK**    | `17` 이상   | Java 개발 키트      |
| **Gradle** | `7.x` 이상  | 빌드 도구           |
| **npm**    | `10.x` 이상 | Node.js 패키지 매니저 |
| **MySQL**  | `8.x` 이상  | 관계형 데이터베이스      |
| **Redis**  | `6.x` 이상  | 인메모리 데이터베이스     |
| **Docker** | `20.x` 이상 | 컨테이너 환경 (선택사항)  |

<br>

### 📚 주요 의존성 라이브러리

프로젝트는 다음과 같은 핵심 라이브러리들을 기반으로 구성되어 있습니다.

**🌟 Spring Boot Ecosystem**
- Spring Boot Starter (Web, Data JPA, Security, WebSocket, Validation, Cache, Mail, Quartz)
- Spring Cloud AWS (S3 연동)
- Spring Security Test

**🗄️ Database & Storage**
- MySQL Connector/J
- Redis
- AWS S3

**🔐 인증 & 보안**
- JJWT (JWT 토큰 처리)
- Spring Security

**🎨 UI & View**
- React
- Swagger (springdoc-openapi) - API 문서화

**🔧 Utility Libraries**
- Lombok (코드 간소화)
- ZXing (QR코드 생성)
- Jackson-datatype-jsr310 (JSON 날짜 처리)
- WebFlux (비동기 처리)

**🧪 테스트 & 개발**
- JUnit (단위 테스트)
- Spring Security Test (보안 테스트)

> 📋 **상세 의존성 정보**  
> 전체 라이브러리 목록과 버전 정보는 프로젝트 루트의 `build.gradle` 파일을 참고하세요.

##  실행 방법

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
2.  아래의 [환경 변수 설정](#환경-변수-설정-applicationproperties) 섹션을 참고하여 본인의 로컬 환경에 맞게 파일 내용을 채워넣습니다.

#### 애플리케이션 실행 (2가지 방법)

**방법 1: Gradle 명령어 사용**

```bash
# 1. 프로젝트 빌드 (Windows)
./gradlew.bat clean build

# 1. 프로젝트 빌드 (Mac/Linux)
./gradlew clean build

# 2. 애플리케이션 실행
./gradlew bootRun
```

**방법 2: JAR 파일 직접 실행**

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
> 실행 후 브라우저에서 `http://localhost:3000/owner/login` 주소로 접속하세요.

<br>

## 데이터베이스 접속 (MySQL Workbench)

MySQL Workbench를 사용하여 원격 RDS 또는 로컬 DB에 접속하고 관리하는 방법입니다.

#### 1. 새 연결 생성

MySQL Workbench 홈 화면에서 `+` 버튼을 눌러 새 연결을 생성합니다.

#### 2. 연결 정보 입력

아래 표를 참고하여 접속 정보를 입력하세요.

| 항목                    | 값                                 | 비고                         |
|-----------------------|-----------------------------------|----------------------------|
| **Connection Name**   | `WTE_RDS`                         | 원하는 이름으로 자유롭게 설정           |
| **Connection Method** | `Standard (TCP/IP)`               | 기본값                        |
| **Hostname**          | `[호스트명 입력]`                       | 예: `your-rds-endpoint.com` |
| **Port**              | `3306`                            | 기본값                        |
| **Username**          | `[사용자명 입력]`                       | 예: `root` 또는 `admin`       |
| **Password**          | `Store in Keychain...` 버튼 클릭 후 입력 | `[비밀번호 입력]`                |

#### 3. 연결 테스트 및 저장

1.  **Test Connection** 버튼을 클릭하여 연결을 테스트합니다.
2.  비밀번호를 입력하라는 창이 뜨면 `[비밀번호 입력]` 값을 넣고 `OK`를 누릅니다.
3.  "Successfully made the MySQL connection" 메시지가 보이면 `OK`를 눌러 연결을 저장합니다.
4.  생성된 연결을 더블클릭하여 접속 후, `[DB이름 입력]` 스키마를 사용합니다.

<br>

## API 문서 (Swagger UI)

백엔드 애플리케이션이 실행 중일 때, 아래 주소로 접속하여 API 명세를 확인하고 테스트할 수 있습니다.

*   [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
*   [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

<br>

## 환경 변수 설정 (`application.properties`)

백엔드 프로젝트의 `src/main/resources/` 경로에 `application.properties` 파일을 생성하고, 아래 예시를 바탕으로 자신의 키와 정보를 입력해야 합니다.

<details>
<summary><strong>전체 환경 변수 예시 보기</strong></summary>

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
recommend.cache.recent-check-minutes=5
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

---

## 📁 프로젝트 산출물

프로젝트 기획부터 최종 발표까지의 모든 산출물은 아래 링크들에서 확인하실 수 있습니다.

**주요 포함 내용:**
-   기획서 및 요구사항 명세서
-   시스템 아키텍처 다이어그램
-   ERD (Entity-Relationship Diagram)
-   최종 발표 자료 (PPT) 등등...

<br>

> **[📂 WTE 프로젝트 산출물 바로가기 (Google Drive)](https://drive.google.com/drive/folders/13XoP2tkQ-fceQ-eqdr7uzskVRE9ba0Z7?hl=ko)**

**UI/UX 디자인:**
> **[🎨 Figma 디자인 시안 바로가기](https://www.figma.com/design/zBE64TmGfzZDdRQjm9fTIU/KDT-2%EC%B0%A8?node-id=0-1&t=der6O9hqjh5SLDZN-1)**

---
