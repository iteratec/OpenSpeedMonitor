databaseChangeLog = {

	changeSet(author: "nkuhn (generated)", id: "1448545220219-9") {
		dropIndex(indexName: "GetLimitedMedianEventResultsBy", tableName: "event_result")
	}

	changeSet(author: "nkuhn (generated)", id: "1448545220219-10") {
		dropIndex(indexName: "jobResultDate_and_jobResultJobConfigId_idx", tableName: "event_result")
	}

	changeSet(author: "nkuhn (generated)", id: "1448545220219-11") {
		dropIndex(indexName: "wJRD_and_wJRJCId_and_mV_and_cV_idx", tableName: "event_result")
	}

	changeSet(author: "nkuhn (generated)", id: "1448545220219-12") {
		dropIndex(indexName: "started_and_iVal_and_aggr_and_tag_idx", tableName: "measured_value")
	}

    changeSet(author: "nkuhn (manual)", id: "1448545220219-13") {
        dropIndex(indexName: "testId_and_jobConfigLabel_idx", tableName: "job_result")
    }

	changeSet(author: "nkuhn (generated)", id: "1448545220219-14") {
		createIndex(indexName: "GetLimitedMedianEventResultsBy", tableName: "event_result") {
			column(name: "job_result_date")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1448545220219-15") {
		createIndex(indexName: "jobResultDate_and_jobResultJobConfigId_idx", tableName: "event_result") {
			column(name: "job_result_date")

			column(name: "job_result_job_config_id")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1448545220219-16") {
		createIndex(indexName: "wJRD_and_wJRJCId_and_mV_and_cV_idx", tableName: "event_result") {
            column(name: "job_result_date")

            column(name: "job_result_job_config_id")

            column(name: "cached_view")

			column(name: "median_value")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1448545220219-17") {
		createIndex(indexName: "label_uniq_idx", tableName: "job", unique: "true") {
			column(name: "label")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1448545220219-18") {
		createIndex(indexName: "closedAndCalculated_and_started_idx", tableName: "measured_value") {
			column(name: "closed_and_calculated")

			column(name: "started")
		}
	}

	changeSet(author: "nkuhn (generated)", id: "1448545220219-19") {
		createIndex(indexName: "started_and_iVal_and_aggr_idx", tableName: "measured_value") {
            column(name: "started")

            column(name: "interval_id")

            column(name: "aggregator_id")
        }
	}

	changeSet(author: "nkuhn (generated)", id: "1448545220219-20") {
		createIndex(indexName: "measuredValueId_idx", tableName: "measured_value_update_event") {
			column(name: "measured_value_id")
		}
	}

    changeSet(author: "nkuhn (manual)", id: "1448545220219-21") {
        createIndex(indexName: "testId_and_jobConfigLabel_idx", tableName: "job_result") {
            column(name: "test_id")

            column(name: "job_config_label")
        }
    }
}
