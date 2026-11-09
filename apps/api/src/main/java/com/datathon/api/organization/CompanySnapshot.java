package com.datathon.api.organization;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "company_snapshot")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanySnapshot {

    public static final String SOURCE_SUDREG = "sudreg";
    public static final String SOURCE_FIXTURE = "fixture";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    private String source;
    private Instant retrievedAt;
    private boolean notFound;
    private String mbs;
    private LocalDate foundedOn;
    private LocalDate dissolvedOn;
    private String legalForm;
    private String mainNkd;
    private String mainNkdName;
    private String nkdVersion;
    private Boolean inBankruptcy;
    private Boolean inLiquidation;
    private String statusText;

    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    public CompanySnapshot(Organization organization, String source, Instant retrievedAt) {
        this.organization = organization;
        this.source = source;
        this.retrievedAt = retrievedAt;
    }
}
