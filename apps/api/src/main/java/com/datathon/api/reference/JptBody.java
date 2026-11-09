package com.datathon.api.reference;

import com.datathon.api.source.Provenance;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "jpt_body")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JptBody {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "char(11)")
    private String oib;

    private String jptCode;
    private String name;
    private String nameNorm;
    private String jptType;
    private String county;
    private String municipalityName;
    private LocalDate foundedOn;

    @JdbcTypeCode(SqlTypes.JSON)
    private String founders;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provenance_id")
    private Provenance provenance;

    public JptBody(String oib) {
        this.oib = oib;
    }
}
