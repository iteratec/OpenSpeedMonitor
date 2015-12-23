databaseChangeLog = {
    changeSet(author: "mmeier", id: "1450786890000-1") {
        sql(''' delete from measured_value_update_event where measured_value_id in (
                    select id from measured_value where closed_and_calculated = 1); ''' )
    }
    changeSet(author: "mmeier", id: "1450786890000-2") {
        sql(''' delete from measured_value_update_event where measured_value_id not in (
                    select id from measured_value); ''' )
    }
}