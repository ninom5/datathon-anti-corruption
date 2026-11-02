package com.datathon.api.contract;

import com.datathon.api.common.PageResponse;
import com.datathon.api.contract.dto.ContractDetail;
import com.datathon.api.contract.dto.ContractFilter;
import com.datathon.api.contract.dto.ContractListItem;
import com.datathon.api.contract.dto.ContractSummary;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Contract explorer (docs/UI_UX.md §3.2) and contract detail. */
@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping
    public PageResponse<ContractListItem> list(@Valid @ModelAttribute ContractFilter filter) {
        return contractService.find(filter);
    }

    @GetMapping("/summary")
    public ContractSummary summary(@Valid @ModelAttribute ContractFilter filter) {
        return contractService.summarize(filter);
    }

    @GetMapping("/{id}")
    public ContractDetail get(@PathVariable Long id) {
        return contractService.getById(id);
    }
}
