package com.datathon.api.organization;

import com.datathon.api.organization.dto.BuyerOption;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Buyers that have at least one contract, for the explorer's buyer filter. The list is short (one row per publishing
 * buyer), so it is not paginated.
 */
@RestController
@RequestMapping("/api/v1/buyers")
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerService buyerService;

    @GetMapping
    public List<BuyerOption> list(@RequestParam(defaultValue = "false") boolean synthetic) {
        return buyerService.listWithContracts(synthetic);
    }
}
