import de.iteratec.osm.report.UserspecificCsiDashboard

databaseChangeLog = {
	changeSet(author: "bwo (generated)", id: "1456313431000-1") {
		sql('''
            UPDATE userspecific_csi_dashboard SET csi_type_doc_complete=1;
        ''')
	}
}