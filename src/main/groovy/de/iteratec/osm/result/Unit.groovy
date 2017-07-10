package de.iteratec.osm.result

enum Unit{
    KILOBYTE("KB",1000),
    MEGABYTE("MB",1000000),
    MILLISECONDS("ms",1),
    SECONDS("s",1000),
    PERCENT("%",1),
    NUMBER("#",1),
    OTHER("",1)

    private String label
    private Double divisor


    private Unit(String label,Double divisor){
        this.label = label
        this.divisor = divisor
    }

    String getLabel(){
        return label
    }

    Double getDivisor(){
        return divisor
    }
}