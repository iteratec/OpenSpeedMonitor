package de.iteratec.osm.dimple

class barchartDTO {
    List<Map<String, String>> data = [].withDefault {[:]}

    String yValueAccessor
    String yValueUnit
    List<String> xGroupings = []
    List<String> stackedAttributes = []
}
