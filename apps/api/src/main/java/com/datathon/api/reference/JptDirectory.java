package com.datathon.api.reference;

import com.datathon.api.ingest.normalize.NameNormalizer;
import com.datathon.api.ingest.normalize.Oibs;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lookups in the JPT register used by ingest and rules. */
@Service
@RequiredArgsConstructor
public class JptDirectory {

    private final JptBodyRepository jptBodyRepository;

    /**
     * OIB of a buyer known only by name (old EOJN report). Only an exact match of the normalised name that is unique
     * in JPT counts; e.g. two municipalities named "Privlaka" give no match rather than a guess.
     */
    @Transactional(readOnly = true)
    public Oibs.Result findOibByName(String name) {
        List<JptBody> matches = jptBodyRepository.findByNameNorm(NameNormalizer.normalize(name));
        return matches.size() == 1 ? Oibs.read(matches.getFirst().getOib(), false) : new Oibs.Result(null, Oibs.Status.MISSING);
    }

    /**
     * True when JPT lists the buyer as a founder of the supplier (e.g. a municipal utility company). Context
     * "informativno" for rules C and F; false also when the supplier is not in JPT at all.
     */
    @Transactional(readOnly = true)
    public boolean isSupplierFoundedByBuyer(String supplierOib, String buyerOib) {
        if (supplierOib == null || buyerOib == null) {
            return false;
        }
        return jptBodyRepository.isFoundedBy(supplierOib, buyerOib);
    }
}
