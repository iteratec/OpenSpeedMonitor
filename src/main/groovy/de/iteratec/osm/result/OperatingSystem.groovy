package de.iteratec.osm.result

enum OperatingSystem {
    WINDOWS("Windows"),
    ANDROID("Android"),
    IOS("iOS"),
    UNKNOWN("Unknown")

    private String label

    private OperatingSystem(String value) {
        this.label = value
    }

    String getOSLabel(){
        return label
    }

    /**
     * Parses the location label to determine the possible operating system
     * @param label location label for wptServer
     */
    static OperatingSystem guessOperatingSystem(String label) {
        switch (label) {
            case ~/(?i).*(-Win|IE\\s*[1-9]*|firefox|nuc|desktop).*/ :
                return OperatingSystem.WINDOWS
            case ~/(?i)(?!(.*(Android|Desktop).*)).*(ios|iphone|ipad).*/ :
                return OperatingSystem.IOS
            case ~/(?i).*(Samsung|Moto|Sony|Nexus|Huawei|Nokie|LG|HTC|Alcatel|OnePlus).*/ :
                return OperatingSystem.ANDROID
            default: return OperatingSystem.UNKNOWN
        }
    }
}
