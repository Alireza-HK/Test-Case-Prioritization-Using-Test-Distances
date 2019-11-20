package oulu.m3s.utils

import org.apache.commons.math3.stat.StatUtils

class NumberUtil {

    private static final double EPSILON = 1e-12;

    public static double scaleToNewRange(double valueCoordinate1,
                                         double startCoordinate, double endCoordinate,
                                         double newStartCoordinate, double newEndCoordinate) {

        if (Math.abs(endCoordinate - startCoordinate) < EPSILON) {
            throw new ArithmeticException("/ 0");
        }
        double offset = newStartCoordinate;
        double ratio = (newEndCoordinate - newStartCoordinate) / (endCoordinate - startCoordinate);
        return ratio * (valueCoordinate1 - startCoordinate) + offset;
    }

    public static String getSummaryStatistics(double[] input){
        double min = (input as List<Double>).min()
        double max = (input as List<Double>).max()
        double mean = StatUtils.mean(input).trunc(2)
        double lowerQuartile = StatUtils.percentile(input, 25)
        double median = StatUtils.percentile(input, 50)
        double upperQuartile = StatUtils.percentile(input, 75)
        String summary = "Mean: ${mean} [${min}-${max}] - P25: ${lowerQuartile} - P50: ${median} - P75: ${upperQuartile}"
        return summary
    }

}
