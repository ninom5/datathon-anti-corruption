package com.datathon.api.assessment.eval;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.assessment.eval.EvalMetrics.Pair;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EvalMetricsTest {

    private static List<Pair> pairs(int bothYes, int onlyFirst, int onlySecond, int bothNo) {
        List<Pair> pairs = new ArrayList<>();
        for (int i = 0; i < bothYes; i++) {
            pairs.add(new Pair(true, true));
        }
        for (int i = 0; i < onlyFirst; i++) {
            pairs.add(new Pair(true, false));
        }
        for (int i = 0; i < onlySecond; i++) {
            pairs.add(new Pair(false, true));
        }
        for (int i = 0; i < bothNo; i++) {
            pairs.add(new Pair(false, false));
        }
        return pairs;
    }

    @Test
    void precisionRecallAndFalsePositiveRate() {
        EvalMetrics metrics = EvalMetrics.of(pairs(8, 2, 1, 9));

        assertThat(metrics.precision()).isEqualByComparingTo("0.889");
        assertThat(metrics.recall()).isEqualByComparingTo("0.800");
        assertThat(metrics.falsePositiveRate()).isEqualByComparingTo("0.100");
    }

    @Test
    void unmeasuredRatiosAreNullNotZero() {
        EvalMetrics metrics = EvalMetrics.of(pairs(0, 0, 0, 5));

        assertThat(metrics.precision()).isNull();
        assertThat(metrics.recall()).isNull();
        assertThat(metrics.falsePositiveRate()).isEqualByComparingTo("0");
    }

    @Test
    void cohensKappaMatchesTextbookExample() {
        assertThat(EvalMetrics.cohensKappa(pairs(20, 5, 10, 15))).isEqualByComparingTo("0.400");
        assertThat(EvalMetrics.cohensKappa(pairs(0, 0, 0, 4))).isNull();
        assertThat(EvalMetrics.cohensKappa(List.of())).isNull();
    }
}
