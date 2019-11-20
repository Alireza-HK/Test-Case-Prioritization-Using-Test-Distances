package oulu.m3s.business.algorithm

class PairwiseAlgorithm {

    //Pairwise Algorithm proposed by Ledru, Yves, et al. "Prioritizing test cases with string distances." Automated Software Engineering 19.1 (2012): 65-95.
    public static List<String> maximizeDiversity(Collection<String> prioritizedTests, Collection<String> nonPrioritizedTests, Map<String, Double> distanceMap){
        ArrayList<String> orderedList = new ArrayList <>()
        //
        nonPrioritizedTests.size().times {
            List<String> initialCollection = null
            if(prioritizedTests.size() > 0){
                initialCollection = new ArrayList<>(prioritizedTests)
            }
            else {
                initialCollection = new ArrayList<>(nonPrioritizedTests)
            }
            //
            HashMap<String, List<Double>> allDistancesByTestName = new HashMap()
            initialCollection.each { prioritizedTest ->
                nonPrioritizedTests.each { nonPrioritizedTest ->
                    if(prioritizedTest.equalsIgnoreCase(nonPrioritizedTest)){ //Ignore exact same test comparison
                        return
                    }
                    String pairID = ([prioritizedTest, nonPrioritizedTest] as ArrayList<String>).sort().join("_&&_")
                    Double distance = distanceMap.get(pairID)
                    if(distance == null){
                        throw new Exception("Looking for: ${pairID} -> ${distance}")
                    }
                    //
                    List<Double> allDistances = new ArrayList<>()
                    if(allDistancesByTestName.containsKey(nonPrioritizedTest)){
                        allDistances = allDistancesByTestName.get(nonPrioritizedTest)
                    }
                    allDistances.add(distance)
                    allDistancesByTestName.put(nonPrioritizedTest, allDistances)
                }
            }
            //Pick the value based on DistanceMode i.e., MaxMin
            HashMap<String, Double> minDistanceByTestName = new HashMap<>()
            allDistancesByTestName.each { nonPrioritizedTest, allDistances ->
                //Ledru et al. used the min operation because an empirical study by Jiang et al. [18] showed that maximize-minimum is more efficient than maximize-average and maximize-maximum.
                Double selectedValue = allDistances.min()
                minDistanceByTestName.put(nonPrioritizedTest, selectedValue)
            }
            //Pick the max
            String selectedTestName = minDistanceByTestName.max {it.value}.key
            //Add to orderedList which will be returned as final prioritized test suite
            orderedList.add(selectedTestName)
            //Add to prioritizedTests which will be used in order to pick most distant item in the next iteration
            prioritizedTests.add(selectedTestName)
            //Remove from nonPrioritizedTests since this item has been processed already
            nonPrioritizedTests.remove(selectedTestName)
        }
        return orderedList
    }
}
