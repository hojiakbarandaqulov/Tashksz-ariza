package org.example.toshkszariza.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationRegionTest {
    @Test
    void exposesExactlyNineKszRegions() {
        long selectableCount = Arrays.stream(ApplicationRegion.values())
                .filter(ApplicationRegion::isSelectable)
                .count();

        assertThat(selectableCount).isEqualTo(9);
        assertThat(ApplicationRegion.fromLabel("Chilonzor KSZ")).contains(ApplicationRegion.CHILONZOR_KSZ);
        assertThat(ApplicationRegion.fromLabel("Chulonzor 2 SoftPlast KSZ"))
                .contains(ApplicationRegion.CHILONZOR_2_SOFTPLAST_KSZ);
        assertThat(ApplicationRegion.fromLabel("Chilonzor tumani")).isEmpty();
        assertThat(ApplicationRegion.fromLabel("Toshkent")).isEmpty();
    }
}
