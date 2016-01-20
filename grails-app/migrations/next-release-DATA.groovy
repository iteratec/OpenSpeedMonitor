databaseChangeLog = {
    changeSet(author: "bka", id: "1452546683118-2") {
        sql(''' update api_key set allowed_for_nightly_database_cleanup_activation = false ''')
    }

    // ### INITIAL CSI-CONFIGURATION ###

    // creating first page_weights
    changeSet(author: "mmi", id: "1453106072000-1") {
        sql('''insert into page_weight(select tb1.*
                from (select id as id, 1 as version, id as page_id, weight as weight from page) as tb1
                where not exists (select id from page_weight))
        ''')
    }

    // creating a first csi-configuration
    changeSet(author: "mmi", id: "1453106072000-4") {
        sql('''insert into csi_configuration(
                select tb1.*
                from (select 1 as id, 1 as version, "a first csi configuration" as description, "initial csi configuration" as label, (select day.id from day) as day_id) as tb1
                where not exists (select * from csi_configuration))
        ''')
    }

    // map browser_connectivity_weights to first csi_configuration
    changeSet(author: "mmi", id: "1453106072000-5") {
        sql('''set @rownum = -1;
                insert into csi_configuration_browser_connectivity_weight (csi_configuration_browser_connectivity_weights_id, browser_connectivity_weight_id, browser_connectivity_weights_idx)
                select csi_configuration.id, browser_connectivity_weight.id, @rownum := @rownum +1
                from csi_configuration, browser_connectivity_weight
                where not exists(select * from csi_configuration_browser_connectivity_weight)
        ''')
    }

    // map page_weights to first csi_configuration
    changeSet(author: "mmi", id: "1453106072000-6") {
        sql('''set @rownum = -1;
                insert into csi_configuration_page_weight (csi_configuration_page_weights_id, page_weight_id, page_weights_idx)
                select csi_configuration.id, page_weight.id, @rownum := @rownum +1
                from csi_configuration, page_weight
                where not exists(select * from csi_configuration_page_weight)
        ''')
    }

    // map time_to_cs_mappings to first csi_configuration
    changeSet(author: "mmi", id: "1453106072000-7") {
        sql('''set @rownum = -1;
                insert into csi_configuration_time_to_cs_mapping (csi_configuration_time_to_cs_mappings_id, time_to_cs_mapping_id, time_to_cs_mappings_idx)
                select csi_configuration.id, time_to_cs_mapping.id, @rownum := @rownum +1
                from csi_configuration, time_to_cs_mapping
                where not exists(select * from csi_configuration_time_to_cs_mapping)
        ''')
    }
//     ### END INITIAL CSI-CONFIGURATION ###
}

