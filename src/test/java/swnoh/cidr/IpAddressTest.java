package swnoh.cidr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class IpAddressTest {
    
    @Test
    @DisplayName("유효한 IP 주소 생성 테스트")
    void testValidIpCreation() {
        assertDoesNotThrow(() -> IpAddress.fromString("192.168.1.1"));
        assertDoesNotThrow(() -> IpAddress.fromString("0.0.0.0"));
        assertDoesNotThrow(() -> IpAddress.fromString("255.255.255.255"));
        assertDoesNotThrow(() -> IpAddress.fromString("10.0.0.1"));
    }
    
    @Test
    @DisplayName("잘못된 IP 주소 생성 테스트")
    void testInvalidIpCreation() {
        assertThrows(IllegalArgumentException.class, () -> IpAddress.fromString("256.168.1.0"));
        assertThrows(IllegalArgumentException.class, () -> IpAddress.fromString("192.256.1.0"));
        assertThrows(IllegalArgumentException.class, () -> IpAddress.fromString("192.168.256.0"));
        assertThrows(IllegalArgumentException.class, () -> IpAddress.fromString("192.168.1.256"));
        assertThrows(IllegalArgumentException.class, () -> IpAddress.fromString("-1.168.1.0"));
        assertThrows(IllegalArgumentException.class, () -> IpAddress.fromString("192.168.1"));
        assertThrows(IllegalArgumentException.class, () -> IpAddress.fromString("192.168.1.a"));
        assertThrows(IllegalArgumentException.class, () -> IpAddress.fromString(""));
        assertThrows(IllegalArgumentException.class, () -> IpAddress.fromString(null));
    }
    
    @Test
    @DisplayName("IP를 long으로 변환 테스트")
    void testToLong() {
        IpAddress ip1 = IpAddress.fromString("192.168.1.1");
        assertEquals(3232235777L, ip1.toLong());
        
        IpAddress ip2 = IpAddress.fromString("0.0.0.0");
        assertEquals(0L, ip2.toLong());
        
        IpAddress ip3 = IpAddress.fromString("255.255.255.255");
        assertEquals(4294967295L, ip3.toLong());
    }
    
    @Test
    @DisplayName("long에서 IP로 변환 테스트")
    void testFromLong() {
        IpAddress ip1 = IpAddress.fromLong(3232235777L);
        assertEquals("192.168.1.1", ip1.toString());
        
        IpAddress ip2 = IpAddress.fromLong(0L);
        assertEquals("0.0.0.0", ip2.toString());
        
        IpAddress ip3 = IpAddress.fromLong(4294967295L);
        assertEquals("255.255.255.255", ip3.toString());
    }
    
    @Test
    @DisplayName("IP 문자열 반환 테스트")
    void testToString() {
        IpAddress ip = IpAddress.fromString("192.168.1.1");
        assertEquals("192.168.1.1", ip.toString());
    }
    
    @Test
    @DisplayName("IP 동등성 테스트")
    void testEquals() {
        IpAddress ip1 = IpAddress.fromString("192.168.1.1");
        IpAddress ip2 = IpAddress.fromString("192.168.1.1");
        IpAddress ip3 = IpAddress.fromString("192.168.1.2");
        
        assertEquals(ip1, ip2);
        assertNotEquals(ip1, ip3);
    }
} 