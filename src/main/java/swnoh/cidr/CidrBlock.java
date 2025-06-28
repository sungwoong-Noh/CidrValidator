package swnoh.cidr;

public class CidrBlock {
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
     * CIDR 블록의 서브넷 마스크를 계산합니다.
     *
     * @return 서브넷 마스크 (long 형식)
     */
    private long getMask() {
        return (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;
    }

    @Override
    public String toString() {
        return ipAddress.toString() + "/" + prefixLength;
    }
}
