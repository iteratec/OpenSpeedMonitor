<!DOCTYPE html>
<%-- <html lang="${org.springframework.web.servlet.support.RequestContextUtils.getLocale(request).toString().replace('_', '-')}"> --%>
<html lang="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}">

<head>

    <asset:stylesheet src="application.css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title><g:layoutTitle default="${meta(name:'app.name')}" /></title>
    <meta charset="utf-8">
    <meta name="viewport"		content="width=device-width, initial-scale=1.0">
    <meta name="description"	content="">
    <meta name="author"			content="">

    <asset:link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>

    <link rel="apple-touch-icon"	href="assets/ico/apple-touch-icon.png">
    <link rel="apple-touch-icon"	href="assets/ico/apple-touch-icon-72x72.png"	sizes="72x72">
    <link rel="apple-touch-icon"	href="assets/ico/apple-touch-icon-114x114.png"	sizes="114x114">

    <g:layoutHead />

</head>

<body>

    <!-----------------------------------------------------Github ribbon-->
    <div id="fork-me">
    <a href="https://github.com/IteraSpeed/OpenSpeedMonitor"><img style="position: fixed; top: -14px; right: -14px; border: 0; z-index: 1040;" src="https://camo.githubusercontent.com/e7bbb0521b397edbd5fe43e7f760759336b5e05f/68747470733a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f72696768745f677265656e5f3030373230302e706e67" alt="Fork me on GitHub" data-canonical-src="https://s3.amazonaws.com/github/ribbons/forkme_right_green_007200.png"></a>
    </div>

    <!-----------------------------------------------------main navigation-->
	<g:render template="/_menu/navbar"/>

    <!-----------------------------------------------------body-->
	<g:render template="/layouts/content"/>

    <!-----------------------------------------------------footer (default or page specific)-->
	<g:if test="${ pageProperty(name:'page.footer') }">
	    <g:pageProperty name="page.footer" />
	</g:if>
	<g:else>
		<g:render template="/layouts/footer"/>														
	</g:else>

    <!-----------------------------------------------------global modal dialogs-->
    <g:render template="/_common/modals/p13nByCookies"/>

    <!-----------------------------------------------------public javascripts-->
    <!--                                                        -public external application.js-->
    <!--                                                        -inline blocks of templates-->
    <asset:javascript src="application.js"/>
    <g:pageProperty name="page.include.p13nByCookies.script" />

    <!-----------------------------------------------------body bottom block for javascripts of root pages-->
	<g:if test="${ pageProperty(name:'page.include.bottom') }">
   		<g:pageProperty name="page.include.bottom" />
	</g:if>

    <!-----------------------------------------------------rickshaw init-->
    <g:if test="${ pageProperty(name:'page.include.rickshaw-init') }">
        <g:pageProperty name="page.include.rickshaw-init" />
    </g:if>

    <!-----------------------------------------------------render all deferred scripts -->
    <asset:deferredScripts/>

    <!-----------------------------------------------------global postload-Javascript-->
    <g:render template="/_common/postloadInitializedJS"/>

</body>

</html>