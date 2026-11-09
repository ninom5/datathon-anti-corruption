package com.datathon.api.municipality;

import com.datathon.api.municipality.dto.MunicipalityOverview;
import com.datathon.api.municipality.dto.PeerComparison;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/municipalities")
@RequiredArgsConstructor
public class MunicipalityController {

    private final MunicipalityService municipalityService;

    @GetMapping
    public List<MunicipalityOverview> list() {
        return municipalityService.listAll();
    }

    @GetMapping("/{oib}/peers")
    public PeerComparison peers(
            @PathVariable @Pattern(regexp = "\\d{11}", message = "OIB mora imati 11 znamenki") String oib,
            @RequestParam(required = false) @Min(value = 2000, message = "Godina mora biti 2000. ili kasnija")
            @Max(value = 2100, message = "Godina nije ispravna") Integer year,
            @RequestParam(required = false) @Pattern(regexp = "\\d{2}", message = "CPV odjeljak ima dvije znamenke") String cpv2) {
        return municipalityService.comparePeers(oib, year, cpv2);
    }
}
