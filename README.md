# CidrValidator

## Overview
**CIDR Validator**는 네트워크 설정에서 CIDR(Classless Inter-Domain Routing) 표기법의 검증, 대역 계산, 정규화 요구사항을 충족하기 위해 개발된 Java 오픈 소스 라이브러리입니다. CSP(클라우드 서비스 제공자) 네트워크 장비 설정 과정에서 CIDR 중복, 대역 불일치, 비표준 형식으로 인한 오류를 체계적으로 해결하고자 만들어졌습니다.

## 🚀 개발 현황

### ✅ 구현 완료된 기능
- [x] **CIDR 정규화**: 비표준 CIDR을 표준 형식으로 변환
  - 예: `192.168.1.1/24` → `192.168.1.0/24`
- [x] **CIDR/IP 검증**: 유효한 CIDR 및 IP 주소 형식 검증
- [x] **기본 클래스 구조**: `CidrBlock`, `IpAddress` 클래스

### 🔄 개발 예정 기능
- [ ] **IP 포함 여부 검사**: IP가 CIDR 대역 내 포함되는지 검사
- [ ] **CIDR 대역 IP 목록 조회**: CIDR의 사용 가능 IP 목록 반환
- [ ] **CIDR 분할**: CIDR을 더 작은 서브넷으로 분할
- [ ] **CIDR 중복/충돌 검출**: CIDR 리스트에서 중복 또는 겹치는 대역 식별

## 📋 Features (계획)

### 1. CIDR 정규화 ✅
```java
CidrBlock cidr = new CidrBlock("192.168.1.100/24");
String normalized = cidr.normalize(); // "192.168.1.0/24"
```

### 2. IP 포함 여부 검사 (예정)
```java
CidrBlock cidr = new CidrBlock("192.168.1.0/24");
boolean contains = cidr.contains("192.168.1.100"); // true
```

### 3. CIDR 대역 IP 목록 조회 (예정)
```java
CidrBlock cidr = new CidrBlock("192.168.1.0/30");
List<String> ips = cidr.getAllIpAddresses(); 
// ["192.168.1.1", "192.168.1.2"] (네트워크/브로드캐스트 제외)
```

### 4. CIDR 분할 (예정)
```java
CidrBlock cidr = new CidrBlock("192.168.1.0/24");
List<CidrBlock> subnets = cidr.split(26);
// [192.168.1.0/26, 192.168.1.64/26, 192.168.1.128/26, 192.168.1.192/26]
```

### 5. CIDR 중복/충돌 검출 (예정)
```java
List<CidrBlock> cidrs = Arrays.asList(
    new CidrBlock("192.168.1.0/24"),
    new CidrBlock("192.168.1.0/25")
);
List<CidrConflict> conflicts = CidrValidator.findConflicts(cidrs);
```

## 🛠️ 사용 방법

### 현재 사용 가능한 기능

#### CIDR 생성 및 정규화
```java
import swnoh.cidr.CidrBlock;

// CIDR 생성
CidrBlock cidr = new CidrBlock("192.168.1.100/24");

// 정규화
String normalized = cidr.normalize(); // "192.168.1.0/24"

// 정보 조회
String networkAddress = cidr.getNetworkAddress(); // "192.168.1.100"
int prefixLength = cidr.getPrefixLength(); // 24
```

#### IP 주소 처리
```java
import swnoh.cidr.IpAddress;

// IP 생성
IpAddress ip = new IpAddress("192.168.1.1");

// long 변환
long ipAsLong = ip.toLong(); // 3232235777

// long에서 IP 생성
IpAddress ipFromLong = IpAddress.fromLong(3232235777L); // "192.168.1.1"
```

## 🏗️ 빌드 및 테스트

```bash
# 프로젝트 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 테스트 리포트 확인
open build/reports/tests/test/index.html
```

## 📦 의존성 추가 (예정)

### Maven
```xml
<dependency>
    <groupId>com.sungwoong</groupId>
    <artifactId>cidr-validator</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```kotlin
implementation("com.sungwoong:cidr-validator:1.0.0")
```

## 🗺️ 개발 로드맵

### Phase 1: 기본 기능 구현 ✅
- [x] CIDR 파싱 및 검증
- [x] IP 주소 검증 
- [x] CIDR 정규화

### Phase 2: 계산 기능 구현 🔄
- [ ] IP 포함 여부 검사
- [ ] CIDR 대역 IP 목록 조회
- [ ] CIDR 분할

### Phase 3: 고급 기능 구현 📋
- [ ] CIDR 중복/충돌 검출
- [ ] 성능 최적화
- [ ] IPv6 지원 (향후)

## 🧪 테스트

현재 구현된 기능들은 JUnit 5를 사용한 포괄적인 테스트를 포함합니다:

- `CidrBlockTest`: CIDR 생성, 검증, 정규화 테스트
- `IpAddressTest`: IP 주소 검증, 변환 테스트

## 📄 라이선스

MIT License - 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 🤝 기여

버그 리포트, 기능 요청, 풀 리퀘스트를 환영합니다!

---

> **개발 중인 프로젝트입니다.** 현재 기본 기능들을 단계적으로 구현하고 있으며, API가 변경될 수 있습니다.
