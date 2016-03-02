databaseChangeLog = {
    changeSet(author: "mmeier", id: "1450271003000-1") {
        sql(''' delete from default_time_to_cs_mapping where name = '1 - impatient'; ''' )
    }
    changeSet(author: "mmeier", id: "1450271003000-2") {
        sql(''' delete from default_time_to_cs_mapping where name = '2'; ''' )
    }
    changeSet(author: "mmeier", id: "1450271003000-3") {
        sql(''' delete from default_time_to_cs_mapping where name = '3'; ''' )
    }
    changeSet(author: "mmeier", id: "1450271003000-4") {
        sql(''' delete from default_time_to_cs_mapping where name = '4'; ''' )
    }
    changeSet(author: "mmeier", id: "1450271003000-5") {
        sql(''' delete from default_time_to_cs_mapping where name = '5 - patient'; ''' )
    }
}