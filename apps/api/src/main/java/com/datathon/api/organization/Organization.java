package com.datathon.api.organization;

import com.datathon.api.source.Provenance;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Buyer or supplier. Same OIB means same organization (docs/DATA_MODEL.md §3). */
@Entity
@Table(name = "organization")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "char(11)")
    private String oib;

    private String name;
    private String nameNorm;
    private OrganizationType orgType;
    private String county;
    /** From the JPT register (#40); null for organizations that are not public bodies. */
    private String jptCode;
    private String jptType;
    private boolean isSynthetic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provenance_id")
    private Provenance provenance;

    public Organization(String oib, String name, String nameNorm, OrganizationType orgType) {
        this.oib = oib;
        this.name = name;
        this.nameNorm = nameNorm;
        this.orgType = orgType;
    }
}
