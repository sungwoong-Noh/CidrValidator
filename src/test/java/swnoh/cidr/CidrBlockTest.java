package swnoh.cidr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CidrBlockTest {
    
    @Test
    @DisplayName("정규화 테스트 - 192.168.1.1/24 -> 192.168.1.0/24")
    void testNormalize_BasicCase() {
        CidrBlock cidr = CidrBlock.of("192.168.1.1/24");
        String normalized = cidr.normalize();
        assertEquals("192.168.1.0/24", normalized);
    }
    
    @Test
    @DisplayName("정규화 테스트 - 10.0.0.100/16 -> 10.0.0.0/16")
    void testNormalize_Class_A() {
        CidrBlock cidr = CidrBlock.of("10.0.0.100/16");
        String normalized = cidr.normalize();
        assertEquals("10.0.0.0/16", normalized);
    }
    
    @Test
    @DisplayName("정규화 테스트 - 172.16.5.10/20 -> 172.16.0.0/20")
    void testNormalize_SubnetBoundary() {
        CidrBlock cidr = CidrBlock.of("172.16.5.10/20");
        String normalized = cidr.normalize();
        assertEquals("172.16.0.0/20", normalized);
    }
    
    @Test
    @DisplayName("정규화 테스트 - 이미 정규화된 CIDR은 그대로 반환")
    void testNormalize_AlreadyNormalized() {
        CidrBlock cidr = CidrBlock.of("192.168.1.0/24");
        String normalized = cidr.normalize();
        assertEquals("192.168.1.0/24", normalized);
    }
    
    @Test
    @DisplayName("CIDR 생성 테스트 - 유효한 입력")
    void testValidCidrCreation() {
        assertDoesNotThrow(() -> CidrBlock.of("192.168.1.0/24"));
        assertDoesNotThrow(() -> CidrBlock.of("10.0.0.0/8"));
        assertDoesNotThrow(() -> CidrBlock.of("172.16.0.0/12"));
        assertEquals("192.168.1.0/24", CidrBlock.of("192.168.1.0/24").toString());
    }
    
    @Test
    @DisplayName("CIDR 생성 테스트 - 잘못된 입력")
    void testInvalidCidrCreation() {
        assertThrows(IllegalArgumentException.class, () -> CidrBlock.of(""));
        assertThrows(IllegalArgumentException.class, () -> CidrBlock.of("192.168.1.0"));
        assertThrows(IllegalArgumentException.class, () -> CidrBlock.of("192.168.1.0/33"));
        assertThrows(IllegalArgumentException.class, () -> CidrBlock.of("192.168.1.0/-1"));
        assertThrows(IllegalArgumentException.class, () -> CidrBlock.of("192.168.1.266/24"));
    }

    @Test
    @DisplayName("IP 포함 테스트 - 기본 케이스")
    void testContains_BasicCase() {
        CidrBlock cidr = CidrBlock.of("192.168.1.0/24");
        
        assertTrue(cidr.contains("192.168.1.1"));
        assertTrue(cidr.contains("192.168.1.100"));
        assertTrue(cidr.contains("192.168.1.254"));
        assertFalse(cidr.contains("192.168.0.1"));
        assertFalse(cidr.contains("192.168.2.1"));
        assertFalse(cidr.contains("10.0.0.1"));
    }

    @Test
    @DisplayName("IP 포함 테스트 - 네트워크/브로드캐스트 주소")
    void testContains_NetworkAndBroadcast() {
        CidrBlock cidr = CidrBlock.of("192.168.1.0/24");
        
        // 네트워크 주소와 브로드캐스트 주소도 포함되어야 함
        assertTrue(cidr.contains("192.168.1.0"));    // 네트워크 주소
        assertTrue(cidr.contains("192.168.1.255"));  // 브로드캐스트 주소
    }

    @Test
    @DisplayName("IP 포함 테스트 - 다양한 prefix length")
    void testContains_DifferentPrefixLengths() {
        // /16 테스트
        CidrBlock cidr16 = CidrBlock.of("10.0.0.0/16");
        assertTrue(cidr16.contains("10.0.1.1"));
        assertTrue(cidr16.contains("10.0.255.255"));
        assertFalse(cidr16.contains("10.1.0.1"));
        
        // /30 테스트 (4개 IP)
        CidrBlock cidr30 = CidrBlock.of("192.168.1.0/30");
        assertTrue(cidr30.contains("192.168.1.0"));   // 네트워크
        assertTrue(cidr30.contains("192.168.1.1"));   // 호스트1
        assertTrue(cidr30.contains("192.168.1.2"));   // 호스트2
        assertTrue(cidr30.contains("192.168.1.3"));   // 브로드캐스트
        assertFalse(cidr30.contains("192.168.1.4"));
    }

    @Test
    @DisplayName("IP 포함 테스트 - Class A, B, C")
    void testContains_DifferentClasses() {
        // Class A
        CidrBlock classA = CidrBlock.of("10.0.0.0/8");
        assertTrue(classA.contains("10.255.255.255"));
        assertFalse(classA.contains("11.0.0.1"));
        
        // Class B
        CidrBlock classB = CidrBlock.of("172.16.0.0/12");
        assertTrue(classB.contains("172.31.255.255"));
        assertFalse(classB.contains("172.32.0.1"));
        
        // Class C
        CidrBlock classC = CidrBlock.of("192.168.0.0/16");
        assertTrue(classC.contains("192.168.255.255"));
        assertFalse(classC.contains("192.169.0.1"));
    }

    @Test
    @DisplayName("IP 포함 테스트 - 경계값")
    void testContains_EdgeCases() {
        CidrBlock cidr = CidrBlock.of("172.16.5.128/25");
        
        // 포함되는 IP들 (172.16.5.128 ~ 172.16.5.255)
        assertTrue(cidr.contains("172.16.5.128"));  // 시작
        assertTrue(cidr.contains("172.16.5.200"));  // 중간
        assertTrue(cidr.contains("172.16.5.255"));  // 끝
        
        // 포함되지 않는 IP들
        assertFalse(cidr.contains("172.16.5.127")); // 바로 아래
        assertFalse(cidr.contains("172.16.6.0"));   // 바로 위
    }

    @Test
    @DisplayName("IP 포함 테스트 - 잘못된 IP 입력")
    void testContains_InvalidIp() {
        CidrBlock cidr = CidrBlock.of("192.168.1.0/24");
        
        assertThrows(IllegalArgumentException.class, () -> cidr.contains(""));
        assertThrows(IllegalArgumentException.class, () -> cidr.contains("192.168.1"));
        assertThrows(IllegalArgumentException.class, () -> cidr.contains("192.168.1.256"));
        assertThrows(IllegalArgumentException.class, () -> cidr.contains("invalid.ip"));
        assertThrows(IllegalArgumentException.class, () -> cidr.contains((String) null));
    }

    @Test
    @DisplayName("IP 포함 테스트 - IpAddress 객체로도 테스트")
    void testContains_WithIpAddressObject() {
        CidrBlock cidr = CidrBlock.of("192.168.1.0/24");
        IpAddress ipInside = IpAddress.fromString("192.168.1.100");
        IpAddress ipOutside = IpAddress.fromString("10.0.0.1");

        assertTrue(cidr.contains("192.168.1.0"));
        assertFalse(cidr.contains("192.168.3.0"));
        assertTrue(cidr.contains(ipInside));
        assertFalse(cidr.contains(ipOutside));
    }

    @Test
    @DisplayName("IP 목록 조회 - /30 서브넷 (4개 IP, 사용가능 2개)")
    void getAllIpAddresses_Subnet30() {
        CidrBlock cidr = CidrBlock.of("192.168.1.0/30");
        List<String> ips = cidr.getAllIpAddresses();

        // /30은 4개 IP: .0(네트워크), .1(사용가능), .2(사용가능), .3(브로드캐스트) => 사용가능 IP는 2개
        assertEquals(2, ips.size());
        assertTrue(ips.contains("192.168.1.1"));
        assertTrue(ips.contains("192.168.1.2"));
        assertFalse(ips.contains("192.168.1.0")); // 네트워크 주소
        assertFalse(ips.contains("192.168.1.4")); // 브로드캐스트 주소
    }

    @Test
    @DisplayName("IP 목록 조회 - /31 서브넷 (2개 IP, 포인트-투-포인트 링크)")
    void getAllIpAddress_Subnet31() {
        CidrBlock cidr = CidrBlock.of("192.168.1.0/31");
        List<String> ips = cidr.getAllIpAddresses();

        // /31은 포인트-투-포인트 링크로 네트워크, 브로드캐스트 개념이 없음(RFC 3021)
        assertEquals(2, ips.size());
        assertTrue(ips.contains("192.168.1.0"));
        assertTrue(ips.contains("192.168.1.1"));
        assertFalse(ips.contains("192.168.1.2"));
        assertFalse(ips.contains("192.168.1.3"));
    }

    @Test
    @DisplayName("IP 목록 조회 - /32 서브넷 (단일 호스트)")
    void getAllIpAddress_Subnet32() {
        CidrBlock cidr = CidrBlock.of("192.168.1.100/32");
        List<String> ips = cidr.getAllIpAddresses();

        // /32는 단일 호스트
        assertEquals(1, ips.size());
        assertTrue(ips.contains("192.168.1.100"));
    }

    @Test
    @DisplayName("IP 목록 조회 - /29 서브넷 (8개 IP, 사용가능 6개)")
    void getAllIpAddress_Subnet29() {
        CidrBlock cidr = CidrBlock.of("192.168.1.0/29");
        List<String> ips = cidr.getAllIpAddresses();

        // /29는 8개 IP: .0(네트워크), .1.2.3.4.5.6(사용가능), .7(브로드캐스트)
        assertEquals(6, ips.size());
        assertFalse(ips.contains("192.168.1.0"));
        assertTrue(ips.contains("192.168.1.1"));
        assertTrue(ips.contains("192.168.1.2"));
        assertTrue(ips.contains("192.168.1.3"));
        assertTrue(ips.contains("192.168.1.4"));
        assertTrue(ips.contains("192.168.1.5"));
        assertTrue(ips.contains("192.168.1.6"));
        assertFalse(ips.contains("192.168.1.7"));

    }

    @Test
    @DisplayName("IP 목록 조회 - /28 서브넷 순서 확인")
    void testGetAllIpAddresses_OrderedList() {
        CidrBlock cidr = CidrBlock.of("10.0.0.0/28");
        List<String> ips = cidr.getAllIpAddresses();

        // /28은 총 16개 IP, 사용가능한 것은 14개
        assertEquals(14, ips.size());

        // 순서가 올바른지 확인
        assertEquals("10.0.0.1", ips.get(0));   // 첫 번째
        assertEquals("10.0.0.14", ips.get(13)); // 마지막

        // 연속성 확인
        for (int i = 0; i < ips.size() - 1; i++) {
            IpAddress current = IpAddress.fromString(ips.get(i));
            IpAddress next = IpAddress.fromString(ips.get(i + 1));
            assertEquals(1, next.toLong() - current.toLong());
        }
    }

    @Test
    @DisplayName("IP 목록 조회 - 성능 테스트 (/24 서브넷)")
    void testGetAllIpAddresses_Performance() {
        CidrBlock cidr = CidrBlock.of("192.168.1.0/24");

        long startTime = System.currentTimeMillis();
        List<String> ips = cidr.getAllIpAddresses();
        long endTime = System.currentTimeMillis();

        // /24는 254개의 사용 가능한 IP
        assertEquals(254, ips.size());

        // 성능 확인 (1초 이내 완료되어야 함)
        assertTrue(endTime - startTime < 1000, "IP 목록 조회가 너무 오래 걸립니다: " + (endTime - startTime) + "ms");

        // 첫 번째와 마지막 IP 확인
        assertEquals("192.168.1.1", ips.get(0));
        assertEquals("192.168.1.254", ips.get(253));
    }

    @Test
    @DisplayName("IP 목록 조회 - 큰 서브넷 제한 (/16 이상) : 어떤식으로 대역을 제한할지 고민 필요")
    void testGetAllIpAddresses_LargeSubnetLimit() {
        // /16 서브넷은 65534개의 호스트 IP를 가지므로 제한이 필요할 수 있음
        CidrBlock cidr = CidrBlock.of("10.0.0.0/16");

        // 큰 서브넷의 경우 예외를 던지거나 제한을 둘 수 있음
        assertThrows(IllegalArgumentException.class, () -> {
            cidr.getAllIpAddresses();
        }, "큰 서브넷에 대해서는 제한이 있어야 합니다");
    }
}