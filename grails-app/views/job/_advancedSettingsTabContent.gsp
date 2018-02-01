<div class="col-md-7">
    <g:render template="${targetTabTemplate}" model="${['job': job]}"/>
</div>
<div class="col-md-4">
    <g:if test="${showWptInfoPanel == true}">
        <div class="panel panel-info">
            <div class="panel-heading">
                <i class="fa fa-info-circle" aria-hidden="true"></i>
                <g:message code="default.info.title" default="Information"/>
            </div>

            <div class="panel-body">
                <p><g:message code="job.description" default="The options on this page are equivalent to those on the www.webpagetest.org website. More information about the API can be found here"/></p>
                <a href="https://sites.google.com/a/webpagetest.org/docs/advanced-features/webpagetest-restful-apis" target="_blank">
                    <img class="infoImage thumbnail" id="infoImage"
                         src="${resource(dir: 'images', file: 'api.png')}"
                         alt="API"/>
                </a>
            </div>
        </div>
    </g:if>
</div>