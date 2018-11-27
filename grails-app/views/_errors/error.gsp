<html>
	<head>
		<title>Grails Runtime Exception</title>
		<meta name="layout" content="layoutOsm" />
		<g:set var="layout_nomainmenu"		value="${true}" scope="request"/>
		<g:set var="layout_nosecondarymenu"	value="${true}" scope="request"/>
	</head>

  <body>

      <section id="Error">
          <div class="big-message">
              <div class="container">
                  <h1>
                      <g:message code="error.callout"/>
                  </h1>
                  <h2>
                      <g:message code="error.title"/>
                  </h2>
                  <div class="alert alert-danger">
                      <strong>Error ${request.'javax.servlet.error.status_code'}: </strong>
                      <g:if test="${request.'javax.servlet.error.message' == null}">
                      </g:if>
                      <g:elseif test="${request.'javax.servlet.error.message'.indexOf(':') != -1}">
                          ${request.'javax.servlet.error.message'?.substring(0, request.'javax.servlet.error.message'?.indexOf(':')).encodeAsHTML()}
                      </g:elseif>
                      <g:else>
                          ${request.'javax.servlet.error.message'?.encodeAsHTML()}
                      </g:else>
                      <g:if test="${exception}">
                          ${exception.className}
                          has problem at line ${exception.lineNumber}
                      </g:if>
                  </div>

                  <div class="actions">
                      <a href="${createLink(uri: '/')}" class="btn btn-lg btn-primary">
                          <i class="fas fa-chevron-left"></i>
                          <g:message code="error.button.backToHome"/>
                      </a>
                      <a href="mailto:osm@iteratec.de" class="btn btn-lg btn-success">
                          <i class="fas fa-envelope"></i>
                          <g:message code="error.button.contactSupport"/>
                      </a>
                  </div>
              </div>
          </div>
      </section>
  
  </body>
</html>