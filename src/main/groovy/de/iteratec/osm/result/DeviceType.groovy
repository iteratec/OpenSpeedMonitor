package de.iteratec.osm.result

enum DeviceType {
    DESKTOP("Desktop"),
    TABLET("Tablet"),
    SMARTPHONE("Smartphone"),
    UNDEFINED("Undefined")

    private String label

    private DeviceType(String value) {
        this.label = value
    }

    String getDeviceTypeLabel(){
        return label
    }
}
