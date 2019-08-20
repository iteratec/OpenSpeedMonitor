databaseChangeLog = {
    changeSet(author: "nkuhn (generated)", id: "1565102741447-1") {
        renameColumn(
                tableName: "event_result",
                oldColumnName: "first_interactive_in_millisecs",
                newColumnName: "first_cpu_idle_in_millisecs",
                columnDataType: "integer")
    }
    changeSet(author: "nkuhn (generated)", id: "1565102741447-2") {
        renameColumn(
                tableName: "event_result",
                oldColumnName: "consistently_interactive_in_millisecs",
                newColumnName: "time_to_interactive_in_millisecs",
                columnDataType: "integer")
    }
    changeSet(author: "nkuhn", id: "1565102741447-3") {
        sql('''
            UPDATE performance_aspect
            SET metric_identifier = 'TIME_TO_INTERACTIVE' where metric_identifier = 'CONSISTENTLY_INTERACTIVE'; 
        ''')
    }
    changeSet(author: "nkuhn", id: "1565102741447-4") {
        sql('''
            UPDATE performance_aspect
            SET metric_identifier = 'FIRST_CPU_IDLE' where metric_identifier = 'FIRST_INTERACTIVE'; 
        ''')
    }
}