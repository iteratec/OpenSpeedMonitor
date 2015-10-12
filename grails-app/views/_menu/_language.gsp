<g:set var="lang"
       value="${session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE' ?: org.springframework.web.servlet.support.RequestContextUtils.getLocale(request).toString().substring(0, 2)}"/>
<g:set var="currentURL" value="${request.forwardURI}"/>

<ul class="nav secondary-nav language-dropdown pull-right">
    <li class="dropdown dropdown-btn js-language-dropdown">
        <a class="dropdown-toggle" role="button" data-toggle="dropdown" data-target="#" href="javascript:;">
            <img class="flag flag-${lang.toString()}"/>
        </a>
        <ul class="dropdown-menu dropdown-menu-dark" role="menu">
            <g:set var="allLocales" value="${grailsApplication.config.grails.i18n.locales}"/>
            <g:each status="i" var="locale" in="${allLocales}">
                <li>
                    <a class="js-language-link"
                       title="${message(code: 'language.' + locale, default: locale)}"
                       data-lang-code="${locale}"
                       href="${currentURL + '?lang=' + locale + (params.isEmpty() ? '' : '&') + params.entrySet().findAll({it.key != 'lang'}).collect({ it.key + '=' + it.value }).join('&')}">
                        <img class="flag flag-${locale}"/>
                        <g:message code="language.${locale}" default="${locale}"/>
                    </a>
                </li>
            </g:each>

        </ul>
    </li>
</ul>