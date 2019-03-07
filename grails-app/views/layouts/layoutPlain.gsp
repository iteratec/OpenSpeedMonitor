<!DOCTYPE html>
<html lang="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}">

    <head>

        <asset:stylesheet src="application.css"/>
        <asset:stylesheet src="frontend/styles.css"/>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><g:layoutTitle default="${meta(name: 'app.name')}"/></title>
        <meta charset="utf-8">
        <meta name="viewport"       content="width=device-width, initial-scale=1.0">
        <meta name="description"    content="">
        <meta name="author"         content="">

        <asset:link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>

        <link rel="apple-touch-icon" href="assets/ico/apple-touch-icon.png">
        <link rel="apple-touch-icon" href="assets/ico/apple-touch-icon-72x72.png" sizes="72x72">
        <link rel="apple-touch-icon" href="assets/ico/apple-touch-icon-114x114.png" sizes="114x114">

        <g:layoutHead/>

        <asset:javascript src="rum/rum.js"/>
    </head>

    <body style="padding-left: 0px; margin-bottom: 0px">
        <!-----------------------------------------------------main navigation-->

        <!-----------------------------------------------------body-->
        <g:render template="/layouts/layoutPlainContent"/>

        <!-----------------------------------------------------footer (default or page specific)-->

        <!-----------------------------------------------------global modal dialogs-->
        <g:render template="/_common/modals/p13nByCookies"/>

        <!-----------------------------------------------------public javascripts-->
        <!--                                                        -public external application.js-->
        <!--                                                        -inline blocks of templates-->
        <g:render template="/_common/jsGlobals"/>
        <asset:javascript src="application.js"/>
        <g:pageProperty name="page.include.p13nByCookies.script"/>


        <g:if test="${pageProperty(name: 'page.needsAngular')}">
            <asset:javascript src="frontend/runtime.js"/>
            <asset:javascript src="frontend/polyfills.js"/>
            <asset:javascript src="frontend/main.js"/>
        </g:if>

        <!-----------------------------------------------------body bottom block for javascripts of root pages-->
        <g:if test="${ pageProperty(name:'page.include.bottom') }">
            <g:pageProperty name="page.include.bottom" />
        </g:if>

        <!-----------------------------------------------------rickshaw init-->
        <g:if test="${pageProperty(name: 'page.include.rickshaw-init')}">
            <g:pageProperty name="page.include.rickshaw-init"/>
        </g:if>

        <!-----------------------------------------------------render all deferred scripts -->
        <asset:deferredScripts/>

        <!-----------------------------------------------------global postload-Javascript-->
        <g:render template="/_common/postloadInitializedJS"/>

    </body>

</html>