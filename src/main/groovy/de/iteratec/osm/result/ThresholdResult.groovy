package de.iteratec.osm.result

enum ThresholdResult {

    /**
     * Enum for a value above the upper boundary.
     */
    BAD("bad"),

    /**
     * Enum for a value between the lower and the upper boundary.
     */
    OK("ok"),

    /**
     * Enum for a value under the lower boundary.
     */
    GOOD("good")

    /**
     * Result name
     */
    private String result

    /**
     * Constructor
     *
     * @param label The label of the threshold result.
     */
    private ThresholdResult(String label){
        result = label
    }

    /**
     * Gets the name of the threshold result.
     *
     * @return The name of the treshold result
     */
    String getResult(){
        return result
    }
}
