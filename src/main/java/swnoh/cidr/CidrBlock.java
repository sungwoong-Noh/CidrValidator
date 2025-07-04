package swnoh.cidr;

import java.util.ArrayList;
import java.util.List;

public class CidrBlock {

    // 큰 서브넷 제한 (최대 1000개 IP까지 허용)
    private static final int MAX_IP_COUNT = 1024;

    private final IpAddress ipAddress;

    private final int prefixLength;
    
    private CidrBlock(String cidr) {
        if (cidr == null || cidr.trim().isEmpty()) {
            throw new IllegalArgumentException("CIDR cannot be null or empty");
        }
        
        String[] parts = cidr.split("/");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid CIDR format. Expected format: x.x.x.x/y");
        }
        
        // IP 검증은 IpAddress 생성자에서 처리
        this.ipAddress = IpAddress.fromString(parts[0].trim());

        try {
            this.prefixLength = Integer.parseInt(parts[1].trim());
            if (prefixLength < 0 || prefixLength > 32) {
                throw new IllegalArgumentException("Prefix length must be between 0 and 32");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid prefix length: " + parts[1]);
        }
    }

    public static CidrBlock of(String cidr) {
        return new CidrBlock(cidr);
    }
    
    public String normalize() {
        // IP를 long으로 변환
        long ipAsLong = ipAddress.toLong();
        
        // 서브넷 마스크 생성 (prefixLength만큼 1로 채우고 나머지는 0)
        long mask = getMask();
        
        // 네트워크 주소 계산 (IP와 마스크의 AND 연산)
        long networkLong = ipAsLong & mask;
        
        // 정규화된 IP 주소 생성
        IpAddress normalizedIp = IpAddress.fromLong(networkLong);

        return normalizedIp.toString() + "/" + prefixLength;
    }
    
    /**
     * 주어진 IP 주소가 이 CIDR 블록에 포함되는지 확인합니다.
     * 
     * @param ip 확인할 IP 주소 문자열
     * @return IP가 CIDR 블록에 포함되면 true, 아니면 false
     */
    public boolean contains(String ip) {
        if (ip == null) {
            throw new IllegalArgumentException("IP address cannot be null");
        }
        
        IpAddress targetIp = IpAddress.fromString(ip);
        return contains(targetIp);
    }

    /**
     * 주어진 IP 주소가 이 CIDR 블록에 포함되는지 확인합니다.
     * 
     * @param ip 확인할 IpAddress 객체
     * @return IP가 CIDR 블록에 포함되면 true, 아니면 false
     */
    public boolean contains(IpAddress ip) {
        if (ip == null) {
            throw new IllegalArgumentException("IP address cannot be null");
        }
        
        // 서브넷 마스크 생성
        long mask = getMask();
        
        // 이 CIDR의 네트워크 주소 계산
        long thisNetworkAddress = ipAddress.toLong() & mask;
        
        // 대상 IP의 네트워크 주소 계산 (같은 마스크 적용)
        long targetNetworkAddress = ip.toLong() & mask;
        
        // 두 네트워크 주소가 같으면 포함됨
        return thisNetworkAddress == targetNetworkAddress;
    }

    /**
     * CIDR 블록에 포함된 모든 IP 주소를 반환합니다.
     *
     * /32 서브넷은 단일 호스트, /31 서브넷은 포인트-투-포인트 링크로 처리합니다.
     * 이외의 서브넷은 네트워크 주소와 브로드캐스트 주소를 제외한 모든 사용 가능한 IP 주소를 반환합니다.
     * @return CIDR 블록에 포함된 모든 IP 주소의 리스트
     */
    public List<String> getAllIpAddresses() {

        //서브넷 크기 제한
        int hostBits = 32 - prefixLength;
        long totalIps = 1L << hostBits;

        // /32: 단일 호스트
        if (prefixLength == 32) {
            return List.of(ipAddress.toString());
        }

        if (prefixLength == 31) {
            long networkAddress = getNetworkAddress();
            return List.of(
                IpAddress.fromLong(networkAddress).toString(),
                IpAddress.fromLong(networkAddress + 1).toString()
            );
        }

        long usableIps = totalIps - 2; // 네트워크 주소와 브로드캐스트 주소 제외

        if (usableIps > MAX_IP_COUNT) {
            throw new IllegalArgumentException("CIDR block exceeds maximum allowed IP count: " + MAX_IP_COUNT);
        }

        ArrayList<String> result = new ArrayList<>((int)usableIps);

        long networkAddress = getNetworkAddress();

        for (int i = 1; i < totalIps - 1; i++) {
            IpAddress ip = IpAddress.fromLong(networkAddress + i);
            result.add(ip.toString());
        }

        return result;
    }


    /**
     * CIDR 블록을 지정된 prefix length로 분할합니다.
     * 
     * @param newPrefixLength 분할할 새로운 prefix length (현재보다 커야 함)
     * @return 분할된 CIDR 블록들의 리스트
     * @throws IllegalArgumentException 잘못된 prefix length인 경우
     */
    public List<CidrBlock> split(int newPrefixLength) {

        // /32는 분할할 수 없음
        if (prefixLength == 32) {
            throw new IllegalArgumentException("단일 호스트(/32)는 분할할 수 없습니다");
        }

        // 입력 유효성 검증
        if (newPrefixLength <= prefixLength) {
            throw new IllegalArgumentException(
                String.format("새로운 prefix length(%d)는 현재 prefix length(%d)보다 커야 합니다", 
                             newPrefixLength, prefixLength));
        }
        
        if (newPrefixLength < 0 || newPrefixLength > 32) {
            throw new IllegalArgumentException("Prefix length는 0-32 범위여야 합니다: " + newPrefixLength);
        }

        // 분할할 서브넷 개수 계산
        int subnetBits = newPrefixLength - prefixLength;
        int subnetCount = 1 << subnetBits;  // 2^subnetBits
        
        // 각 서브넷의 크기 계산
        int hostBits = 32 - newPrefixLength;
        long subnetSize = 1L << hostBits;   // 2^hostBits
        
        List<CidrBlock> result = new ArrayList<>(subnetCount);
        long baseNetworkAddress = getNetworkAddress();
        
        // 각 서브넷 생성
        for (int i = 0; i < subnetCount; i++) {

            // 서브넷의 네트워크 주소 계산
            long subnetNetworkAddress = baseNetworkAddress + (i * subnetSize);

            IpAddress subnetIp = IpAddress.fromLong(subnetNetworkAddress);

            String subnetCidr = subnetIp.toString() + "/" + newPrefixLength;

            result.add(CidrBlock.of(subnetCidr));
        }
        
        return result;
    }


    /**
     * CIDR 블록의 서브넷 마스크를 계산합니다.
     *
     * @return 서브넷 마스크 (long 형식)
     */
    private long getMask() {

        // 0xFFFFFFFFL = 4294967295L
        // 이진수: 11111111 11111111 11111111 11111111 (32개의 1)
        return (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;
    }


    /**
     * CIDR 블록의 네트워크 주소를 계산합니다.
     *
     * @return 네트워크 주소 (long 형식)
     */
    private long getNetworkAddress() {
        long mask = getMask();
        return ipAddress.toLong() & mask;
    }

    // Phase2: 네트워크/브로드캐스트 주소 계산 기능 추가

    /**
     * CIDR 블록의 네트워크 주소를 반환합니다.
     * 네트워크 주소는 서브넷의 첫 번째 주소로, 라우팅에 사용됩니다.
     * 
     * @return 네트워크 주소 문자열 (예: "192.168.1.0")
     */
    public String getNetworkAddressString() {
        long network = getNetworkAddress();
        return IpAddress.fromLong(network).toString();
    }

    /**
     * CIDR 블록의 브로드캐스트 주소를 반환합니다.
     * 브로드캐스트 주소는 서브넷의 마지막 주소로, 해당 네트워크의 모든 호스트에게 전송하는 데 사용됩니다.
     * 
     * 참고: /31, /32 서브넷에서는 브로드캐스트 개념이 적용되지 않습니다.
     * - /32: 단일 호스트이므로 자기 자신이 브로드캐스트 주소
     * - /31: 포인트-투-포인트 링크로 브로드캐스트 개념 없음 (RFC 3021)
     * 
     * @return 브로드캐스트 주소 문자열 (예: "192.168.1.255")
     */
    public String getBroadcastAddress() {
        if (prefixLength == 32) {
            // /32: 단일 호스트 - 자기 자신이 브로드캐스트
            return ipAddress.toString();
        }
        
        if (prefixLength == 31) {
            // /31: 포인트-투-포인트 링크 - 더 큰 주소가 브로드캐스트 역할
            long network = getNetworkAddress();
            return IpAddress.fromLong(network + 1).toString();
        }
        
        // 일반적인 경우: 네트워크 주소 + (호스트 비트가 모두 1인 값)
        long network = getNetworkAddress();
        int hostBits = 32 - prefixLength;
        long hostMask = (1L << hostBits) - 1; // 호스트 부분의 모든 비트를 1로
        long broadcast = network | hostMask;
        
        return IpAddress.fromLong(broadcast).toString();
    }

    /**
     * CIDR 블록의 서브넷 마스크를 반환합니다.
     * 서브넷 마스크는 네트워크 부분과 호스트 부분을 구분하는 데 사용됩니다.
     * 
     * @return 서브넷 마스크 문자열 (예: "255.255.255.0")
     */
    public String getSubnetMask() {
        long mask = getMask();
        return IpAddress.fromLong(mask).toString();
    }

    /**
     * CIDR 블록의 총 IP 주소 개수를 반환합니다.
     * 이는 네트워크 주소와 브로드캐스트 주소를 포함한 모든 IP 개수입니다.
     * 
     * @return 총 IP 개수 (2^호스트비트수)
     */
    public long getTotalIpCount() {
        int hostBits = 32 - prefixLength;
        return 1L << hostBits;
    }

    /**
     * CIDR 블록의 사용 가능한 호스트 IP 개수를 반환합니다.
     * 
     * 계산 규칙:
     * - /32: 1개 (단일 호스트)
     * - /31: 2개 (포인트-투-포인트 링크, RFC 3021)
     * - 그 외: 총 IP 개수 - 2 (네트워크 주소와 브로드캐스트 주소 제외)
     * 
     * @return 사용 가능한 호스트 IP 개수
     */
    public long getUsableIpCount() {
        if (prefixLength == 32) {
            return 1L; // 단일 호스트
        }
        
        if (prefixLength == 31) {
            return 2L; // 포인트-투-포인트 링크
        }
        
        return getTotalIpCount() - 2L; // 네트워크와 브로드캐스트 제외
    }

    /**
     * CIDR 블록의 첫 번째 사용 가능한 호스트 IP 주소를 반환합니다.
     * 
     * @return 첫 번째 사용 가능한 IP 주소 문자열
     */
    public String getFirstUsableIp() {
        if (prefixLength == 32) {
            // /32: 단일 호스트
            return ipAddress.toString();
        }
        
        if (prefixLength == 31) {
            // /31: 네트워크 주소가 첫 번째 사용 가능한 IP
            return getNetworkAddressString();
        }
        
        // 일반적인 경우: 네트워크 주소 + 1
        long network = getNetworkAddress();
        return IpAddress.fromLong(network + 1).toString();
    }

    /**
     * CIDR 블록의 마지막 사용 가능한 호스트 IP 주소를 반환합니다.
     * 
     * @return 마지막 사용 가능한 IP 주소 문자열
     */
    public String getLastUsableIp() {
        if (prefixLength == 32) {
            // /32: 단일 호스트
            return ipAddress.toString();
        }
        
        if (prefixLength == 31) {
            // /31: 브로드캐스트 주소가 마지막 사용 가능한 IP
            return getBroadcastAddress();
        }
        
        // 일반적인 경우: 브로드캐스트 주소 - 1
        long network = getNetworkAddress();
        int hostBits = 32 - prefixLength;
        long hostMask = (1L << hostBits) - 1;
        long broadcast = network | hostMask;
        
        return IpAddress.fromLong(broadcast - 1).toString();
    }

    /**
     * CIDR 블록의 상세 정보를 반환합니다.
     * 네트워크 주소, 브로드캐스트 주소, 서브넷 마스크, IP 개수 등을 포함합니다.
     * 
     * @return CIDR 블록 상세 정보 문자열
     */
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("CIDR: ").append(toString()).append("\n");
        sb.append("네트워크 주소: ").append(getNetworkAddressString()).append("\n");
        sb.append("브로드캐스트 주소: ").append(getBroadcastAddress()).append("\n");
        sb.append("서브넷 마스크: ").append(getSubnetMask()).append("\n");
        sb.append("첫 번째 사용가능 IP: ").append(getFirstUsableIp()).append("\n");
        sb.append("마지막 사용가능 IP: ").append(getLastUsableIp()).append("\n");
        sb.append("총 IP 개수: ").append(getTotalIpCount()).append("\n");
        sb.append("사용가능 IP 개수: ").append(getUsableIpCount());
        
        return sb.toString();
    }

    /**
     * CIDR의 prefix length를 반환합니다.
     * 
     * @return prefix length (0-32)
     */
    public int getPrefixLength() {
        return prefixLength;
    }

    @Override
    public String toString() {
        return ipAddress.toString() + "/" + prefixLength;
    }
}
