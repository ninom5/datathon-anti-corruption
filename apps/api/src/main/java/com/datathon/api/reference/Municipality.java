package com.datathon.api.reference;

import com.datathon.api.source.Provenance;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

/** A JLS (OIB from JPT) with its DZS 2021 population and peer group. Provenance points at the DZS row. */
@Entity
@Table(name = "municipality")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Municipality {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "char(11)")
    private String oib;

    private String name;
    /** 'grad', 'opcina' or 'grad_zagreb', as in docs/DATA_MODEL.md §2. */
    private String kind;
    private String county;
    @Column(name = "population_2021")
    private Integer population2021;
    private String peerGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provenance_id")
    private Provenance provenance;

    public Municipality(String oib) {
        this.oib = oib;
    }
}
