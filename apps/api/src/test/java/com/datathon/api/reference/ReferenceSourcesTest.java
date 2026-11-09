package com.datathon.api.reference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.datathon.api.integration.jpt.JptClient;
import com.datathon.api.integration.jpt.JptEntry;
import com.datathon.api.reference.DzsCensusReader.CensusRow;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class ReferenceSourcesTest {

    @Test
    void mapsJptBodiesWithFounders() throws IOException {
        List<JptEntry> entries = new JptClient(null, JsonMapper.builder().build()).parse(fixture("jpt-izvadak.json"));

        JptEntry osijek = entries.stream().filter(entry -> entry.oib().equals("30050049642")).findFirst().orElseThrow();
        assertThat(osijek.name()).isEqualTo("GRAD OSIJEK");
        assertThat(osijek.jptCode()).isEqualTo("2158-1");
        assertThat(osijek.jptType()).isEqualTo(MunicipalityImporter.JPT_CITY);
        assertThat(osijek.county()).isEqualTo("OSJEČKO-BARANJSKA ŽUPANIJA");
        assertThat(osijek.foundedOn()).isEqualTo(LocalDate.of(1992, 12, 30));
        assertThat(osijek.rawJson()).contains("\"Oznaka\":\"2158-1\"");

        JptEntry zone = entries.stream().filter(entry -> entry.oib().equals("88060877962")).findFirst().orElseThrow();
        assertThat(zone.founders()).containsExactly(new JptEntry.Founder("94115544733", "OPĆINA KRIŽ"));
    }

    @Test
    void readsCensusTableOne() throws IOException {
        List<CensusRow> rows = DzsCensusReader.read(fixture("dzs-popis2021-izvadak.xlsx"));

        assertThat(rows).contains(
                new CensusRow(11, "Zagrebačka", "Grad", "Dugo Selo", 17676),
                new CensusRow(14, "Zagrebačka", "Općina", "Bistra", 6444));
        CensusRow zagreb = rows.stream().filter(row -> row.county().equals("Grad Zagreb") && row.unitKind() == null)
                .findFirst().orElseThrow();
        assertThat(zagreb.population()).isEqualTo(767131);
        assertThat(MunicipalityImporter.kindOf(zagreb)).contains("grad_zagreb");
        assertThat(rows).filteredOn(row -> "Gradska četvrt".equals(row.unitKind()))
                .allMatch(row -> MunicipalityImporter.kindOf(row).isEmpty());
        assertThat(rows).filteredOn(row -> row.county().equals("Republika Hrvatska"))
                .allMatch(row -> MunicipalityImporter.kindOf(row).isEmpty());
    }

    @Test
    void matchesDzsNamesToJptNames() {
        assertThat(MunicipalityImporter.key("Zagrebačka", "grad", "Ivanić-Grad"))
                .isEqualTo(MunicipalityImporter.key("ZAGREBAČKA ŽUPANIJA", "grad", "IVANIĆ-GRAD"));
        assertThat(MunicipalityImporter.key("Primorsko-goranska", "opcina", "Brod Moravice"))
                .isEqualTo(MunicipalityImporter.key("PRIMORSKO-GORANSKA ŽUPANIJA", "opcina", "BROD MORAVICE"));
    }

    @Test
    void assignsPeerGroups() {
        assertThat(PeerGroups.of("opcina", 1999)).isEqualTo("P1");
        assertThat(PeerGroups.of("opcina", 2000)).isEqualTo("P2");
        assertThat(PeerGroups.of("opcina", 5000)).isEqualTo("P3");
        assertThat(PeerGroups.of("grad", 9999)).isEqualTo("P4");
        assertThat(PeerGroups.of("grad", 10000)).isEqualTo("P5");
        assertThat(PeerGroups.of("grad", 50000)).isEqualTo("P6");
        assertThat(PeerGroups.of("grad_zagreb", 767131)).isEqualTo("P7");
        assertThatThrownBy(() -> PeerGroups.of("zupanija", 1)).isInstanceOf(IllegalArgumentException.class);
    }

    private static byte[] fixture(String name) throws IOException {
        try (InputStream input = ReferenceSourcesTest.class.getResourceAsStream("/fixtures/reference/" + name)) {
            return input.readAllBytes();
        }
    }
}
