databaseChangeLog = {
    changeSet(author: "nkuhn", id: "1447858496000-1") {
        sql(''' update measured_value set value = value * 100 where value <= 1; ''' )
    }
    changeSet(author: "nkuhn", id: "1447858496000-2") {
        sql(''' update event_result set customer_satisfaction_in_percent = customer_satisfaction_in_percent * 100 where customer_satisfaction_in_percent <= 1; ''' )
    }
}
