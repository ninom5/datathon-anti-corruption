package com.datathon.api.ingest.normalize;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.organization.OrganizationType;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class NormalizationTest {

    @Test
    void readsOibStatuses() {
        assertThat(Oibs.read("87311810356", false).status()).isEqualTo(Oibs.Status.VALID);
        assertThat(Oibs.read("87311810357", false).status()).isEqualTo(Oibs.Status.INVALID_CHECKSUM);
        assertThat(Oibs.read("87311810357", false).oib()).isEqualTo("87311810357");
        assertThat(Oibs.read("123456789", false)).isEqualTo(new Oibs.Result(null, Oibs.Status.MALFORMED));
        assertThat(Oibs.read("", false).status()).isEqualTo(Oibs.Status.MISSING);
        assertThat(Oibs.read("00000000012", true).status()).isEqualTo(Oibs.Status.SYNTHETIC_RESERVED);
        assertThat(Oibs.read("00000000012", false).status()).isEqualTo(Oibs.Status.INVALID_CHECKSUM);
    }

    @Test
    void readsMoneyAndConvertsHrkByDate() {
        assertThat(Money.read("198400", LocalDate.of(2025, 1, 1)).value()).isEqualByComparingTo("198400.00");
        assertThat(Money.read("1.500,50", LocalDate.of(2025, 1, 1)).value()).isEqualByComparingTo("1500.50");
        assertThat(Money.read("7534,50", LocalDate.of(2022, 12, 31)).value()).isEqualByComparingTo("1000.00");
        assertThat(Money.read("1.500,00 kn mjesečno", LocalDate.of(2022, 1, 1)).status()).isEqualTo(Money.Status.PERIODIC);
        assertThat(Money.read("nije poznato", LocalDate.of(2025, 1, 1)).status()).isEqualTo(Money.Status.UNREADABLE);
        assertThat(Money.read(" ", LocalDate.of(2025, 1, 1)).status()).isEqualTo(Money.Status.MISSING);
    }

    @Test
    void readsDatesInEveryRegisterFormat() {
        assertThat(Dates.read("46003")).contains(LocalDate.of(2025, 12, 12));
        assertThat(Dates.read("2025-12-12")).contains(LocalDate.of(2025, 12, 12));
        assertThat(Dates.read("12.12.2025.")).contains(LocalDate.of(2025, 12, 12));
        assertThat(Dates.read("32.13.2025.")).isEmpty();
        assertThat(Dates.read("12345")).isEmpty();
    }

    @Test
    void normalizesNames() {
        assertThat(NameNormalizer.normalize("HP - Hrvatska pošta d.d.")).isEqualTo("hp hrvatska posta dd");
        assertThat(NameNormalizer.normalize("Primjer d. o. o.")).isEqualTo("primjer doo");
        assertThat(NameNormalizer.normalize("Mali j.d.o.o.")).isEqualTo("mali jdoo");
        assertThat(NameNormalizer.normalize("Đurđevac Građenje")).isEqualTo("djurdjevac gradjenje");
    }

    @Test
    void classifiesSuppliersAndHidesLikelyPrivatePersons() {
        assertThat(OrganizationClassifier.classifySupplier("Primjer d.o.o.")).isEqualTo(OrganizationType.LLC);
        assertThat(OrganizationClassifier.classifySupplier("Primjer j.d.o.o.")).isEqualTo(OrganizationType.SIMPLE_LLC);
        assertThat(OrganizationClassifier.classifySupplier("Hrvatski Telekom d.d.")).isEqualTo(OrganizationType.JOINT_STOCK);
        assertThat(OrganizationClassifier.classifySupplier("Obrt Primjer, vl. Ivo Ivić")).isEqualTo(OrganizationType.CRAFT);
        assertThat(OrganizationClassifier.classifySupplier("Ivo Ivić")).isEqualTo(OrganizationType.NATURAL_PERSON);
        assertThat(OrganizationClassifier.classifySupplier("ZAGREBAČKI HOLDING")).isEqualTo(OrganizationType.OTHER);
        // Cases seen in the 2026-11-02 ingest (names here are invented, shapes are real).
        assertThat(OrganizationClassifier.classifySupplier("IVO IVIĆ")).isEqualTo(OrganizationType.NATURAL_PERSON);
        assertThat(OrganizationClassifier.classifySupplier("Ana  Anić-Horvat")).isEqualTo(OrganizationType.NATURAL_PERSON);
        assertThat(OrganizationClassifier.classifySupplier("OPG Ivo Ivić")).isEqualTo(OrganizationType.CRAFT);
        assertThat(OrganizationClassifier.classifySupplier("KLESAR, IVO IVIĆ, PRIMJEROVO, GLAVNA 1")).isEqualTo(OrganizationType.CRAFT);
        assertThat(OrganizationClassifier.classifySupplier("ODVJETNIČKI URED IVO IVIĆ")).isEqualTo(OrganizationType.CRAFT);
        assertThat(OrganizationClassifier.classifySupplier("Ured ovlaštenog arhitekta Ivo Ivić")).isEqualTo(OrganizationType.CRAFT);
        assertThat(OrganizationClassifier.classifySupplier("Primjer nafta d.d., Zagreb")).isEqualTo(OrganizationType.JOINT_STOCK);
        assertThat(OrganizationClassifier.classifySupplier("PRIMJER-GRADNJA d.o")).isEqualTo(OrganizationType.LLC);
        assertThat(OrganizationClassifier.classifySupplier("KSV Karlovac")).isEqualTo(OrganizationType.OTHER);
        assertThat(OrganizationClassifier.classifySupplier("Sveučilište u Primjerovu")).isEqualTo(OrganizationType.OTHER);
        assertThat(OrganizationClassifier.classifyBuyer("GRAD OSIJEK")).isEqualTo(OrganizationType.LOCAL_GOVERNMENT);
        assertThat(OrganizationClassifier.classifyBuyer("Osječko-baranjska županija")).isEqualTo(OrganizationType.COUNTY);
    }
}
