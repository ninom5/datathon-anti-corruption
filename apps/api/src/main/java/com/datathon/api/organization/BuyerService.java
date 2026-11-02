package com.datathon.api.organization;

import com.datathon.api.organization.dto.BuyerOption;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BuyerService {

    private final OrganizationRepository organizationRepository;

    @Transactional(readOnly = true)
    public List<BuyerOption> listWithContracts(boolean includeSynthetic) {
        return organizationRepository.findBuyersWithContractCount(includeSynthetic);
    }
}
