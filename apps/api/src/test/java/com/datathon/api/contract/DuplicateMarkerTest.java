package com.datathon.api.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.ingest.IssueCode;
import com.datathon.api.organization.Organization;
import com.datathon.api.organization.OrganizationType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Re-entries of one contract (Grad Lepoglava, J-107/25: two EOJN links, every other field equal). */
class DuplicateMarkerTest {

    private static final Organization BUYER = organization(1L);
    private static final Organization SUPPLIER = organization(2L);

    @Test
    void marksLaterReEntryOnly() {
        Contract first = contract(10L, "21/2025-GRAD", "4162.00");
        Contract second = contract(20L, "21/2025-GRAD", "4162");
        Contract otherNumber = contract(30L, "22/2025-GRAD", "4162.00");

        assertThat(DuplicateMarker.mark(List.of(second, first, otherNumber))).isEqualTo(1);
        assertThat(DuplicateMarker.isMarked(first)).isFalse();
        assertThat(DuplicateMarker.isMarked(second)).isTrue();
        assertThat(second.getQualityFlags()).containsExactly(IssueCode.POSSIBLE_DUPLICATE);
        assertThat(DuplicateMarker.isMarked(otherNumber)).as("another contract number is another contract").isFalse();
    }

    @Test
    void neverMergesRowsWithoutContractNumberAndClearsStaleFlag() {
        Contract first = contract(10L, null, "1000");
        Contract second = contract(20L, null, "1000");
        second.setQualityFlags(List.of(IssueCode.POSSIBLE_DUPLICATE));

        assertThat(DuplicateMarker.mark(List.of(first, second))).isZero();
        assertThat(second.getQualityFlags()).isEmpty();
    }

    private static Contract contract(Long id, String contractNumber, String value) {
        Contract contract = new Contract("https://eojn.hr/contract-eo/" + id);
        contract.setId(id);
        contract.setBuyer(BUYER);
        contract.setSupplier(SUPPLIER);
        contract.setDateSigned(LocalDate.of(2025, 2, 20));
        contract.setSubject("Nabava  čipke");
        contract.setEvidNumber("J-107/25");
        contract.setContractNumber(contractNumber);
        contract.setInitialValue(new BigDecimal(value));
        return contract;
    }

    private static Organization organization(Long id) {
        Organization organization = new Organization("0000000000" + id, "Org " + id, "org " + id, OrganizationType.LLC);
        organization.setId(id);
        return organization;
    }
}
