<g:set var="lang" value="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'}"/>
<div id="Navbar" class="navbar navbar-fixed-top navbar-inverse">
    <div class="container-fluid">
        <div class="navbar-header">
            <a class="navbar-brand" href="${createLink(uri: '/')}">
                <img class="logo"
                     src="${resource(dir: 'images', file: 'OpenSpeedMonitor-onblack-monitorWritingLight_39pxHeight.gif')}"
                     alt="${meta(name: 'app.name')}"/>
            </a>
        </div>
		<ul class="nav navbar-nav navbar-right">
            <g:render template="/_menu/gotoPage"/>
            <g:render template="/_menu/user"/>
            <g:render template="/_menu/admin"/>
            <g:render template="/_menu/info"/>
            <g:render template="/_menu/language"/>
        </ul>
    </div>
</div>
