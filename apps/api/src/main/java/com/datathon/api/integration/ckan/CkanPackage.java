package com.datathon.api.integration.ckan;

import java.time.Instant;
import java.util.List;

/** A CKAN package (dataset). {@code name} is the stable slug used in https://data.gov.hr/dataset/{name}. */
public record CkanPackage(String name, String title, String organizationTitle, String licenseId,
                          Instant metadataModified, List<CkanResource> resources) {

    public String landingUrl() {
        return "https://data.gov.hr/dataset/" + name;
    }
}
