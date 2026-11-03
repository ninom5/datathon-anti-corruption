package com.datathon.api.organization;

import com.datathon.api.organization.dto.BuyerOption;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByOib(String oib);

    Optional<Organization> findFirstByOibIsNullAndNameNormAndIsSynthetic(String nameNorm, boolean isSynthetic);

    List<Organization> findByNameNormAndOibIsNotNullAndIsSyntheticFalse(String nameNorm);

    /** Buyers with their number of contracts; synthetic buyers only when asked for. */
    @Query("""
            select new com.datathon.api.organization.dto.BuyerOption(b.id, b.oib, b.name, b.orgType, count(c), b.isSynthetic)
            from Contract c join c.buyer b
            where :synthetic = true or c.isSynthetic = false
            group by b.id, b.oib, b.name, b.orgType, b.isSynthetic
            order by b.name""")
    List<BuyerOption> findBuyersWithContractCount(@Param("synthetic") boolean synthetic);
}
