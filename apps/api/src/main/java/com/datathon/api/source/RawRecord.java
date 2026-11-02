package com.datathon.api.source;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** The source row exactly as read (header -> cell text). Never deleted, also kept for rejected rows. */
@Entity
@Getter
@Table(name = "raw_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RawRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id")
    private SourceFile file;

    private String locator;

    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    public RawRecord(SourceFile file, String locator, String payload) {
        this.file = file;
        this.locator = locator;
        this.payload = payload;
    }
}
