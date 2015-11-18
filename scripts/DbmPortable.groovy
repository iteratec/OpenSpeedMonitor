target(default: "Murderizes default schema names from liquibase schemas") {
    new File( 'grails-app/migrations' ).eachFile{ file ->
        if( !file.name.startsWith( '.' ) ){
            file.write(
                    file.text.replaceAll( /(baseTableSchemaName: ".+?",)?(referencedTableSchemaName: ".+?",)?(schemaName: ".+?",)?/,'' )
            )
        }
    }
}
