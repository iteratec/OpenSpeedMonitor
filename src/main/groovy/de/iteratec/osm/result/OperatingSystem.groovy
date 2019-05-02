package de.iteratec.osm.result

enum OperatingSystem {
    WINDOWS("Windows"),
    ANDROID("Android"),
    IOS("iOS"),
    UNKOWN("Unknown")

    private String label

    private OperatingSystem(String value) {
        this.label = value
    }

    String getOSLabel(){
        return label
    }
}
