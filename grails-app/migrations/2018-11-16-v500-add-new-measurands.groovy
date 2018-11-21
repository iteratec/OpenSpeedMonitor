databaseChangeLog = {
    changeSet(author: "fwieczorek", id: "1542369419628-23") {
        addColumn(tableName: "event_result") {
            column(name: "js_total_bytes", type: "INT")
            column(name: "image_total_bytes", type: "INT")
            column(name: "css_total_bytes", type: "INT")
            column(name: "html_total_bytes", type: "INT")
            column(name: "first_meaningful_paint", type: "INT")
            column(name: "first_contentful_paint", type: "INT")
        }
    }
}