package com.datathon.api.rules;

import com.datathon.api.contract.Contract;
import com.datathon.api.organization.Organization;
import java.util.List;
import java.util.Set;

/**
 * Input of a group rule: one buyer and its contracts that rules may see (no natural persons, GDPR).
 *
 * @param supplierFounderPairs "supplierOib|founderOib" from JPT founders, to recognise the buyer's own companies
 */
public record BuyerContracts(Organization buyer, List<Contract> contracts, Set<String> supplierFounderPairs) {

    /** True when JPT lists the buyer as a founder of the supplier (e.g. a municipal utility company). */
    public boolean isFoundedByBuyer(Organization supplier) {
        if (supplier == null || supplier.getOib() == null || buyer.getOib() == null) {
            return false;
        }
        return supplierFounderPairs.contains(supplier.getOib() + "|" + buyer.getOib());
    }
}
