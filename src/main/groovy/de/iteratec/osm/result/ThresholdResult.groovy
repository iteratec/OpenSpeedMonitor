package de.iteratec.osm.result

enum ThresholdResult {

    BAD("bad"),
    OK("ok"),
    GOOD("good")

    private String result

    private ThresholdResult(String label){
        result = label
    }

    String getResult(){
        return result
    }
}
