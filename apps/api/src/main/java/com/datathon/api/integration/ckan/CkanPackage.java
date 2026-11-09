package com.datathon.api.integration.ckan;

import java.time.Instant;
import java.util.List;

public record CkanPackage(String name, String title, String organizationTitle, String licenseId,
                          Instant metadataModified, List<CkanResource> resources) {

    public String landingUrl() {
        return "https://data.gov.hr/dataset/" + name;
    }
}
