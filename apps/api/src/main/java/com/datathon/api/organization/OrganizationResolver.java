package com.datathon.api.organization;

import com.datathon.api.ingest.normalize.NameNormalizer;
import com.datathon.api.ingest.normalize.Oibs;
import com.datathon.api.ingest.normalize.OrganizationClassifier;
import com.datathon.api.source.Provenance;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationResolver {

    private static final String NAME_NOT_PUBLISHED = "(naziv nije objavljen)";

    private final OrganizationRepository organizationRepository;

    public Organization resolveBuyer(String name, Oibs.Result oib, boolean isSynthetic, Provenance provenance) {
        String displayName = name == null || name.isBlank() ? NAME_NOT_PUBLISHED : name.strip();
        return resolve(displayName, oib, isSynthetic, provenance, OrganizationClassifier.classifyBuyer(displayName));
    }

    public Optional<Organization> resolveSupplier(String name, Oibs.Result oib, boolean isSynthetic, Provenance provenance) {
        if ((name == null || name.isBlank()) && !oib.isUsable()) {
            return Optional.empty();
        }
        String displayName = name == null || name.isBlank() ? NAME_NOT_PUBLISHED : name.strip();
        return Optional.of(resolve(displayName, oib, isSynthetic, provenance, OrganizationClassifier.classifySupplier(displayName)));
    }

    private Organization resolve(String name, Oibs.Result oib, boolean isSynthetic, Provenance provenance, OrganizationType type) {
        String nameNorm = NameNormalizer.normalize(name);
        Optional<Organization> existing = oib.isUsable()
                ? organizationRepository.findByOib(oib.oib())
                : organizationRepository.findFirstByOibIsNullAndNameNormAndIsSynthetic(nameNorm, isSynthetic);
        return existing.orElseGet(() -> create(name, nameNorm, oib, isSynthetic, provenance, type));
    }

    private Organization create(String name, String nameNorm, Oibs.Result oib, boolean isSynthetic,
                                Provenance provenance, OrganizationType type) {
        Organization organization = new Organization(oib.oib(), name, nameNorm, type);
        organization.setSynthetic(isSynthetic);
        organization.setProvenance(provenance);
        return organizationRepository.save(organization);
    }
}
