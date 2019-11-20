package oulu.m3s.metric

import oulu.m3s.utils.NumberUtil


//Average Percentage of Faults Detected is a metric for measuring the effectiveness of test suite.
class AvgPercentageFaultsDetected {

    public static Double calculate(int totalTestNumber, int totalFaultNumber, Collection<Integer> faultyTestIndex){
        Double APFD = 1 - ( (faultyTestIndex.sum()) / (totalTestNumber*totalFaultNumber) ) + ( 1/(2*totalTestNumber))
        return NumberUtil.scaleToNewRange(APFD, 0, 1, 0, 100).trunc(2)
    }
}
