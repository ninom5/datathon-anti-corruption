package com.datathon.api.organization;

import com.datathon.api.organization.dto.BuyerOption;
import com.datathon.api.organization.dto.BuyerProfile;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/buyers")
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerService buyerService;
    private final BuyerProfileService buyerProfileService;

    @GetMapping
    public List<BuyerOption> list(@RequestParam(defaultValue = "false") boolean synthetic) {
        return buyerService.listWithContracts(synthetic);
    }

    @GetMapping("/{oib}")
    public BuyerProfile profile(@PathVariable @Pattern(regexp = "\\d{11}", message = "OIB mora imati 11 znamenki") String oib) {
        return buyerProfileService.getByOib(oib);
    }
}
