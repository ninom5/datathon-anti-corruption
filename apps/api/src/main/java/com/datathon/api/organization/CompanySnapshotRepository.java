package com.datathon.api.organization;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanySnapshotRepository extends JpaRepository<CompanySnapshot, Long> {

    @Query("""
            select s from CompanySnapshot s
            where s.organization.id = :organizationId
            order by s.retrievedAt desc, s.id desc
            limit 1""")
    Optional<CompanySnapshot> findLatest(@Param("organizationId") Long organizationId);

    @Query(nativeQuery = true, value = """
            select distinct s.id, s.oib from organization s
            join contract c on c.supplier_id = s.id and not c.is_synthetic
            where s.oib ~ '^[0-9]{11}$' and s.org_type in ('doo', 'jdoo', 'dd', 'javno_tijelo')
              and not exists (select 1 from company_snapshot cs where cs.organization_id = s.id)
            order by s.id""")
    List<Object[]> findSuppliersWithoutSnapshot();
}
