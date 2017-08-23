databaseChangeLog = {
    changeSet(author: "mwg (generated)", id: "1499868526467-1") {
        addColumn(tableName: "event_result") {
            column(name: "consistently_interactive_in_millisecs", type: "integer")
            column(name: "first_interactive_in_millisecs", type: "integer")
            column(name: "visually_complete85in_millisecs", type: "integer")
            column(name: "visually_complete90in_millisecs", type: "integer")
            column(name: "visually_complete95in_millisecs", type: "integer")
            column(name: "visually_complete99in_millisecs", type: "integer")
        }
    }
}
