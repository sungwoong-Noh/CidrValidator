package swnoh.cidr;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CidrUtils {


    /**
     * 1. 인접한 CIDR 브록들을 병합하여 더 큰 CIDR 블록으로 결합한다.
     *
     * @param cidrBlocks 병합할 CIDR 블록들의 리스트
     * @return 병합된 CIDR 블록들의 리스트
     */
    public static List<CidrBlock> merge(List<CidrBlock> cidrBlocks) {

        if (cidrBlocks == null || cidrBlocks.isEmpty()) {
            return new ArrayList<>();
        }


        if (cidrBlocks.size() == 1) {
            return new ArrayList<>(cidrBlocks);
        }

        // 1. CIDR 블록을 정규화하고 정렬합니다.
        List<CidrBlock> normalized = cidrBlocks.stream()
                .map(cidr -> CidrBlock.of(cidr.normalize()))
                .sorted(new CidrComparator())
                .toList();

        // 2. 재귀적으로 병합 수행
        return performMerge(normalized);
    }

    /**
     * 2. 실제 병합 작업을 수행한다 : 그룹핑 + 병합
     */
    private static List<CidrBlock> performMerge(List<CidrBlock> cidrBlocks) {

        List<CidrBlock> result = new ArrayList<>();
        List<CidrBlock> currentGroup = new ArrayList<>();

        for (CidrBlock cidrBlock : cidrBlocks) {

            if (currentGroup.isEmpty()) {
                currentGroup.add(cidrBlock);
            } else {
                // 현재 그룹과 병합 가능한지 확인 : 현재 그룹의 마지막 요소와 cidr 비교
                if (canMergeWithGroup(currentGroup, cidrBlock)) {
                    currentGroup.add(cidrBlock);
                } else {
                    // 현재 그룹 병합 시도
                    result.addAll(mergeGroup(currentGroup));
                    currentGroup.clear();
                    currentGroup.add(cidrBlock);
                }
            }
        }

        // 마지막 그룹 병합
        if (!currentGroup.isEmpty()) {
            result.addAll(mergeGroup(currentGroup));
        }

        // 추가 병합이 가능한지 확인(재귀)
        if (result.size() < cidrBlocks.size() && result.size() > 1) {
            return performMerge(result);
        }

        return result;
    }

    /**
     * 그룹 내의 CIDR들을 병합한다.
     */
    private static Collection<? extends CidrBlock> mergeGroup(List<CidrBlock> group) {

        if (group.size() < 2) {
            return new ArrayList<>(group);
        }

        // 2의 거듭제곱 개수로만 병합 가능
        List<CidrBlock> result = new ArrayList<>();

        // 병합 가능한 크기들을 찾아서 처리
        int i = 0;

        while (i < group.size()) {
            int mergeSize = findLargestMergeableSize(group, i);

            if (mergeSize > 1) {
                // 병합 수행
                CidrBlock merged = mergeContinuousGroup(group.subList(i, i + mergeSize));
                result.add(merged);
                i += mergeSize;
            } else {
                // 병합할 수 없는 경우, 단일 CIDR로 추가
                result.add(group.get(i));
                i++;
            }
        }

        return result;
    }

    /**
     * 연속된 CIDR 블록들을 병합한다.
     */
    private static CidrBlock mergeContinuousGroup(List<CidrBlock> group) {

        if (group.size() == 1) {
            return group.get(0);
        }

        CidrBlock first = group.get(0);
        int newPrefixLength = first.getPrefixLength() - Integer.numberOfTrailingZeros(group.size()); // 병합하려는 prefix 길이 계산

        long networkAddress = getNetworkAddressAsLong(first);
        IpAddress newNetworkIp = IpAddress.fromLong(networkAddress);

        return CidrBlock.of(newNetworkIp.toString() + "/" + newPrefixLength);
    }

    /**
     * 주어진 위치에서 시작하여 병합 가능한 최대 크기를 찾는다.
     */
    private static int findLargestMergeableSize(List<CidrBlock> group, int startIndex) {

        int maxSize = 1;

        // 2의 제곱 순으로 확인
        for (int size = 2; size <= (group.size() - startIndex); size *= 2) {

            if (canMergeContinuousGroup(group.subList(startIndex, startIndex + size))) {
                maxSize = size;
            } else {
                break; // 더 이상 병합할 수 없으면 중단
            }

        }

        return maxSize;
    }

    /**
     * 연속된 CIDR 그룹이 병합 가능한지 확인한다.
     */
    private static boolean canMergeContinuousGroup(List<CidrBlock> group) {
        if (group.size() < 2 || (group.size() & (group.size() - 1)) != 0) {
            return false; // 2의 거듭제곱이 아니면 병합 불가
        }

        // 모든 CIDR가 같은 prefix를 가지고 있는지 확인
        int prefixLength = group.get(0).getPrefixLength();
        for (CidrBlock cidrBlock : group) {
            if (cidrBlock.getPrefixLength() != prefixLength) {
                return false; // prefix가 다르면 병합 불가
            }
        }

        // 연속적인지 확인
        for (int i = 0; i < group.size() - 1; i++) {
            if (!areAdjacent(group.get(i), group.get(i + 1))) {
                return false;
            }
        }

        // 병합 후 올바른 경계에 정렬되는지 확인
        long firstNetwork = getNetworkAddressAsLong(group.get(0));
        int newPrefixLength = prefixLength - Integer.numberOfTrailingZeros(group.size());
        long newCidrSize = 1L << (32 - newPrefixLength);

        return (firstNetwork % newCidrSize) == 0;
    }

    /**
     * CIDR이 현재 그룹과 병합 가능한지 확인한다.
     */
    private static boolean canMergeWithGroup(List<CidrBlock> currentGroup, CidrBlock cidrBlock) {

        if (currentGroup.isEmpty()) {
            return true;
        }

        CidrBlock last = currentGroup.get(currentGroup.size() - 1);

        // 같은 prefix여야 함.
        if (last.getPrefixLength() != cidrBlock.getPrefixLength()) {
            return false;
        }

        // 인접한 주소 범위인지 확인
        return areAdjacent(last, cidrBlock);
    }

    /**
     * 두 CIDR가 인접한지 확인한다.
     *
     * @return
     */
    private static boolean areAdjacent(CidrBlock cidr1, CidrBlock cidr2) {
        long network1 = getNetworkAddressAsLong(cidr1);
        long network2 = getNetworkAddressAsLong(cidr2);

        // 두 네트워크가 같은 대역인지 검사한다.
        int hostBits = 32 - cidr1.getPrefixLength();
        long cidrSize = 1L << hostBits; // CIDR 블록의 크기

        return (network2 - network1) == cidrSize;
    }


    /**
     * CIDR의 네트워크 주소를 long으로 반환합니다.
     */
    private static long getNetworkAddressAsLong(CidrBlock cidr) {
        String normalized = cidr.normalize();
        String networkIp = normalized.split("/")[0];
        return IpAddress.fromString(networkIp).toLong();
    }

    /**
     * CIDR 정렬을 위한 Comparator 클래스.
     */
    private static class CidrComparator implements java.util.Comparator<CidrBlock> {
        @Override
        public int compare(CidrBlock c1, CidrBlock c2) {

            long network1 = getNetworkAddressAsLong(c1);
            long network2 = getNetworkAddressAsLong(c2);

            int networkComparison = Long.compare(network1, network2);

            if (networkComparison != 0) {
                return networkComparison;
            }

            return Integer.compare(c2.getPrefixLength(), c1.getPrefixLength());
        }
    }


}
