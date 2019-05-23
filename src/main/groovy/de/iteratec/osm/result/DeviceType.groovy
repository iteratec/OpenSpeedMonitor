package de.iteratec.osm.result

enum DeviceType {
    DESKTOP("Desktop", "desktop"),
    TABLET("Tablet", "tablet"),
    SMARTPHONE("Smartphone", "mobile"),
    UNDEFINED("Undefined", "question")

    private String label
    private String icon

    private DeviceType(String value, String icon) {
        this.label = value
        this.icon = icon
    }

    String getDeviceTypeLabel(){
        return label
    }

    String getDeviceTypeIcon() {
        return icon
    }

    /**
     * Parses the location label to determine the possible device type
     * @param label location label for wptServer
     */
    static DeviceType guessDeviceType(String label) {
        switch (label) {
            case ~/(?i).*(-Win|IE\\s*[1-9]*|firefox|nuc).*/ :
                return DeviceType.DESKTOP
            case ~/(?i).*(Pad|Tab|Note|Xoom|Book|Tablet).*/ :
                return DeviceType.TABLET
            case ~/(?i)(?!(.*(Pad|Tab|Note|Xoom|Book|Tablet).*)).*(Samsung|Moto|Sony|Nexus|Huawei|Nokia|Alcatel|LG|OnePlus|HTC|Phone).*/ :
                return DeviceType.SMARTPHONE
            default: return DeviceType.UNDEFINED
        }
    }
}
