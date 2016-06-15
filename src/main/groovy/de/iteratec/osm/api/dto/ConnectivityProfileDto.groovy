package de.iteratec.osm.api.dto

import de.iteratec.osm.measurement.schedule.ConnectivityProfile

/**
 * Created by marko on 15.06.16.
 */
class ConnectivityProfileDto {
    String name
    Boolean active
    Integer bandwidthDown
    Integer bandwidthUp
    Integer latency
    Integer packetLoss

    public static ConnectivityProfileDto create(ConnectivityProfile connectivityProfile){
        ConnectivityProfileDto connectivityProfileDto = new ConnectivityProfileDto()
        connectivityProfileDto.name = connectivityProfile.name
        connectivityProfileDto.active = connectivityProfile.active
        connectivityProfileDto.bandwidthDown = connectivityProfile.bandwidthDown
        connectivityProfileDto.bandwidthUp = connectivityProfile.bandwidthUp
        connectivityProfileDto.latency = connectivityProfile.latency
        connectivityProfileDto.packetLoss = connectivityProfile.packetLoss
        return connectivityProfileDto
    }

    public static List<ConnectivityProfileDto> create (List<ConnectivityProfile> connectivityProfileList){
        List<ConnectivityProfileDto> connectivityProfileDtoList = []
        connectivityProfileList.each {
            connectivityProfileDtoList.add(create(it))
        }
        return connectivityProfileDtoList
    }
}
