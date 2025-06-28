package swnoh.cidr;

public class CidrBlock {
    private final IpAddress ipAddress;
    private final int prefixLength;
    
    public CidrBlock(String cidr) {
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
        long mask = (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;
        
        // 네트워크 주소 계산 (IP와 마스크의 AND 연산)
        long networkLong = ipAsLong & mask;
        
        // 정규화된 IP 주소 생성
        IpAddress normalizedIp = IpAddress.fromLong(networkLong);
        
        return normalizedIp.toString() + "/" + prefixLength;
    }
    
    public String getNetworkAddress() {
        return ipAddress.toString();
    }
    
    public int getPrefixLength() {
        return prefixLength;
    }
    
    @Override
    public String toString() {
        return ipAddress.toString() + "/" + prefixLength;
    }
}
