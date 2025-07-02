package swnoh.cidr;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class CidrUtilsTest {

    @Test
    @DisplayName("CIDR 병합 - 기본케이스(/26 4개 -> /24 1개")
    void testMerge_Basic() {

        List<CidrBlock> cidrs = Arrays.asList(
                CidrBlock.of("192.168.1.0/26"),
                CidrBlock.of("192.168.1.64/26"),
                CidrBlock.of("192.168.1.128/26"),
                CidrBlock.of("192.168.1.192/26")
        );

        List<CidrBlock> merge = CidrUtils.merge(cidrs);

        assertEquals(1, merge.size());
        assertEquals("192.168.1.0/24", merge.get(0).toString());
    }

    @Test
    @DisplayName("CIDR 병합 - 순서가 뒤바뀐 경우")
    void testMerge_UnorderedInput() {

        List<CidrBlock> cidrs = Arrays.asList(
                CidrBlock.of("192.168.1.192/26"),
                CidrBlock.of("192.168.1.0/26"),
                CidrBlock.of("192.168.1.128/26"),
                CidrBlock.of("192.168.1.64/26")
        );

        List<CidrBlock> merge = CidrUtils.merge(cidrs);

        assertEquals(1, merge.size());
        assertEquals("192.168.1.0/24", merge.get(0).toString());
    }

    @Test
    @DisplayName("CIDR 병합 - /27 2ro -> /26 1개")
    void testMerge_27to26() {

        List<CidrBlock> cidrs = Arrays.asList(
                CidrBlock.of("10.0.0.0/27"),
                CidrBlock.of("10.0.0.32/27")
        );

        List<CidrBlock> merged = CidrUtils.merge(cidrs);

        assertEquals(1, merged.size());
        assertEquals("10.0.0.0/26", merged.get(0).toString());
    }

    @Test
    @DisplayName("CIDR 병합 - 부분 병합 (일부만 병합 가능)")
    void testMerge_PartialMerge() {
        List<CidrBlock> cidrs = Arrays.asList(
                CidrBlock.of("192.168.1.0/26"),
                CidrBlock.of("192.168.1.64/26"),  // 위와 병합 가능
                CidrBlock.of("192.168.2.0/26"),   // 별도의 네트워크
                CidrBlock.of("192.168.2.64/26")   // 위와 병합 가능
        );

        List<CidrBlock> merged = CidrUtils.merge(cidrs);

        assertEquals(2, merged.size());
        assertEquals(merged.get(0).toString(), "192.168.1.0/25");
        assertEquals(merged.get(1).toString(), "192.168.2.0/25");
    }

    @Test
    @DisplayName("CIDR 병합 - 병합 불가능한 경우")
    void testMerge_NotMergeable() {

        List<CidrBlock> cidrs = Arrays.asList(
                CidrBlock.of("192.168.1.0/26"),
                CidrBlock.of("192.168.1.128/26"),  // 인접하지 않음 (64 건너뜀)
                CidrBlock.of("10.0.0.0/24")        // 완전히 다른 네트워크
        );

        List<CidrBlock> merged = CidrUtils.merge(cidrs);

        assertEquals(3, merged.size());
        assertEquals(merged.get(0).toString(), "10.0.0.0/24");
        assertEquals(merged.get(1).toString(), "192.168.1.0/26");
        assertEquals(merged.get(2).toString(), "192.168.1.128/26");
    }

    @Test
    @DisplayName("CIDR 병합 - 다른 Prefix length 혼재")
    void testMerge_DifferentPrefixLengths() {
        List<CidrBlock> cidrs = Arrays.asList(
                CidrBlock.of("192.168.1.0/26"),
                CidrBlock.of("192.168.1.64/27"),  // 다른 prefix length
                CidrBlock.of("192.168.1.96/27")   // 위와 병합되어 /26이 될 수 있음
        );

        List<CidrBlock> merged = CidrUtils.merge(cidrs);

        // 세부 병합 로직에 따라 결과가 달라질 수 있음
        assertEquals(merged.size(), 1);
    }


}