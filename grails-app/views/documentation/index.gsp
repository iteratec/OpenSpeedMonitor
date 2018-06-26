<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="kickstart_osm"/>
    <title>Documentation</title>
</head>

<body>
<app-documentation
        data-module-path="src/app/documentation/documentation.module#DocumentationModule"></app-documentation>
<asset:stylesheet src="frontend/styles.css"/>
<asset:javascript src="frontend/runtime.js"/>
<asset:javascript src="frontend/polyfills.js"/>
<asset:javascript src="frontend/main.js"/>
</body>
</html>