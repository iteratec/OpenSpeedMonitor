package de.iteratec.osm.api.dto

class ApplicationCsiDto {
    CsiDto[] csiValues
    boolean hasCsiConfiguration
    boolean hasJobResults
    boolean hasInvalidJobResults

    static ApplicationCsiDto createWithoutConfiguration() {
        ApplicationCsiDto dto = new ApplicationCsiDto()
        dto.hasCsiConfiguration = false
        dto.csiValues = []
        return dto
    }
}
