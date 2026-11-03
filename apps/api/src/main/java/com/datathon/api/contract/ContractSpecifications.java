package com.datathon.api.contract;

import com.datathon.api.contract.dto.ContractFilter;
import com.datathon.api.indicator.Indicator;
import com.datathon.api.indicator.IndicatorStatus;
import com.datathon.api.indicator.MagnitudeBand;
import com.datathon.api.indicator.SeverityNote;
import com.datathon.api.organization.Organization;
import com.datathon.api.organization.OrganizationType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/** Filters of the contract explorer as JPA criteria, shared by the list and the summary. */
public final class ContractSpecifications {

    private ContractSpecifications() {
    }

    /** Everything the explorer may show for the filter. Contracts with natural persons are never shown (GDPR). */
    public static Specification<Contract> visible(ContractFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(notWithNaturalPerson(root, cb));
            if (!filter.includesSynthetic()) {
                predicates.add(cb.isFalse(root.get("isSynthetic")));
            }
            if (filter.buyerOib() != null) {
                predicates.add(cb.equal(root.get("buyer").get("oib"), filter.buyerOib()));
            }
            if (filter.supplierOib() != null) {
                predicates.add(cb.equal(root.get("supplier").get("oib"), filter.supplierOib()));
            }
            if (filter.year() != null) {
                predicates.add(cb.between(root.get("dateSigned"), LocalDate.of(filter.year(), 1, 1),
                        LocalDate.of(filter.year(), 12, 31)));
            }
            if (filter.hasIndicator() != null) {
                Predicate hasIndicator = cb.exists(activeIndicator(root, query, cb, null));
                predicates.add(filter.hasIndicator() ? hasIndicator : cb.not(hasIndicator));
            }
            if (filter.severity() != null || !filter.ruleCodes().isEmpty() || filter.band() != null) {
                IndicatorCriteria criteria = new IndicatorCriteria(
                        filter.severity() == null ? null : SeverityNote.fromCode(filter.severity()), filter.ruleCodes(),
                        filter.band() == null ? null : MagnitudeBand.fromCode(filter.band()).orElseThrow());
                predicates.add(cb.exists(activeIndicator(root, query, cb, criteria)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    public static Specification<Contract> realOnly() {
        return (root, query, cb) -> cb.isFalse(root.get("isSynthetic"));
    }

    public static Specification<Contract> syntheticOnly() {
        return (root, query, cb) -> cb.isTrue(root.get("isSynthetic"));
    }

    /** Crafts are left out of summaries and top lists, because a craft's name usually names its owner. */
    public static Specification<Contract> withoutCrafts() {
        return (root, query, cb) -> notSupplierType(root, cb, OrganizationType.CRAFT);
    }

    public static Specification<Contract> withCraft() {
        return (root, query, cb) -> cb.equal(root.get("supplier").get("orgType"), OrganizationType.CRAFT);
    }

    public static Specification<Contract> withIndicator(SeverityNote severity) {
        return (root, query, cb) -> cb.exists(activeIndicator(root, query, cb, new IndicatorCriteria(severity, List.of(), null)));
    }

    /** Probable entry errors in the source: "informativno" indicators of rule D (see Indicator.isPossibleEntryError). */
    public static Specification<Contract> withPossibleEntryError() {
        return (root, query, cb) -> cb.exists(activeIndicator(root, query, cb,
                new IndicatorCriteria(SeverityNote.INFORMATIVE, List.of("D_VALUE_INCREASE", "D_OVERPAYMENT"), null)));
    }

    public static Specification<Contract> withIndicatorOfRule(String ruleCode) {
        return (root, query, cb) -> cb.exists(activeIndicator(root, query, cb, new IndicatorCriteria(null, List.of(ruleCode), null)));
    }

    /** Conditions on one indicator; all of them must hold for the same indicator. Empty parts are ignored. */
    private record IndicatorCriteria(SeverityNote severity, List<String> ruleCodes, MagnitudeBand band) {
    }

    private static Predicate notWithNaturalPerson(Root<Contract> root, CriteriaBuilder cb) {
        return notSupplierType(root, cb, OrganizationType.NATURAL_PERSON);
    }

    private static Predicate notSupplierType(Root<Contract> root, CriteriaBuilder cb, OrganizationType type) {
        // A left join, because a contract without a named supplier must stay visible.
        Join<Contract, Organization> supplier = root.join("supplier", JoinType.LEFT);
        return cb.or(cb.isNull(supplier.get("id")), cb.notEqual(supplier.get("orgType"), type));
    }

    private static Subquery<Long> activeIndicator(Root<Contract> root, CriteriaQuery<?> query, CriteriaBuilder cb,
                                                  IndicatorCriteria criteria) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Indicator> indicator = subquery.from(Indicator.class);
        List<Predicate> predicates = new ArrayList<>(List.of(
                cb.equal(indicator.join("contractIds"), root.get("id")),
                cb.equal(indicator.get("status"), IndicatorStatus.ACTIVE)));
        if (criteria.severity() != null) {
            predicates.add(cb.equal(indicator.get("severityNote"), criteria.severity()));
        }
        if (!criteria.ruleCodes().isEmpty()) {
            predicates.add(indicator.get("ruleCode").in(criteria.ruleCodes()));
        }
        if (criteria.band() != null) {
            predicates.add(cb.equal(indicator.get("magnitudeBand"), criteria.band()));
        }
        return subquery.select(indicator.get("id")).where(predicates.toArray(Predicate[]::new));
    }
}
