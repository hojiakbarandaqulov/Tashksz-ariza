package org.example.toshkszariza.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationRegionTest {
    @Test
    void exposesExactlyThirteenKszAndSzRegions() {
        long selectableCount = Arrays.stream(ApplicationRegion.values())
                .filter(ApplicationRegion::isSelectable)
                .count();

        assertThat(selectableCount).isEqualTo(13);
        assertThat(ApplicationRegion.fromLabel("Chilonzor KSZ")).contains(ApplicationRegion.CHILONZOR_KSZ);
        assertThat(ApplicationRegion.fromLabel("Chulonzor 2 SoftPlast KSZ"))
                .contains(ApplicationRegion.CHILONZOR_2_SOFTPLAST_KSZ);
        assertThat(ApplicationRegion.fromLabel("Uchtepa yoshlar SZ"))
                .contains(ApplicationRegion.UCHTEPA_YOSHLAR_SZ);
        assertThat(ApplicationRegion.fromLabel("Yashnobod yoshlar SZ"))
                .contains(ApplicationRegion.YASHNOBOD_YOSHLAR_SZ);
        assertThat(ApplicationRegion.fromLabel("Bektemir yoshlar SZ"))
                .contains(ApplicationRegion.BEKTEMIR_YOSHLAR_SZ);
        assertThat(ApplicationRegion.fromLabel("Yangihayot yoshlar SZ"))
                .contains(ApplicationRegion.YANGIHAYOT_YOSHLAR_SZ);
        assertThat(ApplicationRegion.fromLabel("Chilonzor tumani")).isEmpty();
        assertThat(ApplicationRegion.fromLabel("Toshkent")).isEmpty();
    }
}
