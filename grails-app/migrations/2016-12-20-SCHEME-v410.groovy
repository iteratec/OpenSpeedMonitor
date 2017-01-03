databaseChangeLog = {

    changeSet(author: "marko (generated)", id: "1482245814394-1") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "script_id", tableName: "job")
    }
    changeSet(author: "marko (generated)", id: "1482245814394-2") {
        sql('''
            UPDATE job SET script_id = NULL WHERE job.deleted = 1;
        ''')
    }

    changeSet(author: "marko (generated)", id: "1483441134102-6") {
        dropColumn(columnName: "valid", tableName: "location")
    }

    changeSet(author: "mmi", id: "1482414034000-1") {
        grailsChange {
            change {
                // create tags from old jobSets
                sql.executeInsert('''INSERT INTO tags (version, name) SELECT 0,js.name FROM job_set js
                                    WHERE NOT EXISTS (SELECT name FROM tags t WHERE t.name=js.name);''')
            }
        }
    }

    changeSet(author: "mmi", id: "1482414034000-2") {
        grailsChange {
            change {
                // link tags with name of the jobset to jobs
                sql.executeInsert('''INSERT INTO tags_links (version, tag_id, tag_ref, type)
                    SELECT 
                        0, t.id, jsj.job_id, "job"
                    FROM
                        job_set_job jsj
                            INNER JOIN
                        job_set js ON js.id = job_set_jobs_id
                            INNER JOIN
                        tags t ON t.name = js.name;''')
            }
        }
    }

    changeSet(author: "mmi", id: "1482414034000-3") {
        dropTable(tableName: "job_set_job")
        dropTable(tableName: "job_set")
    }
}
