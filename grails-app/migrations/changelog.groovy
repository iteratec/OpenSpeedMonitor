databaseChangeLog = {
	include file: '2015-11-18-SCHEME-initial-liquibase.groovy'
    include file: '2015-11-18-DATA-set-initial-csi-transformation.groovy'
    include file: '2015-11-18-DATA-multiply-csi-values-by-100.groovy'
    include file: '2015-11-26-SCHEME-optimizing-indices.groovy'
	include file: '2015-12-15-SCHEME-measured-value-and-connectivity-profile.groovy'
    include file: '2015-12-16-DATA-delete-invalid-default-csi-mappings.groovy'
}
