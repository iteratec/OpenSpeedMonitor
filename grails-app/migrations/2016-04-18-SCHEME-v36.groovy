databaseChangeLog = {

    changeSet(author: "msk", id: "1460989702778-1") {
        renameTable(oldTableName: "tag_links", newTableName: "tags_links")
    }
}
