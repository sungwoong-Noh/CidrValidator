package swnoh.cidr;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
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

    @Test
    @DisplayName("CIDR 분할 테스트 - /24를 /25로 분할")
    void testSplit_24to25() {

        CidrBlock cidrBlock = CidrBlock.of("192.168.1.0/24");
        List<CidrBlock> subnets = cidrBlock.split(25);

        assertEquals(2, subnets.size());
        assertEquals("192.168.1.0/25", subnets.get(0).toString());
        assertEquals("192.168.1.128/25", subnets.get(1).toString());
    }

    @Test
    @DisplayName("CIDR 분할 테스트 - /16을 /24로 분할")
    void testSplit_16to24() {
        CidrBlock cidrBlock = CidrBlock.of("10.0.0.0/16");
        List<CidrBlock> subnets = cidrBlock.split(24);

        assertEquals(256, subnets.size());
        assertEquals("10.0.0.0/24", subnets.get(0).toString());
        assertEquals("10.0.1.0/24", subnets.get(1).toString());
        assertEquals("10.0.255.0/24", subnets.get(255).toString());
    }

    @Test
    @DisplayName("CIDR 분할 테스트 - /32는 분할 불가")
    void testSplit_32() {

        CidrBlock cidrBlock = CidrBlock.of("10.0.0.0/32");

        assertThrows(IllegalArgumentException.class, () -> cidrBlock.split(32));
    }

    @Test
    @DisplayName("CIDR 분할 테스트 - 분할된 서브넷들의 유효성 검증")
    void testSplit_ValidateSubnets() {

        CidrBlock cidrBlock = CidrBlock.of("192.168.0.0/20");
        List<CidrBlock> subnets = cidrBlock.split(22);

        assertEquals(4, subnets.size());

        for (int i = 0; i < subnets.size(); i++) {
            for (int j = i + 1; j < subnets.size(); j++) {

                CidrBlock subnet1 = subnets.get(i);
                CidrBlock subnet2 = subnets.get(j);

                // 분할된 서브넷 들은 서로 겹치지 않아야 함
                List<String> ips1 = subnet1.getAllIpAddresses();
                List<String> ips2 = subnet2.getAllIpAddresses();

                // 각 서브넷의 첫 번째 IP가 서로 포함되지 않아야 함
                if (!ips1.isEmpty() && !ips2.isEmpty()) {
                    assertFalse(subnet1.contains(ips2.getFirst()));
                    assertFalse(subnet2.contains(ips1.getFirst()));
                }
            }
        }
    }

    @Test
    @DisplayName("CIDR 분할 테스트 - 모든 IP가 커버되는지 확인")
    void testSplit_CompleteCoverage() {
        //64개 ip
        CidrBlock originalCidr = CidrBlock.of("192.168.1.0/26");
        List<CidrBlock> subnets = originalCidr.split(28);

        // 가져올 때는 네트워크와 브로드캐스트 주소를 제외한 IP 목록을 가져옴 => -2 개
        List<String> originalIps = originalCidr.getAllIpAddresses();

        List<String> splitIps = new ArrayList<>();

        for (CidrBlock subnet : subnets) {
            // 각 서브넷의 모든 IP 주소를 가져옴 => 네트워크 주소와 브로드캐스트 주소를 제외한 IP 목록, -2개 씩
            splitIps.addAll(subnet.getAllIpAddresses());
        }

        int networkAndBroadIpsCount = 2 * subnets.size() - 2;

        assertEquals(originalIps.size(), splitIps.size() + networkAndBroadIpsCount); // 네트워크 주소와 브로드캐스트 주소 제외

    }

    @Test
    @DisplayName("CIDR 분할 - 기본 케이스만")
    void testSplit_Basic() {
        CidrBlock cidr = CidrBlock.of("192.168.1.0/24");
        List<CidrBlock> subnets = cidr.split(26);
        
        assertEquals(4, subnets.size());
        assertEquals("192.168.1.0/26", subnets.get(0).toString());
        assertEquals("192.168.1.64/26", subnets.get(1).toString());
        assertEquals("192.168.1.128/26", subnets.get(2).toString());
        assertEquals("192.168.1.192/26", subnets.get(3).toString());
    }

    @Test
    @DisplayName("CIDR 분할 - 예외 처리")
    void testSplit_Exceptions() {
        CidrBlock cidr = CidrBlock.of("192.168.1.0/24");
        
        assertThrows(IllegalArgumentException.class, () -> cidr.split(24));  // 같은 크기
        assertThrows(IllegalArgumentException.class, () -> cidr.split(23));  // 더 작은 크기
        assertThrows(IllegalArgumentException.class, () -> cidr.split(33));  // 범위 초과
        assertThrows(IllegalArgumentException.class, () -> cidr.split(-1));  // 음수
        
        CidrBlock host = CidrBlock.of("192.168.1.1/32");
        assertThrows(IllegalArgumentException.class, () -> host.split(33));
    }

    // ==================== Phase2: 네트워크/브로드캐스트 주소 계산 기능 테스트 ====================

    @Test
    @DisplayName("네트워크 주소 계산 - 기본 케이스들")
    void testGetNetworkAddress_BasicCases() {
        // /24 서브넷
        CidrBlock cidr24 = CidrBlock.of("192.168.1.100/24");
        assertEquals("192.168.1.0", cidr24.getNetworkAddressString());
        
        // /16 서브넷
        CidrBlock cidr16 = CidrBlock.of("10.0.5.10/16");
        assertEquals("10.0.0.0", cidr16.getNetworkAddressString());
        
        // /8 서브넷
        CidrBlock cidr8 = CidrBlock.of("172.20.30.40/8");
        assertEquals("172.0.0.0", cidr8.getNetworkAddressString());
        
        // /30 서브넷
        CidrBlock cidr30 = CidrBlock.of("192.168.1.9/30");
        assertEquals("192.168.1.8", cidr30.getNetworkAddressString());
    }

    @Test
    @DisplayName("브로드캐스트 주소 계산 - 기본 케이스들")
    void testGetBroadcastAddress_BasicCases() {
        // /24 서브넷
        CidrBlock cidr24 = CidrBlock.of("192.168.1.100/24");
        assertEquals("192.168.1.255", cidr24.getBroadcastAddress());
        
        // /16 서브넷
        CidrBlock cidr16 = CidrBlock.of("10.0.5.10/16");
        assertEquals("10.0.255.255", cidr16.getBroadcastAddress());
        
        // /30 서브넷
        CidrBlock cidr30 = CidrBlock.of("192.168.1.9/30");
        assertEquals("192.168.1.11", cidr30.getBroadcastAddress());
        
        // /25 서브넷
        CidrBlock cidr25 = CidrBlock.of("192.168.1.200/25");
        assertEquals("192.168.1.255", cidr25.getBroadcastAddress());
    }

    @Test
    @DisplayName("브로드캐스트 주소 계산 - 특수 케이스 (/31, /32)")
    void testGetBroadcastAddress_SpecialCases() {
        // /32: 단일 호스트
        CidrBlock cidr32 = CidrBlock.of("192.168.1.100/32");
        assertEquals("192.168.1.100", cidr32.getBroadcastAddress());
        
        // /31: 포인트-투-포인트 링크
        CidrBlock cidr31 = CidrBlock.of("192.168.1.0/31");
        assertEquals("192.168.1.1", cidr31.getBroadcastAddress());
        
        CidrBlock cidr31_odd = CidrBlock.of("192.168.1.1/31");
        assertEquals("192.168.1.1", cidr31_odd.getBroadcastAddress());
    }

    @Test
    @DisplayName("서브넷 마스크 계산 테스트")
    void testGetSubnetMask() {
        // 일반적인 서브넷 마스크들
        assertEquals("255.255.255.0", CidrBlock.of("192.168.1.0/24").getSubnetMask());
        assertEquals("255.255.0.0", CidrBlock.of("10.0.0.0/16").getSubnetMask());
        assertEquals("255.0.0.0", CidrBlock.of("172.16.0.0/8").getSubnetMask());
        
        // 비일반적인 서브넷 마스크들
        assertEquals("255.255.255.240", CidrBlock.of("192.168.1.0/28").getSubnetMask());
        assertEquals("255.255.255.252", CidrBlock.of("192.168.1.0/30").getSubnetMask());
        assertEquals("255.255.255.254", CidrBlock.of("192.168.1.0/31").getSubnetMask());
        assertEquals("255.255.255.255", CidrBlock.of("192.168.1.0/32").getSubnetMask());
        
        // VLSM 예시
        assertEquals("255.255.248.0", CidrBlock.of("172.16.0.0/21").getSubnetMask());
    }

    @Test
    @DisplayName("총 IP 개수 계산 테스트")
    void testGetTotalIpCount() {
        assertEquals(256L, CidrBlock.of("192.168.1.0/24").getTotalIpCount());
        assertEquals(65536L, CidrBlock.of("10.0.0.0/16").getTotalIpCount());
        assertEquals(16777216L, CidrBlock.of("172.16.0.0/8").getTotalIpCount());
        assertEquals(4L, CidrBlock.of("192.168.1.0/30").getTotalIpCount());
        assertEquals(2L, CidrBlock.of("192.168.1.0/31").getTotalIpCount());
        assertEquals(1L, CidrBlock.of("192.168.1.100/32").getTotalIpCount());
        
        // 큰 서브넷
        assertEquals(1024L, CidrBlock.of("10.0.0.0/22").getTotalIpCount());
        assertEquals(512L, CidrBlock.of("172.16.0.0/23").getTotalIpCount());
    }

    @Test
    @DisplayName("사용 가능한 IP 개수 계산 테스트")
    void testGetUsableIpCount() {
        // 일반적인 케이스 (총 IP - 2)
        assertEquals(254L, CidrBlock.of("192.168.1.0/24").getUsableIpCount());
        assertEquals(65534L, CidrBlock.of("10.0.0.0/16").getUsableIpCount());
        assertEquals(2L, CidrBlock.of("192.168.1.0/30").getUsableIpCount());
        assertEquals(14L, CidrBlock.of("192.168.1.0/28").getUsableIpCount());
        
        // 특수 케이스
        assertEquals(2L, CidrBlock.of("192.168.1.0/31").getUsableIpCount()); // 포인트-투-포인트
        assertEquals(1L, CidrBlock.of("192.168.1.100/32").getUsableIpCount()); // 단일 호스트
    }

    @Test
    @DisplayName("첫 번째 사용 가능한 IP 계산 테스트")
    void testGetFirstUsableIp() {
        // 일반적인 케이스 (네트워크 주소 + 1)
        assertEquals("192.168.1.1", CidrBlock.of("192.168.1.0/24").getFirstUsableIp());
        assertEquals("10.0.0.1", CidrBlock.of("10.0.0.0/16").getFirstUsableIp());
        assertEquals("192.168.1.9", CidrBlock.of("192.168.1.8/30").getFirstUsableIp());
        
        // 특수 케이스
        assertEquals("192.168.1.0", CidrBlock.of("192.168.1.0/31").getFirstUsableIp()); // /31: 네트워크 주소
        assertEquals("192.168.1.100", CidrBlock.of("192.168.1.100/32").getFirstUsableIp()); // /32: 자기 자신
    }

    @Test
    @DisplayName("마지막 사용 가능한 IP 계산 테스트")
    void testGetLastUsableIp() {
        // 일반적인 케이스 (브로드캐스트 주소 - 1)
        assertEquals("192.168.1.254", CidrBlock.of("192.168.1.0/24").getLastUsableIp());
        assertEquals("10.0.255.254", CidrBlock.of("10.0.0.0/16").getLastUsableIp());
        assertEquals("192.168.1.10", CidrBlock.of("192.168.1.8/30").getLastUsableIp());
        
        // 특수 케이스
        assertEquals("192.168.1.1", CidrBlock.of("192.168.1.0/31").getLastUsableIp()); // /31: 브로드캐스트 주소
        assertEquals("192.168.1.100", CidrBlock.of("192.168.1.100/32").getLastUsableIp()); // /32: 자기 자신
    }

    @Test
    @DisplayName("네트워크 정보 일관성 검증 - /24")
    void testNetworkInfoConsistency_24() {
        CidrBlock cidr = CidrBlock.of("192.168.1.100/24");
        
        assertEquals("192.168.1.0", cidr.getNetworkAddressString());
        assertEquals("192.168.1.255", cidr.getBroadcastAddress());
        assertEquals("192.168.1.1", cidr.getFirstUsableIp());
        assertEquals("192.168.1.254", cidr.getLastUsableIp());
        assertEquals(256L, cidr.getTotalIpCount());
        assertEquals(254L, cidr.getUsableIpCount());
        assertEquals("255.255.255.0", cidr.getSubnetMask());
        
        // 범위 검증
        assertTrue(cidr.contains(cidr.getNetworkAddressString()));      // 네트워크 주소 포함
        assertTrue(cidr.contains(cidr.getBroadcastAddress()));          // 브로드캐스트 주소 포함
        assertTrue(cidr.contains(cidr.getFirstUsableIp()));             // 첫 번째 사용가능 IP 포함
        assertTrue(cidr.contains(cidr.getLastUsableIp()));              // 마지막 사용가능 IP 포함
    }

    @Test
    @DisplayName("네트워크 정보 일관성 검증 - /30")
    void testNetworkInfoConsistency_30() {
        CidrBlock cidr = CidrBlock.of("192.168.1.9/30");
        
        assertEquals("192.168.1.8", cidr.getNetworkAddressString());
        assertEquals("192.168.1.11", cidr.getBroadcastAddress());
        assertEquals("192.168.1.9", cidr.getFirstUsableIp());
        assertEquals("192.168.1.10", cidr.getLastUsableIp());
        assertEquals(4L, cidr.getTotalIpCount());
        assertEquals(2L, cidr.getUsableIpCount());
        assertEquals("255.255.255.252", cidr.getSubnetMask());
        
        // 범위 검증
        assertTrue(cidr.contains("192.168.1.8"));   // 네트워크
        assertTrue(cidr.contains("192.168.1.9"));   // 첫 번째 사용가능
        assertTrue(cidr.contains("192.168.1.10"));  // 마지막 사용가능
        assertTrue(cidr.contains("192.168.1.11"));  // 브로드캐스트
        assertFalse(cidr.contains("192.168.1.7"));  // 범위 밖
        assertFalse(cidr.contains("192.168.1.12")); // 범위 밖
    }

    @Test
    @DisplayName("네트워크 정보 일관성 검증 - /31 (포인트-투-포인트)")
    void testNetworkInfoConsistency_31() {
        CidrBlock cidr = CidrBlock.of("192.168.1.10/31");
        
        assertEquals("192.168.1.10", cidr.getNetworkAddressString());
        assertEquals("192.168.1.11", cidr.getBroadcastAddress());
        assertEquals("192.168.1.10", cidr.getFirstUsableIp());
        assertEquals("192.168.1.11", cidr.getLastUsableIp());
        assertEquals(2L, cidr.getTotalIpCount());
        assertEquals(2L, cidr.getUsableIpCount());
        assertEquals("255.255.255.254", cidr.getSubnetMask());
        
        // /31에서는 모든 IP가 사용 가능
        assertTrue(cidr.contains("192.168.1.10"));
        assertTrue(cidr.contains("192.168.1.11"));
        assertFalse(cidr.contains("192.168.1.9"));
        assertFalse(cidr.contains("192.168.1.12"));
    }

    @Test
    @DisplayName("네트워크 정보 일관성 검증 - /32 (단일 호스트)")
    void testNetworkInfoConsistency_32() {
        CidrBlock cidr = CidrBlock.of("192.168.1.100/32");
        
        assertEquals("192.168.1.100", cidr.getNetworkAddressString());
        assertEquals("192.168.1.100", cidr.getBroadcastAddress());
        assertEquals("192.168.1.100", cidr.getFirstUsableIp());
        assertEquals("192.168.1.100", cidr.getLastUsableIp());
        assertEquals(1L, cidr.getTotalIpCount());
        assertEquals(1L, cidr.getUsableIpCount());
        assertEquals("255.255.255.255", cidr.getSubnetMask());
        
        // /32에서는 자기 자신만 포함
        assertTrue(cidr.contains("192.168.1.100"));
        assertFalse(cidr.contains("192.168.1.99"));
        assertFalse(cidr.contains("192.168.1.101"));
    }

    @Test
    @DisplayName("상세 정보 출력 테스트")
    void testGetDetailedInfo() {
        CidrBlock cidr = CidrBlock.of("192.168.1.100/24");
        String info = cidr.getDetailedInfo();
        
        assertNotNull(info);
        assertTrue(info.contains("CIDR: 192.168.1.100/24"));
        assertTrue(info.contains("네트워크 주소: 192.168.1.0"));
        assertTrue(info.contains("브로드캐스트 주소: 192.168.1.255"));
        assertTrue(info.contains("서브넷 마스크: 255.255.255.0"));
        assertTrue(info.contains("첫 번째 사용가능 IP: 192.168.1.1"));
        assertTrue(info.contains("마지막 사용가능 IP: 192.168.1.254"));
        assertTrue(info.contains("총 IP 개수: 256"));
        assertTrue(info.contains("사용가능 IP 개수: 254"));
    }

    @Test
    @DisplayName("다양한 서브넷 크기별 계산 검증")
    void testVariousSubnetSizes() {
        // /25 (128개 IP)
        CidrBlock cidr25 = CidrBlock.of("192.168.1.0/25");
        assertEquals("192.168.1.0", cidr25.getNetworkAddressString());
        assertEquals("192.168.1.127", cidr25.getBroadcastAddress());
        assertEquals(128L, cidr25.getTotalIpCount());
        assertEquals(126L, cidr25.getUsableIpCount());
        
        // /26 (64개 IP)
        CidrBlock cidr26 = CidrBlock.of("192.168.1.64/26");
        assertEquals("192.168.1.64", cidr26.getNetworkAddressString());
        assertEquals("192.168.1.127", cidr26.getBroadcastAddress());  
        assertEquals(64L, cidr26.getTotalIpCount());
        assertEquals(62L, cidr26.getUsableIpCount());
        
        // /27 (32개 IP)
        CidrBlock cidr27 = CidrBlock.of("192.168.1.96/27");
        assertEquals("192.168.1.96", cidr27.getNetworkAddressString());
        assertEquals("192.168.1.127", cidr27.getBroadcastAddress());
        assertEquals(32L, cidr27.getTotalIpCount());
        assertEquals(30L, cidr27.getUsableIpCount());
        
        // /28 (16개 IP)
        CidrBlock cidr28 = CidrBlock.of("192.168.1.112/28");
        assertEquals("192.168.1.112", cidr28.getNetworkAddressString());
        assertEquals("192.168.1.127", cidr28.getBroadcastAddress());
        assertEquals(16L, cidr28.getTotalIpCount());
        assertEquals(14L, cidr28.getUsableIpCount());
        
        // /29 (8개 IP)
        CidrBlock cidr29 = CidrBlock.of("192.168.1.120/29");
        assertEquals("192.168.1.120", cidr29.getNetworkAddressString());
        assertEquals("192.168.1.127", cidr29.getBroadcastAddress());
        assertEquals(8L, cidr29.getTotalIpCount());
        assertEquals(6L, cidr29.getUsableIpCount());
    }
}