package com.datathon.api.organization.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.ingest.normalize.OrganizationClassifier;
import com.datathon.api.organization.Organization;
import com.datathon.api.organization.OrganizationType;
import org.junit.jupiter.api.Test;

/** GDPR display (decision 2026-11-05): names from the 2026-11-05 data the classifier did not mark as crafts. */
class OrganizationRefTest {

    private static Organization organization(String name) {
        return new Organization("00000000099", name, name.toLowerCase(), OrganizationClassifier.classifySupplier(name));
    }

    @Test
    void unclassifiedNamesThatMayBePeopleAreHidden() {
        for (String name : new String[] {"TERMODOR, DRAGUTIN KRIZMANIĆ", "Dr. Neven Martinović",
                "Ante Vojvodić AT CORNER", "LJEKARNA LJUBICA PLEŠKO-FRUŽA"}) {
            OrganizationRef ref = OrganizationRef.from(organization(name));
            assertThat(ref.nameHidden()).as(name).isTrue();
            assertThat(ref.name()).as(name).doesNotContain("KRIZMANIĆ", "Martinović", "Vojvodić", "PLEŠKO");
        }
    }

    @Test
    void ownerMarkersAfterACommaAreCrafts() {
        assertThat(OrganizationClassifier.classifySupplier("SIGNAL-BROD,vl. Robert Vrljić")).isEqualTo(OrganizationType.CRAFT);
        assertThat(OrganizationClassifier.classifySupplier("DIKTATORI , član Dino Kursan")).isEqualTo(OrganizationType.CRAFT);
        assertThat(OrganizationClassifier.classifySupplier("Ured ovl. Arh. Dinko Grgurević")).isEqualTo(OrganizationType.CRAFT);
        assertThat(OrganizationRef.from(organization("SIGNAL-BROD,vl. Robert Vrljić")).name())
                .isEqualTo(OrganizationRef.HIDDEN_CRAFT_NAME);
    }

    @Test
    void institutionsAndCompaniesKeepTheirNames() {
        for (String name : new String[] {"Zavod za javno zdravstvo Istarske županije", "Gradski muzej Korčula",
                "Poljoprivredna zadruga Oleander", "Primjer d.o.o."}) {
            OrganizationRef ref = OrganizationRef.from(organization(name));
            assertThat(ref.nameHidden()).as(name).isFalse();
            assertThat(ref.name()).isEqualTo(name);
        }
    }
}
