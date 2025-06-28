package swnoh.cidr;

public class CidrBlock {
    private final String networkAddress;
    private final int prefixLength;
    
    public CidrBlock(String cidr) {
        if (cidr == null || cidr.trim().isEmpty()) {
            throw new IllegalArgumentException("CIDR cannot be null or empty");
        }
        
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid CIDR format. Expected format: x.x.x.x/y");
        }
        
        this.networkAddress = parts[0].trim();

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
        // IP 주소를 점으로 분리
        String[] ipParts = networkAddress.split("\\.");
        if (ipParts.length != 4) {
            throw new IllegalArgumentException("Invalid IP address format");
        }
        
        // 각 부분을 정수로 변환
        int[] octets = new int[4];
        for (int i = 0; i < 4; i++) {
            try {
                octets[i] = Integer.parseInt(ipParts[i]);
                if (octets[i] < 0 || octets[i] > 255) {
                    throw new IllegalArgumentException("Invalid IP octet: " + octets[i]);
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid IP octet: " + ipParts[i]);
            }
        }
        
        // IP를 32비트 정수로 변환
        long ipAsLong = (long) octets[0] << 24 | 
                        (long) octets[1] << 16 | 
                        (long) octets[2] << 8 | 
                        (long) octets[3];
        
        // 서브넷 마스크 생성 (prefixLength만큼 1로 채우고 나머지는 0)
        long mask = (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;
        
        // 네트워크 주소 계산 (IP와 마스크의 AND 연산)
        long networkLong = ipAsLong & mask;
        
        // 다시 IP 주소 형태로 변환
        int[] normalizedOctets = new int[4];
        normalizedOctets[0] = (int) ((networkLong >> 24) & 0xFF);
        normalizedOctets[1] = (int) ((networkLong >> 16) & 0xFF);
        normalizedOctets[2] = (int) ((networkLong >> 8) & 0xFF);
        normalizedOctets[3] = (int) (networkLong & 0xFF);
        
        return String.format("%d.%d.%d.%d/%d", 
                            normalizedOctets[0],
                            normalizedOctets[1],
                            normalizedOctets[2],
                            normalizedOctets[3],
                            prefixLength);
    }
    
    public String getNetworkAddress() {
        return networkAddress;
    }
    
    public int getPrefixLength() {
        return prefixLength;
    }
    
    @Override
    public String toString() {
        return networkAddress + "/" + prefixLength;
    }
}
