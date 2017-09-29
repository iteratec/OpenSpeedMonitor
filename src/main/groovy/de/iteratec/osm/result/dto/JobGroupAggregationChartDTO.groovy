package de.iteratec.osm.result.dto

class JobGroupAggregationChartDTO {
    String measurand = ""
    String unit = ""
    String measurandGroup = ""
    List<JobGroupDTO> groupData = []
    Map<String, String> i18nMap = [:]
}
