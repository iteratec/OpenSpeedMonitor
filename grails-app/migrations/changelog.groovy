databaseChangeLog = {
    include file: '2015-11-18-SCHEME-initial-liquibase.groovy'
    include file: '2015-11-18-DATA-set-initial-csi-transformation.groovy'
    include file: '2015-11-18-DATA-multiply-csi-values-by-100.groovy'
    include file: '2015-11-26-SCHEME-optimizing-indices.groovy'
    include file: '2015-12-09-SCHEME-csi-configuration.groovy'
    include file: '2015-12-15-SCHEME-measured-value-and-connectivity-profile.groovy'
    include file: '2015-12-16-DATA-delete-invalid-default-csi-mappings.groovy'
    include file: '2015-12-22-DATA-delete-measured-value-update-events.groovy'
    include file: '2015-12-23-SCHEME-CsiDay-class-with-hoursOfDay.groovy'
    include file: '2015-12-23-SCHEME-replaced-hourOfDays-with-CsiDay-in-CsiConfiguration.groovy'
    include file: '2015-12-23-DATA-convert-hoursOfDay-to-CsiDay.groovy'
    include file: '2015-12-23-SCHEME-added-csiConfiguration-to-jobGroup.groovy'
    include file: '2016-01-04-DATA-create-initial-browser-connectivity-weights.groovy'

    include file: 'next-release-SCHEME.groovy'
    include file: 'next-release-DATA.groovy'

	include file: '2016-01-27-SCHEME-CustomDashboardJSValues.groovy'
}
