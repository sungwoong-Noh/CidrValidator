# CidrValidator

## Overview
**CIDR Validator**는 네트워크 설정에서 CIDR(Classless Inter-Domain Routing) 표기법의 검증, 대역 계산, 정규화 요구사항을 충족하기 위해 개발된 Java 오픈 소스 라이브러리입니다. CSP(클라우드 서비스 제공자) 네트워크 장비 설정 과정에서 CIDR 중복, 대역 불일치, 비표준 형식으로 인한 오류를 체계적으로 해결하고자 만들어졌습니다.

> **Apache Commons vs CIDR Validator**: Apache Commons IP에서도 일부 기능을 제공하지만, 본 라이브러리는 네트워크 관리에 특화된 추가 기능들(CIDR 병합, 분할, 충돌 검출 등)을 제공합니다.

## 🚀 개발 현황

### ✅ 구현 완료된 기능
- [x] **CIDR 정규화**: 비표준 CIDR을 표준 형식으로 변환
  - 예: `192.168.1.1/24` → `192.168.1.0/24`
- [x] **IP 포함 여부 검사**: IP가 CIDR 대역 내 포함되는지 검사
  - 예: `192.168.1.100`이 `192.168.1.0/24`에 포함되는지 확인
- [x] **CIDR 대역 IP 목록 조회**: CIDR의 사용 가능 IP 목록 반환
  - 예: `192.168.1.0/30` → [`192.168.1.1`, `192.168.1.2`]
- [x] **CIDR/IP 유효성 검증**: 유효한 CIDR 및 IP 주소 형식 검증
  - CIDR 형식, IP 범위(0~255), prefix(0~32) 검증
- [x] **기본 클래스 구조**: `CidrBlock`, `IpAddress` 클래스

### 🔄 개발 예정 기능
- [ ] **CIDR 분할**: CIDR을 더 작은 서브넷으로 분할
  - 네트워크 설계 시 대역을 세분화하여 효율적으로 관리 가능
- [ ] **CIDR 병합**: 인접한 대역의 CIDR을 병합하여 더 큰 CIDR로 표현
  - 네트워크 관리에서 불필요한 세분화된 CIDR을 통합하여 간소화
- [ ] **CIDR 중복/충돌 검출**: CIDR 리스트에서 중복 또는 겹치는 대역 식별
  - 클라우드 환경에서 서브넷 구성 시 유효성 검증에 활용

## 📋 Features

### 1. CIDR 정규화 ✅
비표준 CIDR을 네트워크 주소 기반의 표준 형식으로 변환합니다.
```java
CidrBlock cidr = CidrBlock.of("192.168.1.100/24");
String normalized = cidr.normalize(); // "192.168.1.0/24"
```

### 2. IP 포함 여부 검사 ✅
주어진 IP가 CIDR 대역 내에 포함되는지 확인합니다.
```java
CidrBlock cidr = CidrBlock.of("192.168.1.0/24");
boolean contains = cidr.contains("192.168.1.100"); // true
boolean notContains = cidr.contains("10.0.0.1"); // false
```

### 3. CIDR 대역 IP 목록 조회 ✅
입력된 CIDR의 사용 가능한 IP 주소 리스트를 반환합니다 (네트워크/브로드캐스트 제외).
```java
CidrBlock cidr = CidrBlock.of("192.168.1.0/30");
List<String> ips = cidr.getAllIpAddresses(); 
// ["192.168.1.1", "192.168.1.2"] (네트워크/브로드캐스트 제외)

// 특수 케이스
CidrBlock hostRoute = CidrBlock.of("192.168.1.100/32");
List<String> singleIp = hostRoute.getAllIpAddresses(); // ["192.168.1.100"]
```

### 4. CIDR 유효성 검증 ✅
CIDR 형식, IP 범위, prefix 길이를 검증합니다.
```java
// 유효한 CIDR
assertDoesNotThrow(() -> CidrBlock.of("192.168.1.0/24"));

// 무효한 CIDR들
assertThrows(IllegalArgumentException.class, () -> CidrBlock.of("192.168.1.256/24")); // IP 범위 초과
assertThrows(IllegalArgumentException.class, () -> CidrBlock.of("192.168.1.0/33"));   // prefix 범위 초과
assertThrows(IllegalArgumentException.class, () -> CidrBlock.of("192.168.1"));        // 잘못된 형식
```

### 5. CIDR 분할 (예정)
CIDR을 더 작은 서브넷으로 분할해 하위 CIDR 리스트를 반환합니다.
```java
CidrBlock cidr = CidrBlock.of("192.168.1.0/24");
List<CidrBlock> subnets = cidr.split(26);
// [192.168.1.0/26, 192.168.1.64/26, 192.168.1.128/26, 192.168.1.192/26]

// 더 세분화된 분할
List<CidrBlock> smallSubnets = cidr.splitInto(8); // 8개의 동일한 크기 서브넷으로 분할
```

### 6. CIDR 병합 (예정)
인접한 대역의 CIDR을 병합하여 더 큰 CIDR로 표현합니다.
```java
List<CidrBlock> cidrs = Arrays.asList(
    CidrBlock.of("192.168.1.0/26"),
    CidrBlock.of("192.168.1.64/26"),
    CidrBlock.of("192.168.1.128/26"),
    CidrBlock.of("192.168.1.192/26")
);

List<CidrBlock> merged = CidrUtils.merge(cidrs);
// [192.168.1.0/24] - 4개의 /26이 하나의 /24로 병합
```

### 7. CIDR 중복/충돌 검출 (예정)
CIDR 리스트에서 중복 또는 겹치는 대역을 식별합니다.
```java
List<CidrBlock> cidrs = Arrays.asList(
    CidrBlock.of("192.168.1.0/24"),
    CidrBlock.of("192.168.1.0/25"),     // 위와 겹침
    CidrBlock.of("10.0.0.0/8"),
    CidrBlock.of("192.168.1.128/25")    // 첫 번째와 겹침
);

List<CidrConflict> conflicts = CidrValidator.findConflicts(cidrs);
// CidrConflict 객체에는 충돌 유형, 관련 CIDR들, 충돌 영역 정보 포함
```

### 8. CIDR 대역 포함 관계 분석(예정)
다른 CIDR에 완전히 포함되는지, 부분적으로 겹치는지, 완전히 분리되어 있는지 분석합니다.
포함 관계는 열거형 형태로 제공됩니다. 
```java
enum Relationship { CONTAINS, CONTAINED_BY, OVERLAPS, DISJOINT }
Relationship analyzeRelationship(String cidr1, String cidr2);
```


## 🛠️ 사용 방법

### 현재 사용 가능한 기능

#### CIDR 생성 및 정규화
```java
import swnoh.cidr.CidrBlock;

// CIDR 생성 (자동 검증 포함)
CidrBlock cidr = CidrBlock.of("192.168.1.100/24");

// 정규화
String normalized = cidr.normalize(); // "192.168.1.0/24"

// 정보 조회
String networkAddress = cidr.getNetworkAddress(); // "192.168.1.100"
int prefixLength = cidr.getPrefixLength(); // 24
```

#### IP 포함 여부 확인
```java
CidrBlock cidr = CidrBlock.of("192.168.1.0/24");

// 문자열 IP로 확인
boolean contains1 = cidr.contains("192.168.1.100"); // true
boolean contains2 = cidr.contains("10.0.0.1");      // false

// IpAddress 객체로 확인
IpAddress ip = IpAddress.fromString("192.168.1.50");
boolean contains3 = cidr.contains(ip); // true
```

#### IP 목록 조회
```java
// 작은 서브넷의 모든 사용 가능한 IP 조회
CidrBlock smallCidr = CidrBlock.of("192.168.1.0/29");
List<String> ips = smallCidr.getAllIpAddresses();
// ["192.168.1.1", "192.168.1.2", "192.168.1.3", "192.168.1.4", "192.168.1.5", "192.168.1.6"]

// 큰 서브넷은 제한됨 (최대 1000개 IP)
assertThrows(IllegalArgumentException.class, () -> {
    CidrBlock.of("10.0.0.0/16").getAllIpAddresses(); // 65534개 IP로 제한 초과
});
```

#### IP 주소 직접 처리
```java
import swnoh.cidr.IpAddress;

// IP 생성 및 검증
IpAddress ip = IpAddress.fromString("192.168.1.1");

// long 변환 (비트 연산용)
long ipAsLong = ip.toLong(); // 3232235777

// long에서 IP 생성
IpAddress ipFromLong = IpAddress.fromLong(3232235777L); // "192.168.1.1"

// IP 동등성 비교
IpAddress ip1 = IpAddress.fromString("192.168.1.1");
IpAddress ip2 = IpAddress.fromString("192.168.1.1");
assertEquals(ip1, ip2); // true
```

## 🏗️ 빌드 및 테스트

```bash
# 프로젝트 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 테스트 리포트 확인 (macOS/Linux)
open build/reports/tests/test/index.html

# 테스트 리포트 확인 (Windows)
start build/reports/tests/test/index.html
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
- [x] IP 포함 여부 검사
- [x] CIDR 대역 IP 목록 조회

### Phase 2: 고급 계산 기능 구현 🔄
- [x] CIDR 분할 (split, splitInto)
- [ ] CIDR 병합 (merge)
- [ ] 네트워크/브로드캐스트 주소 계산

### Phase 3: 네트워크 관리 기능 📋
- [ ] CIDR 중복/충돌 검출
- [ ] CIDR 최적화 (불필요한 세분화 제거)
- [ ] 성능 최적화
- [ ] IPv6 지원 (향후)

## 🧪 테스트

현재 구현된 기능들은 JUnit 5를 사용한 포괄적인 테스트를 포함합니다:

- `CidrBlockTest`: CIDR 생성, 검증, 정규화, 포함 여부, IP 목록 조회 테스트
- `IpAddressTest`: IP 주소 검증, 변환, 동등성 테스트
- **테스트 커버리지**: 90% 이상 목표

### 테스트 케이스 예시
```java
// 경계값 테스트
@Test void testEdgeCases() {
    CidrBlock cidr = CidrBlock.of("172.16.5.128/25");
    assertTrue(cidr.contains("172.16.5.128"));  // 시작
    assertTrue(cidr.contains("172.16.5.255"));  // 끝
    assertFalse(cidr.contains("172.16.5.127")); // 범위 밖
}

// 특수 서브넷 테스트
@Test void testSpecialSubnets() {
    // /31: 포인트-투-포인트 링크
    CidrBlock p2p = CidrBlock.of("192.168.1.0/31");
    assertEquals(2, p2p.getAllIpAddresses().size());
    
    // /32: 단일 호스트
    CidrBlock host = CidrBlock.of("192.168.1.100/32");
    assertEquals(1, host.getAllIpAddresses().size());
}
```

## 🔗 관련 RFC 및 표준

- **RFC 4632**: Classless Inter-domain Routing (CIDR)
- **RFC 3021**: Using 31-Bit Prefixes on IPv4 Point-to-Point Links
- **RFC 1918**: Address Allocation for Private Internets

## 📄 라이선스

MIT License - 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 🤝 기여

버그 리포트, 기능 요청, 풀 리퀘스트를 환영합니다!

### 기여 가이드라인
1. 새로운 기능은 반드시 테스트 코드와 함께 제출
2. 코드 스타일은 Google Java Style Guide 준수
3. API 변경 시 README 업데이트 필수

---

> **개발 중인 프로젝트입니다.** 현재 기본 기능들을 단계적으로 구현하고 있으며, API가 변경될 수 있습니다.
