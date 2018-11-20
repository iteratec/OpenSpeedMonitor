databaseChangeLog = {

    changeSet(author: "fwieczorek", id: "20181115-hero-elements") {
        addColumn(tableName: "job") {
            column(name: "hero_element_times", type: "bit") {
                constraints(nullable: "false")
            }

            column(name: "hero_elements", type: "text") {
                constraints(nullable: "true")
            }
        }
    }
}