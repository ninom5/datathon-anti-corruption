package com.datathon.api.ingest.eojn;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SupplierCellTest {

    @Test
    void splitsNameAndOib() {
        assertThat(SupplierCell.split("Crodux plin d.o.o. 50388109754"))
                .isEqualTo(new SupplierCell("Crodux plin d.o.o.", "50388109754", 1));
        assertThat(SupplierCell.split("HRVATSKI TELEKOM d.d.\nOIB: 81793146560"))
                .isEqualTo(new SupplierCell("HRVATSKI TELEKOM d.d.", "81793146560", 1));
        assertThat(SupplierCell.split("CESTE ZADARSKE ŽUPANIJE D.O. OIB: 63782992615").name())
                .isEqualTo("CESTE ZADARSKE ŽUPANIJE D.O.");
    }

    @Test
    void repeatedOibIsOneSupplier() {
        assertThat(SupplierCell.split("Pismorad d.o.o., 33260306505 33260306505"))
                .isEqualTo(new SupplierCell("Pismorad d.o.o.", "33260306505", 1));
    }

    @Test
    void consortiumKeepsLeaderOib() {
        SupplierCell cell = SupplierCell.split(
                "Zajednica gospodarskih subjekata: SPAN d.d., OIB: 19680551758 i COMBIS d.o.o., OIB: 91678676896");
        assertThat(cell.oibText()).isEqualTo("19680551758");
        assertThat(cell.isConsortium()).isTrue();
    }

    @Test
    void mistypedOrForeignNumbersAreNotOibs() {
        assertThat(SupplierCell.split("Obrt za rač. usluge, IT Smart Solutions 253918658591").oibText())
                .as("12 digits: kept for the malformed-OIB flag").isEqualTo("253918658591");
        assertThat(SupplierCell.split("RIKO d.o.o. 48222917").oibText()).isEqualTo("48222917");
        SupplierCell foreign = SupplierCell.split("OPERA GROUP d.o.o. SI82719241");
        assertThat(foreign.oibText()).isNull();
        assertThat(foreign.name()).isEqualTo("OPERA GROUP d.o.o. SI82719241");
        assertThat(SupplierCell.split("PLAN 10 d.o.o.").name()).as("short numbers stay in the name").isEqualTo("PLAN 10 d.o.o.");
        assertThat(SupplierCell.split("  ")).isEqualTo(new SupplierCell(null, null, 0));
    }
}
