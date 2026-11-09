package com.datathon.api.contract;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ContractTotals {

    private final EntityManager entityManager;

    record Totals(long contracts, BigDecimal totalValue, long buyers) {
    }

    Totals of(Specification<Contract> specification) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Contract> root = query.from(Contract.class);
        Expression<BigDecimal> value = cb.coalesce(root.get("totalValue"), root.get("initialValue"));
        query.multiselect(cb.count(root), cb.sum(value), cb.countDistinct(root.get("buyer")));
        query.where(specification.toPredicate(root, query, cb));
        Tuple tuple = entityManager.createQuery(query).getSingleResult();
        BigDecimal sum = tuple.get(1, BigDecimal.class);
        return new Totals(tuple.get(0, Long.class), sum == null ? BigDecimal.ZERO : sum, tuple.get(2, Long.class));
    }
}
