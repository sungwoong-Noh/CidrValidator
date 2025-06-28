package swnoh.cidr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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
}