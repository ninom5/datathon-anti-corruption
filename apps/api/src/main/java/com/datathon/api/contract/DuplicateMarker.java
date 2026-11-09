package com.datathon.api.contract;

import com.datathon.api.ingest.IssueCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class DuplicateMarker {

    private record Key(Long buyerId, Long supplierId, LocalDate dateSigned, BigDecimal initialValue, String evidNumber,
                       String contractNumber) {
    }

    private DuplicateMarker() {
    }

    public static int mark(List<Contract> contracts) {
        List<Contract> byId = new ArrayList<>(contracts);
        byId.sort(Comparator.comparing(Contract::getId, Comparator.nullsLast(Comparator.naturalOrder())));
        Set<Key> seen = new HashSet<>();
        int marked = 0;
        for (Contract contract : byId) {
            boolean isDuplicate = isComparable(contract) && !seen.add(keyOf(contract));
            setFlag(contract, isDuplicate);
            marked += isDuplicate ? 1 : 0;
        }
        return marked;
    }

    public static boolean isMarked(Contract contract) {
        return contract.getQualityFlags().contains(IssueCode.POSSIBLE_DUPLICATE);
    }

    private static boolean isComparable(Contract contract) {
        return contract.getContractNumber() != null && !contract.getContractNumber().isBlank()
                && contract.getInitialValue() != null && contract.getSupplier() != null;
    }

    private static Key keyOf(Contract contract) {
        return new Key(contract.getBuyer().getId(), contract.getSupplier().getId(), contract.getDateSigned(),
                contract.getInitialValue().stripTrailingZeros(), normalize(contract.getEvidNumber()),
                normalize(contract.getContractNumber()));
    }

    private static void setFlag(Contract contract, boolean isDuplicate) {
        boolean hasFlag = isMarked(contract);
        if (isDuplicate == hasFlag) {
            return;
        }
        List<String> flags = new ArrayList<>(contract.getQualityFlags());
        if (isDuplicate) {
            flags.add(IssueCode.POSSIBLE_DUPLICATE);
        } else {
            flags.remove(IssueCode.POSSIBLE_DUPLICATE);
        }
        contract.setQualityFlags(flags);
    }

    private static String normalize(String text) {
        return text == null ? "" : text.strip().replaceAll("\s+", " ").toLowerCase(Locale.ROOT);
    }
}
