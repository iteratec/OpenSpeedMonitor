databaseChangeLog = {
    changeSet(author: "nkuhn", id: "1447854544000-1") {
        sql(''' update osm_configuration set csi_transformation = 'BY_MAPPING' ''' )
    }
}
