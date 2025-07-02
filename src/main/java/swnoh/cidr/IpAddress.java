package swnoh.cidr;

public class IpAddress {
    private final int[] octets;
    
    private IpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("IP address cannot be null or empty");
        }
        
        this.octets = parseAndValidate(ipAddress.trim());
    }
    
    private IpAddress(int[] octets) {
        this.octets = octets.clone();
    }

    /**
     * IP 주소 문자열을 파싱하고 유효성을 검사합니다.
     *
     * @param ipAddress
     * @return
     */
    private int[] parseAndValidate(String ipAddress) {
        String[] parts = ipAddress.split("\\.");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid IP address format: " + ipAddress);
        }
        
        int[] result = new int[4];
        for (int i = 0; i < 4; i++) {
            try {
                int octet = Integer.parseInt(parts[i]);
                if (octet < 0 || octet > 255) {
                    throw new IllegalArgumentException(
                        "Invalid IP octet: " + octet + ". Must be between 0 and 255");
                }
                result[i] = octet;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    "Invalid IP octet: " + parts[i] + ". Must be a number");
            }
        }
        return result;
    }

    /**
     * IP 주소를 long 타입으로 변환합니다.
     *
     * @return
     */
    public long toLong() {
        return ((long) octets[0] << 24) | 
               ((long) octets[1] << 16) | 
               ((long) octets[2] << 8) | 
               ((long) octets[3]);
    }


    public static IpAddress fromString(String ipAddress) {
        return new IpAddress(ipAddress);
    }
    
    public static IpAddress fromLong(long ipAsLong) {
        if (ipAsLong < 0 || ipAsLong > 4294967295L) {
            throw new IllegalArgumentException("IP address as long must be between 0 and 4294967295");
        }
        
        int[] octets = new int[4];
        octets[0] = (int) ((ipAsLong >> 24) & 0xFF);
        octets[1] = (int) ((ipAsLong >> 16) & 0xFF);
        octets[2] = (int) ((ipAsLong >> 8) & 0xFF);
        octets[3] = (int) (ipAsLong & 0xFF);
        
        return new IpAddress(octets);
    }
    
    @Override
    public String toString() {
        return String.format("%d.%d.%d.%d", octets[0], octets[1], octets[2], octets[3]);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        IpAddress ipAddress = (IpAddress) obj;
        return java.util.Arrays.equals(octets, ipAddress.octets);
    }
    
    @Override
    public int hashCode() {
        return java.util.Arrays.hashCode(octets);
    }
} 