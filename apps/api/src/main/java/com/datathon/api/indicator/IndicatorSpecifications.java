package com.datathon.api.indicator;

import com.datathon.api.indicator.dto.IndicatorFilter;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class IndicatorSpecifications {

    private IndicatorSpecifications() {
    }

    public static Specification<Indicator> matching(IndicatorFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), IndicatorStatus.ACTIVE));
            if (!filter.includesSynthetic()) {
                predicates.add(cb.isFalse(root.get("buyer").get("isSynthetic")));
            }
            if (!filter.ruleCodes().isEmpty()) {
                predicates.add(root.get("ruleCode").in(filter.ruleCodes()));
            }
            if (filter.buyerOib() != null) {
                predicates.add(cb.equal(root.get("buyer").get("oib"), filter.buyerOib()));
            }
            if (filter.band() != null) {
                predicates.add(cb.equal(root.get("magnitudeBand"), MagnitudeBand.fromCode(filter.band()).orElseThrow()));
            }
            if (filter.severity() != null) {
                predicates.add(cb.equal(root.get("severityNote"), SeverityNote.fromCode(filter.severity())));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
