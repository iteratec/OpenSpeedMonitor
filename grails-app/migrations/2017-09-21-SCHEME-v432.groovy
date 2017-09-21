databaseChangeLog = {
    changeSet(author: "nkuhn", id: "1506008225000-1") {
        modifyDataType(
            tableName: "location",
            columnName: "location",
            newDataType: "varchar(255)"
        )
        modifyDataType(
                tableName: "location",
                columnName: "label",
                newDataType: "varchar(255)"
        )
    }
}