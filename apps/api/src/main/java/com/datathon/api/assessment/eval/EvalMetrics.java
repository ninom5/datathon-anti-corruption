package com.datathon.api.assessment.eval;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record EvalMetrics(int cases, int truePositives, int falsePositives, int trueNegatives, int falseNegatives,
                          BigDecimal precision, BigDecimal recall, BigDecimal falsePositiveRate) {

    public record Pair(boolean expectedFlag, boolean actualFlag) {
    }

    public static EvalMetrics of(List<Pair> pairs) {
        int tp = 0;
        int fp = 0;
        int tn = 0;
        int fn = 0;
        for (Pair pair : pairs) {
            if (pair.expectedFlag() && pair.actualFlag()) {
                tp++;
            } else if (!pair.expectedFlag() && pair.actualFlag()) {
                fp++;
            } else if (!pair.expectedFlag()) {
                tn++;
            } else {
                fn++;
            }
        }
        return new EvalMetrics(pairs.size(), tp, fp, tn, fn, ratio(tp, tp + fp), ratio(tp, tp + fn), ratio(fp, fp + tn));
    }

    public static BigDecimal cohensKappa(List<Pair> pairs) {
        if (pairs.isEmpty()) {
            return null;
        }
        double n = pairs.size();
        double agree = pairs.stream().filter(pair -> pair.expectedFlag() == pair.actualFlag()).count() / n;
        double firstYes = pairs.stream().filter(Pair::expectedFlag).count() / n;
        double secondYes = pairs.stream().filter(Pair::actualFlag).count() / n;
        double chance = firstYes * secondYes + (1 - firstYes) * (1 - secondYes);
        if (chance >= 1.0) {
            return null;
        }
        return BigDecimal.valueOf((agree - chance) / (1 - chance)).setScale(3, RoundingMode.HALF_UP);
    }

    static BigDecimal ratio(int numerator, int denominator) {
        return denominator == 0 ? null
                : BigDecimal.valueOf(numerator).divide(BigDecimal.valueOf(denominator), 3, RoundingMode.HALF_UP);
    }
}
