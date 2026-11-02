package com.datathon.api.contract;

import com.datathon.api.organization.Organization;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** A contract from a register. Amounts are EUR; null means "not published", never 0. */
@Entity
@Table(name = "contract")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String naturalKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "buyer_id")
    private Organization buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Organization supplier;

    private String eojnContractUrl;
    private String eojnTenderUrl;
    private String frameworkAgreementUrl;
    private String evidNumber;
    private String contractNumber;
    private String subject;
    private SubjectType subjectType;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "char(8)")
    private String cpv;

    private String cpvLabel;
    private String procedureType;
    private boolean isFrameworkBased;
    private LocalDate dateSigned;
    private String durationText;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "char(3)")
    private String currencyOriginal;

    @Column(name = "initial_value_eur")
    private BigDecimal initialValue;

    @Column(name = "initial_value_vat_eur")
    private BigDecimal initialValueGross;

    @Column(name = "amendments_value_eur")
    private BigDecimal amendmentsValue;

    @Column(name = "total_value_eur")
    private BigDecimal totalValue;

    @Column(name = "total_value_vat_eur")
    private BigDecimal totalValueGross;

    @Column(name = "paid_eur")
    private BigDecimal paidAmount;

    private LocalDate completedOn;
    private String paymentNote;
    private String note;
    private String status;
    private Boolean euFunded;

    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> qualityFlags = new ArrayList<>();

    private boolean isSynthetic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provenance_id")
    private Provenance provenance;

    private Instant validFrom;

    public Contract(String naturalKey) {
        this.naturalKey = naturalKey;
    }
}
