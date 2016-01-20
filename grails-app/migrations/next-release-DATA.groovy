databaseChangeLog = {
    changeSet(author: "bka", id: "1452546683118-2") {
        sql(''' update api_key set allowed_for_nightly_database_cleanup_activation = false ''')
    }

    // ### INITIAL CSI-CONFIGURATION ###

    // creating first page_weights
    changeSet(author: "mmi", id: "1453106072000-1") {
        preConditions(onFail: 'MARK_RAN'){
            and{
                sqlCheck(expectedResult: 0, 'select count(*) from page_weight')
                not{
                    sqlCheck(expectedResult: 0, 'select count(*) from page')
                }
            }

        }
        sql('''insert into page_weight(select tb1.*
                from (select id as id, 1 as version, id as page_id, weight as weight from page) as tb1)
        ''')
    }

    // creating a first csi-configuration
    changeSet(author: "mmi", id: "1453106072000-4") {
        preConditions(onFail: 'MARK_RAN'){
            and{
                sqlCheck(expectedResult:0, 'select count(*) from csi_configuration')
                not{
                    sqlCheck(expectedResult:0, 'select count(*) from csi_day')
                }
            }
        }
        sql('''insert into csi_configuration(
                select tb1.*
                from (select 1 as id, 1 as version, "a first csi configuration" as description, "initial csi configuration" as label, (select csi_day.id from csi_day) as csi_day_id) as tb1)
        ''')
    }

    // map browser_connectivity_weights to first csi_configuration
    changeSet(author: "mmi", id: "1453106072000-5") {
        preConditions(onFail: 'MARK_RAN') {
            and{
                sqlCheck(expectedResult: 0, 'select count(*) from csi_configuration_browser_connectivity_weight')
                not{
                    sqlCheck(expectedResult: 0, 'select count(*) from csi_configuration')
                }
                not{
                    sqlCheck(expectedResult: 0, 'select count(*) from browser_connectivity_weight')
                }
            }
        }
        sql('''set @rownum = -1;
                insert into csi_configuration_browser_connectivity_weight (csi_configuration_browser_connectivity_weights_id, browser_connectivity_weight_id, browser_connectivity_weights_idx)
                select csi_configuration.id, browser_connectivity_weight.id, @rownum := @rownum +1
                from csi_configuration, browser_connectivity_weight
        ''')
    }

    // map page_weights to first csi_configuration
    changeSet(author: "mmi", id: "1453106072000-6") {
        preConditions(onFail: 'MARK_RAN') {
            and{
                sqlCheck(expectedResult: 0, 'select count(*) from csi_configuration_page_weight')
                not{
                    sqlCheck(expectedResult: 0, 'select count(*) from csi_configuration')
                }
                not{
                    sqlCheck(expectedResult: 0, 'select count(*) from page_weight')
                }
            }
        }
        sql('''set @rownum = -1;
                insert into csi_configuration_page_weight (csi_configuration_page_weights_id, page_weight_id, page_weights_idx)
                select csi_configuration.id, page_weight.id, @rownum := @rownum +1
                from csi_configuration, page_weight
        ''')
    }

    // map time_to_cs_mappings to first csi_configuration
    changeSet(author: "mmi", id: "1453106072000-7") {
        preConditions(onFail: 'MARK_RAN') {
            and{
                sqlCheck(expectedResult: 0, 'select count(*) from csi_configuration_time_to_cs_mapping')
                not{
                    sqlCheck(expectedResult: 0, 'select count(*) from csi_configuration')
                }
                not{
                    sqlCheck(expectedResult: 0, 'select count(*) from time_to_cs_mapping')
                }
            }
        }
        sql('''set @rownum = -1;
                insert into csi_configuration_time_to_cs_mapping (csi_configuration_time_to_cs_mappings_id, time_to_cs_mapping_id, time_to_cs_mappings_idx)
                select csi_configuration.id, time_to_cs_mapping.id, @rownum := @rownum +1
                from csi_configuration, time_to_cs_mapping
        ''')
    }

    // apply first csi configuration to all jobGroups having jobGroupType 'CSI_AGGREGATION'
    changeSet(author: "mmi", id: "1453106072000-8") {
        sql('''
            update job_group
            set csi_configuration_id = (select id from csi_configuration)
            where group_type = 'CSI_AGGREGATION'
        ''')
    }
//     ### END INITIAL CSI-CONFIGURATION ###
}

