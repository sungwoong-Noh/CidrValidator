package swnoh;

import swnoh.cidr.IpAddress;
import swnoh.cidr.CidrBlock;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== IP to Long 변환 예시 ===");
        
        // 다양한 IP 주소들의 long 변환 결과
        String[] ips = {"0.0.0.0", "192.168.1.1", "192.168.1.100", "255.255.255.255"};
        
        for (String ip : ips) {
            IpAddress ipAddr = IpAddress.fromString(ip);
            long longValue = ipAddr.toLong();
            System.out.printf("%s -> %d\n", ip, longValue);
        }
        
        System.out.println("\n=== CIDR 정규화에서 long 활용 ===");
        
        // 정규화 과정에서 long이 어떻게 사용되는지
        CidrBlock cidr = new CidrBlock("192.168.1.100/24");
        System.out.printf("원본 CIDR: %s\n", cidr.toString());
        System.out.printf("정규화 결과: %s\n", cidr.normalize());
        
        // IP 주소를 long으로 변환해서 비트 연산 시연
        IpAddress originalIp = IpAddress.fromString("192.168.1.100");
        long ipAsLong = originalIp.toLong();
        long mask = 0xFFFFFF00L; // /24 마스크 (255.255.255.0)
        long networkLong = ipAsLong & mask;
        
        System.out.printf("\n=== 비트 연산 과정 ===\n");
        System.out.printf("원본 IP: %s (%d)\n", originalIp.toString(), ipAsLong);
        System.out.printf("서브넷 마스크: /24 (%d)\n", mask);
        System.out.printf("네트워크 주소: %s (%d)\n", 
                         IpAddress.fromLong(networkLong).toString(), networkLong);
    }
}