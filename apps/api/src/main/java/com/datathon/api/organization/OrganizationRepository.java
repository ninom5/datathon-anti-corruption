package com.datathon.api.organization;

import com.datathon.api.organization.dto.BuyerOption;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByOib(String oib);

    Optional<Organization> findFirstByOibIsNullAndNameNormAndIsSynthetic(String nameNorm, boolean isSynthetic);

    List<Organization> findByNameNormAndOibIsNotNullAndIsSyntheticFalse(String nameNorm);

    @Query("""
            select new com.datathon.api.organization.dto.BuyerOption(b.id, b.oib, b.name, b.orgType, count(c), b.isSynthetic)
            from Contract c join c.buyer b
            where :synthetic = true or c.isSynthetic = false
            group by b.id, b.oib, b.name, b.orgType, b.isSynthetic
            order by b.name""")
    List<BuyerOption> findBuyersWithContractCount(@Param("synthetic") boolean synthetic);

    @Query(value = """
            select o.oib || '|' || (f ->> 'oib') from organization o, jsonb_array_elements(o.founders) f
            where o.oib is not null and o.founders is not null and f ->> 'oib' is not null""", nativeQuery = true)
    List<String> findSupplierFounderPairs();

    @Modifying
    @Transactional
    @Query(value = "update organization set founders = cast(:founders as jsonb) where oib = :oib and is_synthetic",
            nativeQuery = true)
    int setSyntheticFounders(@Param("oib") String oib, @Param("founders") String founders);
}
