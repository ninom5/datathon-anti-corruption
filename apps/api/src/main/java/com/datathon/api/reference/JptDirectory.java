package com.datathon.api.reference;

import com.datathon.api.ingest.normalize.NameNormalizer;
import com.datathon.api.ingest.normalize.Oibs;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JptDirectory {

    private final JptBodyRepository jptBodyRepository;

    @Transactional(readOnly = true)
    public Oibs.Result findOibByName(String name) {
        List<JptBody> matches = jptBodyRepository.findByNameNorm(NameNormalizer.normalize(name));
        return matches.size() == 1 ? Oibs.read(matches.getFirst().getOib(), false) : new Oibs.Result(null, Oibs.Status.MISSING);
    }

    @Transactional(readOnly = true)
    public boolean isSupplierFoundedByBuyer(String supplierOib, String buyerOib) {
        if (supplierOib == null || buyerOib == null) {
            return false;
        }
        return jptBodyRepository.isFoundedBy(supplierOib, buyerOib);
    }
}
