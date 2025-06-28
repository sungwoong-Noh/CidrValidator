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
}