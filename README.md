# CidrValidator

## Overview
**CIDR Validator**는 네트워크 설정에서 CIDR(Classless Inter-Domain Routing) 표기법의 검증, 대역 계산, 정규화 요구사항을 충족하기 위해 개발된 오픈 소스 라이브러리입니다. CSP(클라우드 서비스 제공자) 네트워크 장비 설정 과정에서 CIDR 중복, 대역 불일치, 비표준 형식으로 인한 오류를 경험하며, 이러한 문제를 체계적으로 해결하고자 만들어졌습니다. 

## Features
CIDR Validator는 네트워크 설정을 간소화하고 오류를 장비하는 다음 기능을 제공합니다. 
- **CIDR 대역 IP 목록 조회**: CIDR의 사용 가능 IP 목록을 반환합니다.
- **IP 포함 여부 검사**: IP가 CIDR 대역 내 포함되는지 검사합니다..
- **CIDR 중복/충돌 검출**: CIDR 리스트에서 중복 또는 겹치는 대역을 식별합니다.
- **CIDR 분할**: CIDR을 더 작은 서브넷으로 분할해 하위 CIDR 목록을 반환합니다.
    - 예: `192.168.1.0/24` -> [`192.168.1.0/26`, `192.168.1.64/26`, `192.168.1.128/26`, `192.168.1.192/26`]
- **CIDR 정규화**: 비표준 CIDR을 표준형식으로 변환합니다.
    - 예: `192.168.1.1/24` -> `192.168.1.0/24`

# 사용 방법 
## 1. 의존성 추가 
```
//maven
<dependency>
    <groupId>com.sungwoong</groupId>
    <artifactId>cidr-validator</artifactId>
    <version>1.0.0</version>
</dependency>

//gradle
implementation("com.sungwoong:cidr-validator:1.0.0")
`````
