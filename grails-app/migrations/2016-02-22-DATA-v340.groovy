import org.joda.time.DateTime

databaseChangeLog = {

    //set value for new attribute
    changeSet(author: "bka", id: "1452546683118-2") {
        sql(''' update api_key set allowed_for_nightly_database_cleanup_activation = false ''')
    }

    // ### INITIAL CSI-CONFIGURATION ######################################################################

    // creating first page_weights
    changeSet(author: "mmi", id: "1453106072000-1") {
        preConditions(onFail: 'MARK_RAN') {
            and {
                sqlCheck(expectedResult: '0', 'select count(*) from page_weight')
                not {
                    sqlCheck(expectedResult: '0', 'select count(*) from page')
                }
            }

        }
        sql('''insert into page_weight(select tb1.*
                from (select id as id, 1 as version, id as page_id, weight as weight from page) as tb1)
        ''')
    }

    // creating a first csi-configuration
    changeSet(author: "mmi", id: "1453106072000-4") {
        preConditions(onFail: 'MARK_RAN') {
            and {
                sqlCheck(expectedResult: '0', 'select count(*) from csi_configuration')
                not {
                    sqlCheck(expectedResult: '0', 'select count(*) from csi_day')
                }
            }
        }
        sql('insert into csi_configuration (version, description, label, csi_day_id) values (1, "a first csi configuration", "initial csi configuration", (select csi_day.id from csi_day))')
    }

    // map browser_connectivity_weights to first csi_configuration
    changeSet(author: "mmi", id: "1453106072000-5") {
        preConditions(onFail: 'MARK_RAN') {
            and {
                sqlCheck(expectedResult: '0', 'select count(*) from csi_configuration_browser_connectivity_weight')
                not {
                    sqlCheck(expectedResult: '0', 'select count(*) from csi_configuration')
                }
                not {
                    sqlCheck(expectedResult: '0', 'select count(*) from browser_connectivity_weight')
                }
            }
        }
        sql('''
                insert into csi_configuration_browser_connectivity_weight (csi_configuration_browser_connectivity_weights_id, browser_connectivity_weight_id)
                select csi_configuration.id, browser_connectivity_weight.id
                from csi_configuration, browser_connectivity_weight
        ''')
    }

    // map page_weights to first csi_configuration
    changeSet(author: "mmi", id: "1453106072000-6") {
        preConditions(onFail: 'MARK_RAN') {
            and {
                sqlCheck(expectedResult: '0', 'select count(*) from csi_configuration_page_weight')
                not {
                    sqlCheck(expectedResult: '0', 'select count(*) from csi_configuration')
                }
                not {
                    sqlCheck(expectedResult: '0', 'select count(*) from page_weight')
                }
            }
        }
        sql('''
                insert into csi_configuration_page_weight (csi_configuration_page_weights_id, page_weight_id)
                select csi_configuration.id, page_weight.id
                from csi_configuration, page_weight
        ''')
    }

    // map time_to_cs_mappings to first csi_configuration
    changeSet(author: "mmi", id: "1453106072000-7") {
        preConditions(onFail: 'MARK_RAN') {
            and {
                sqlCheck(expectedResult: '0', 'select count(*) from csi_configuration_time_to_cs_mapping')
                not {
                    sqlCheck(expectedResult: '0', 'select count(*) from csi_configuration')
                }
                not {
                    sqlCheck(expectedResult: '0', 'select count(*) from time_to_cs_mapping')
                }
            }
        }
        sql('''
                insert into csi_configuration_time_to_cs_mapping (csi_configuration_time_to_cs_mappings_id, time_to_cs_mapping_id)
                select csi_configuration.id, time_to_cs_mapping.id
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

    //     ### END INITIAL CSI-CONFIGURATION ######################################################################

    /**
     * In the past we had grailschanges using GORM. There was a possible bug,
     * which prevents us from using GORM in grailschanges with java 8.
     * We had to delete the old entries and rewrite this changes.
     * Because there are instances which already ran the old changelog,
     * we first check if the changelog with the given id is already in the database. If this is not
     * the case we can safely execute the rewritten changelog
     **/
    changeSet(author: "mmi", id: "1453106072001-1") {
        preConditions(onFail: 'MARK_RAN') {
            // The old changelog is not already ran
            sqlCheck(expectedResult: '0', "select count(*) from DATABASECHANGELOG where id = '1453106072000-9'")
            // The application is not started with empty database
            not {
                sqlCheck(expectedResult: '0', "select count(*) from csi_aggregation")
            }
        }
        // add a valid connectivity-profile to every hourly-measured-value
        grailsChange {
            change {
                int maxItemsToProcess = 10000

                int hourlyIntervalID = sql.firstRow("SELECT id FROM csi_aggregation_interval where interval_in_minutes = ?", [60]).id
                int dailyIntervalId = sql.firstRow("SELECT id FROM csi_aggregation_interval where interval_in_minutes = ?", [24 * 60]).id
                int weeklyIntervalId = sql.firstRow("SELECT id FROM csi_aggregation_interval where interval_in_minutes = ?", [7 * 24 * 60]).id
                int measuredEventAggregatorID = sql.firstRow("SELECT id FROM aggregator_type where name = ?", ["measuredEvent"]).id
                int pageAggregatorID = sql.firstRow("SELECT id FROM aggregator_type where name = ?", ["page"]).id
                int shopAggregatorID = sql.firstRow("SELECT id FROM aggregator_type where name = ?", ["shop"]).id
                def csiSystemAggregatorID = sql.firstRow("SELECT id FROM aggregator_type where name = ?", ["csiSystem"])?.id

                int amountMvsHourly = sql.firstRow(
                        "select count(*) from csi_aggregation where aggregator_id = ? and interval_id = ? and underlying_event_results_by_wpt_doc_complete !=''",
                        [measuredEventAggregatorID, hourlyIntervalID])[0]

                int amountLoops = amountMvsHourly / maxItemsToProcess
                amountLoops.times { loopNumber ->
                    int offset = loopNumber * maxItemsToProcess
                    sql.eachRow("SELECT * FROM csi_aggregation where aggregator_id = ? and interval_id = ? and underlying_event_results_by_wpt_doc_complete !=''",
                            [measuredEventAggregatorID, hourlyIntervalID], offset, maxItemsToProcess) { csiAggregation ->
                        String eventResultIds = csiAggregation['underlying_event_results_by_wpt_doc_complete']
                        def eventResultsOfCsiAggregation = sql.rows("SELECT * FROM event_result where id in (:ids)", [ids: eventResultIds])
                        int amountDifferentConnectivityProfiles = eventResultsOfCsiAggregation*.connectivity_profile_id.unique(false).size()

                        // simple case: if all results have same connectivity
                        if (amountDifferentConnectivityProfiles == 1 && eventResultsOfCsiAggregation.first().connectivity_profile_id) {
                            // ... then add connectivity from any of its results to CsiAggregation
                            sql.executeUpdate("UPDATE csi_aggregation SET connectivity_profile_id = :cp where id=:csiAggregationID", [cp: eventResultsOfCsiAggregation.
                                    first().connectivity_profile_id, csiAggregationID                                                   : csiAggregation.id])
                        } else {
                            // write update events for csi aggregations based on eventResults
                            eventResultsOfCsiAggregation.each { eventResult ->
                                DateTime started = new DateTime(eventResult['date_created']).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).withHourOfDay(0)
                                // Daily Page
                                sql.eachRow("SELECT * FROM csi_aggregation WHERE aggregator_id = ? AND interval_id = ? AND started = ?",
                                        [pageAggregatorID, dailyIntervalId, started.toDate()]) {
                                    sql.executeInsert("INSERT INTO csi_aggregation_update_event (version, date_of_update, csi_aggregation_id, update_cause) VALUES (0, ?, ?, 'OUTDATED')",
                                            [new Date(), it.id])
                                    sql.executeUpdate("UPDATE csi_aggregation SET closed_and_calculated = false WHERE id = ?", [it.id])
                                }
                                // Daily Shop
                                sql.eachRow("SELECT * FROM csi_aggregation WHERE aggregator_id = ? AND interval_id = ? AND started = ?",
                                        [shopAggregatorID, dailyIntervalId, started.toDate()]) {
                                    sql.executeInsert("INSERT INTO csi_aggregation_update_event (version, date_of_update, csi_aggregation_id, update_cause) VALUES (0, ?, ?, 'OUTDATED')",
                                            [new Date(), it.id])
                                    sql.executeUpdate("UPDATE csi_aggregation SET closed_and_calculated = false WHERE id = ?", [it.id])
                                }
                                // Daily CsiSystem
                                if (csiSystemAggregatorID) {
                                    sql.eachRow("SELECT * FROM csi_aggregation WHERE aggregator_id = ? AND interval_id = ? AND started = ?",
                                            [csiSystemAggregatorID, dailyIntervalId, started.toDate()]) {
                                        sql.executeInsert("INSERT INTO csi_aggregation_update_event (version, date_of_update, csi_aggregation_id, update_cause) VALUES (0, ?, ?, 'OUTDATED')",
                                                [new Date(), it.id])
                                        sql.executeUpdate("UPDATE csi_aggregation SET closed_and_calculated = false WHERE id = ?", [it.id])
                                    }
                                }
                                // Weekly Page
                                started = started.withDayOfWeek(5) // Friday
                                if (started.isAfter(new DateTime(eventResult['date_created']))) {
                                    started = started.minusWeeks(1);
                                }
                                sql.eachRow("SELECT * FROM csi_aggregation WHERE aggregator_id = ? AND interval_id = ? AND started = ?",
                                        [pageAggregatorID, weeklyIntervalId, started.toDate()]) {
                                    sql.executeInsert("INSERT INTO csi_aggregation_update_event (version, date_of_update, csi_aggregation_id, update_cause) VALUES (0, ?, ?, 'OUTDATED')",
                                            [new Date(), it.id])
                                    sql.executeUpdate("UPDATE csi_aggregation SET closed_and_calculated = false WHERE id = ?", [it.id])
                                }
                                // Weekly Shop
                                sql.eachRow("SELECT * FROM csi_aggregation WHERE aggregator_id = ? AND interval_id = ? AND started = ?",
                                        [shopAggregatorID, weeklyIntervalId, started.toDate()]) {
                                    sql.executeInsert("INSERT INTO csi_aggregation_update_event (version, date_of_update, csi_aggregation_id, update_cause) VALUES (0, ?, ?, 'OUTDATED')",
                                            [new Date(), it.id])
                                    sql.executeUpdate("UPDATE csi_aggregation SET closed_and_calculated = false WHERE id = ?", [it.id])
                                }
                                // Weekly CsiSystem
                                if (csiSystemAggregatorID) {
                                    sql.eachRow("SELECT * FROM csi_aggregation WHERE aggregator_id = ? AND interval_id = ? AND started = ?",
                                            [csiSystemAggregatorID, weeklyIntervalId, started.toDate()]) {
                                        sql.executeInsert("INSERT INTO csi_aggregation_update_event (version, date_of_update, csi_aggregation_id, update_cause) VALUES (0, ?, ?, 'OUTDATED')",
                                                [new Date(), it.id])
                                        sql.executeUpdate("UPDATE csi_aggregation SET closed_and_calculated = false WHERE id = ?", [it.id])
                                    }
                                }
                            }

                            // remove hourly csi aggregation...
                            sql.executeUpdate("DELETE FROM csi_aggregation_event_result where csi_aggregation_underlying_event_results_by_visually_complete_id = ?", [csiAggregation.id])
                            sql.executeUpdate("DELETE FROM csi_aggregation where id = ?", [csiAggregation.id])

                            // ... and calc it again for each connectivity profile
                            eventResultsOfCsiAggregation.groupBy({ result -> result['connectivity_profile_id'] }).each { connectivityProfileID, eventResultsForConnectivityProfile ->
                                double csByWptDocCompleteInPercent = 0.0
                                int csByWptDocCompleteInPercentCounter = 0
                                double csByWptVisuallyCompleteInPercent = 0.0
                                int csByWptVisuallyCompleteInPercentCounter = 0
                                List<String> underlying_event_results_by_wpt_doc_complete = []

                                // sum all results
                                eventResultsForConnectivityProfile.each { eventResult ->
                                    if (eventResult['cs_by_wpt_doc_complete_in_percent']) {
                                        csByWptDocCompleteInPercent += eventResult['cs_by_wpt_doc_complete_in_percent']
                                        csByWptDocCompleteInPercentCounter++
                                        underlying_event_results_by_wpt_doc_complete << eventResult.id
                                    }
                                    if (eventResult['cs_by_wpt_visually_complete_in_percent']) {
                                        csByWptVisuallyCompleteInPercent += eventResult['cs_by_wpt_visually_complete_in_percent']
                                        csByWptVisuallyCompleteInPercentCounter++
                                    }
                                }

                                // calculate the mean
                                if (csByWptDocCompleteInPercentCounter > 0) {
                                    csByWptDocCompleteInPercent /= csByWptDocCompleteInPercentCounter
                                }
                                if (csByWptVisuallyCompleteInPercentCounter > 0) {
                                    csByWptVisuallyCompleteInPercent /= csByWptVisuallyCompleteInPercentCounter
                                }

                                // store new csiAggregation
                                def newCsiAggregationID = sql.executeInsert('''INSERT INTO csi_aggregation
                                    (version, aggregator_id, interval_id, underlying_event_results_by_wpt_doc_complete, started, tag, cs_by_wpt_doc_complete_in_percent, closed_and_calculated, connectivity_profile_id, csi_system_id, cs_by_wpt_visually_complete_in_percent) VALUES
                                    (0, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)''',
                                        [measuredEventAggregatorID, hourlyIntervalID, underlying_event_results_by_wpt_doc_complete.join(','), csiAggregation.started, csiAggregation.tag, csByWptDocCompleteInPercent, csiAggregation['closed_and_calculated'], connectivityProfileID, csiAggregation['csi_system_id'], csByWptVisuallyCompleteInPercent])[0][0]// gets id of inserted object

                                // link underlying event result
                                eventResultsForConnectivityProfile.each { eventResult ->
                                    if (eventResult['cs_by_wpt_visually_complete_in_percent']) {
                                        sql.executeInsert("INSERT INTO csi_aggregation_event_result VALUES (?,?)"[newCsiAggregationID, eventResult.id])
                                    }
                                }
                                // create update event
                                sql.executeInsert("INSERT INTO csi_aggregation_update_event (version, date_of_update, csi_aggregation_id, update_cause) VALUES (0, ?, ?, 'CALCULATED')",
                                        [new Date(), newCsiAggregationID])
                            }
                        }
                    }
                }
            }
        }
    }

    // ### Delete old hourOfDay data ###
    changeSet(author: "mmi", id: "1453304178000-1") {
        sql('''
            delete from hour_of_day
        ''')
    }

    changeSet(author: "bwo", id: "1455705739000-1") {
        sql('''
            UPDATE osm_configuration SET initial_chart_width_in_pixels=1070;
        ''')
    }

    // ### rename AggregatorType ###
    changeSet(author: "bwo", id: "1456135825001-1") {
        /**
         * In the past we had grailschanges using GORM. There was a possible bug,
         * which prevents us from using GORM in grailschanges with java 8.
         * We had to delete the old entries and rewrite this changes.
         * Because there are instances which already ran the old changelog,
         * we first check if the changelog with the given id is already in the database. If this is not
         * the case we can safely execute the rewritten changelog
         **/
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "select count(*) from DATABASECHANGELOG where id = '1456135825000-1'")
        }
        change {
            sql("UPDATE aggregator_type SET name = 'csByWptDocCompleteInPercentCached' WHERE name = 'customerSatisfactionInPercentCached'")
            sql("UPDATE aggregator_type SET name = 'csByWptDocCompleteInPercentUncached' WHERE name = 'customerSatisfactionInPercentUncached'")
        }
    }

}
