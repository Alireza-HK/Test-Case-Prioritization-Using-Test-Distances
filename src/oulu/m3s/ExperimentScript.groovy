package oulu.m3s

import groovy.time.TimeCategory
import groovyx.gpars.GParsPool
import org.apache.commons.math3.stat.StatUtils
import org.codehaus.groovy.reflection.ReflectionUtils
import oulu.m3s.business.algorithm.PairwiseAlgorithm
import oulu.m3s.business.algorithm.RandomPermutation
import oulu.m3s.business.similarity.SimilarityMetric
import oulu.m3s.business.similarity.impl.JaccardDistance
import oulu.m3s.business.similarity.impl.NormalisedCompressionDistance
import oulu.m3s.datamodel.GithubRevisionModel
import oulu.m3s.utils.FileUtil
import oulu.m3s.metric.AvgPercentageFaultsDetected
import oulu.m3s.utils.NumberUtil

import java.util.concurrent.ConcurrentHashMap


def getResourcePath = {def resource-> ReflectionUtils.getCallingClass(0).getResource(resource).toURI().path}

//A sample data included in the project. This data file includes information about 27 faulty revisions extracted from Joda Time project. The CSV data file has the following structure: [project, bugID, revisionID, revisionDate, modifiedSources, executedTests, failedTests]
String inputDatabase = getResourcePath("/Joda Time_2018-12-07 175637.csv")

//Read CSV data file and instantiate Defect4JRevisionModel objects
List<GithubRevisionModel> revisions = new ArrayList<>()
new File(inputDatabase).readLines().each { line ->
    revisions.add(new GithubRevisionModel(line))
}
//This directory includes list of test files for the 27 analyzed revisions extracted from Joda Time project
String dataPath = getResourcePath("/joda_revisions")

//Create an output log for the experiment
def experimentOutputLog = new File("ExperimentLog_${System.currentTimeMillis()}.txt")

//Collections for gathering all APFD (i.e., average percentage of faults detected) for different Test Case Prioritization techniques
List<Double> allMeasuresOrderedRandomly = new ArrayList<>()
List<Double> allMeasuresOrderedByPairwiseUsingNCD = new ArrayList<>()
List<Double> allMeasuresOrderedByPairwiseUsingJaccard = new ArrayList<>()

//Iterate over each revision for Joda Time. There are in overall 27 revisions available in CSV file
revisions.each { revisionModel ->
    experimentOutputLog  << "${revisionModel} \n"

    //Step 1: read input directory and build a map containing key [test file name] and value [source code]
    String revisionDirectory = "${dataPath}\\rev_${revisionModel.bugID}"
    experimentOutputLog  << "revisionDirectory: ${revisionDirectory} \n"
    HashMap<String, String> directoryMap = FileUtil.loadSourceFilesFromDirectory(revisionDirectory)

    //Step 2: calculate all unique comparisons (pairs) between test classes
    HashSet<String> allUniquePairs = new HashSet<>()
    directoryMap.keySet().each { object ->
        directoryMap.keySet().each { anotherObject ->
            String pairID = ([object, anotherObject] as ArrayList<String>).sort().join("_&&_")
            allUniquePairs.add(pairID)
        }
    }
    // ((input_size * input_size) - all duplicates) + (self comparison - duplicated self comparison)
    int expectedUniquePairs = ((directoryMap.size() * directoryMap.size()) / 2) + directoryMap.size() / 2
    assert allUniquePairs.size() == expectedUniquePairs


    //Step 3: calculate all distances and make a map containing a key [pair ID of two objects] and a value [distance value]
    /* To improve performance, one can 1) parallelize this step and/or 2) implement a simple caching system where distances among the tests are calculated upon request and retained.
    The distance value should be updated only if one of the relevant tests is observed in the revision change list.
    */
    Map<String, Double> distanceMapNCD = new ConcurrentHashMap<>()
    Map<String, Double> distanceMapJaccard = new ConcurrentHashMap<>()
    Date startTime, endTime
    experimentOutputLog  << "UniqueTestFiles: ${directoryMap.keySet().size()} -> Calculating Distances for allUniquePairs: ${allUniquePairs.size()} \n"
    SimilarityMetric similarityMetric = null

    //This block is similar with the next block of code. However, for the purpose of this task, I intentionally did this to measure the time for each similarity metric. In real-world application, one should aim to minimize the clone (duplicate code) in the source-code as much as possible.
    startTime = new Date()
    GParsPool.withPool() {
        allUniquePairs.eachParallel { pairID ->
            def allTokens = pairID.split("_&&_")
            String x = directoryMap.get(allTokens[0])
            String y = directoryMap.get(allTokens[1])
            //initialize similarityMetric using NCD
            similarityMetric = new NormalisedCompressionDistance();
            distanceMapNCD.put(pairID, similarityMetric.distance(x, y))
        }
    }
    endTime = new Date();
    experimentOutputLog  << "NormalisedCompressionDistance Took ${TimeCategory.minus(endTime, startTime)} \n"
    //
    startTime = new Date()
    GParsPool.withPool() {
        allUniquePairs.eachParallel { pairID ->
            def allTokens = pairID.split("_&&_")
            String x = directoryMap.get(allTokens[0])
            String y = directoryMap.get(allTokens[1])
            //initialize similarityMetric using Jaccard
            similarityMetric = new JaccardDistance();
            distanceMapJaccard.put(pairID, similarityMetric.distance(x, y))
        }
    }
    endTime = new Date();
    experimentOutputLog  << "JaccardDistance Took ${TimeCategory.minus(endTime, startTime)} \n"


    //Step 4: Test Case Prioritization algorithms and measurements
    Set<String> revisionTestSuite = revisionModel.listExecutedTests()
    Set<String> revisionFailedTests = revisionModel.listFailedTests()

    List<String> orderedRandomly = RandomPermutation.randomize(revisionTestSuite)
    List<String> orderedByPairwiseUsingNCD = PairwiseAlgorithm.maximizeDiversity(new ArrayList<String>(), revisionTestSuite.asList(), distanceMapNCD)
    List<String> orderedByPairwiseUsingJaccard = PairwiseAlgorithm.maximizeDiversity(new ArrayList<String>(), revisionTestSuite.asList(), distanceMapJaccard)

    assert revisionTestSuite.size() == orderedRandomly.size()
    assert revisionTestSuite.size() == orderedByPairwiseUsingNCD.size()
    assert revisionTestSuite.size() == orderedByPairwiseUsingJaccard.size()

    HashMap<String, Integer> resultOrderedRandomly = getFaultRevealingTestMap(orderedRandomly, revisionFailedTests)
    HashMap<String, Integer> resultOrderedByPairwiseUsingNCD = getFaultRevealingTestMap(orderedByPairwiseUsingNCD, revisionFailedTests)
    HashMap<String, Integer> resultOrderedByPairwiseUsingJaccard = getFaultRevealingTestMap(orderedByPairwiseUsingJaccard, revisionFailedTests)
    //
    allMeasuresOrderedRandomly.add(AvgPercentageFaultsDetected.calculate(revisionTestSuite.size(), revisionFailedTests.size(), resultOrderedRandomly.values()))
    allMeasuresOrderedByPairwiseUsingNCD.add(AvgPercentageFaultsDetected.calculate(revisionTestSuite.size(), revisionFailedTests.size(), resultOrderedByPairwiseUsingNCD.values()))
    allMeasuresOrderedByPairwiseUsingJaccard.add(AvgPercentageFaultsDetected.calculate(revisionTestSuite.size(), revisionFailedTests.size(), resultOrderedByPairwiseUsingJaccard.values()))

    experimentOutputLog  << "------------------------------------------------ \n"
}

//Summarize all aggregated APFDs for different Test Case Prioritization techniques
experimentOutputLog  << "orderedRandomly: ${NumberUtil.getSummaryStatistics(allMeasuresOrderedRandomly as double[])} \n"
experimentOutputLog  << "orderedByPairwiseUsingNCD: ${NumberUtil.getSummaryStatistics(allMeasuresOrderedByPairwiseUsingNCD as double[])} \n"
experimentOutputLog  << "orderedByPairwiseUsingJaccard: ${NumberUtil.getSummaryStatistics(allMeasuresOrderedByPairwiseUsingJaccard as double[])} \n"



HashMap<String, Integer> getFaultRevealingTestMap(List<String> orderedTestSuite, Set<String> failedTests) {
    int runningIndex = 1
    HashMap<String, Integer> faultyTestMap = new HashMap<>()
    orderedTestSuite.each { name ->
        if (failedTests.contains(name)) {
            faultyTestMap.put(name, runningIndex)
        }
        runningIndex++
    }
    return faultyTestMap
}
