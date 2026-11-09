package com.datathon.api.rules;

import com.datathon.api.contract.Contract;
import com.datathon.api.organization.Organization;
import java.util.List;
import java.util.Set;

public record BuyerContracts(Organization buyer, List<Contract> contracts, Set<String> supplierFounderPairs) {

    public boolean isFoundedByBuyer(Organization supplier) {
        if (supplier == null || supplier.getOib() == null || buyer.getOib() == null) {
            return false;
        }
        return supplierFounderPairs.contains(supplier.getOib() + "|" + buyer.getOib());
    }
}
