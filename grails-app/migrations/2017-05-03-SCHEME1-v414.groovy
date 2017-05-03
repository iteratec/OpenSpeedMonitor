databaseChangeLog = {

    changeSet(author: "sburnicki (generated)", id: "1493813874383-1") {
        addColumn(tableName: "userspecific_dashboard_base") {
            column(name: "from", type: "datetime")
        }
    }

    changeSet(author: "sburnicki (generated)", id: "1493813874383-2") {
        addColumn(tableName: "userspecific_dashboard_base") {
            column(name: "to", type: "datetime")
        }
    }
}
