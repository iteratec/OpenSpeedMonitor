
<div class="row">
    <div class="col-md-2">
        <ul class="nav nav-pills nav-stacked">
            <li class="active">
                <a data-toggle="pill" href="#AdvancedContent" onClick="showWptInfoPanel()">
                    <g:message code="job.tab.advanced.label" default="Advanced"/>
                </a>
            </li>
            <li>
                <a data-toggle="pill" id="chromeTabLink" href="#ChromeContent" onClick="showWptInfoPanel()">
                    <g:message code="job.tab.chrome.label" default="Chrome"/>
                </a>
            </li>
            <li>
                <a data-toggle="pill" href="#AuthContent" onClick="showWptInfoPanel()">
                    <g:message code="job.tab.auth.label" default="Authentication"/>
                </a>
            </li>
            <li>
                <a data-toggle="pill" href="#BlockContent" onClick="showWptInfoPanel()">
                    <g:message code="job.tab.block.label" default="Block URLs"/>
                </a>
            </li>
            <li>
                <a data-toggle="pill" href="#SPOFContent" onClick="showWptInfoPanel()">
                    <g:message code="job.tab.spof.label" default="SPOF"/>
                </a>
            </li>
            <li>
                <a data-toggle="pill" href="#CustomContent" onClick="showWptInfoPanel()">
                    <g:message code="job.tab.custom.label" default="Custom Metrics"/>
                </a>
            </li>
            <g:if test="${job.id != null}">
                <li>
                    <a data-toggle="pill" href="#ThresholdContent" onClick="hideWptInfoPanel()">
                        <g:message code="job.tab.threshold.label" default="Thresholds"/>
                    </a>
                </li>
            </g:if>
        </ul>
    </div>

    <div class="col-md-6">
        <div class="tab-content" style="margin-top: 5px">
            <div id="AdvancedContent" class="tab-pane active">
                <g:render template="advancedTab" model="${['job': job]}"/>
            </div>

            <div id="ChromeContent" class="tab-pane">
                <g:render template="chromeTab" model="${['job': job]}"/>
            </div>

            <div id="AuthContent" class="tab-pane">
                <g:render template="authTab" model="${['job': job]}"/>
            </div>

            <div id="BlockContent" class="tab-pane">
                <g:render template="blockTab" model="${['job': job]}"/>
            </div>

            <div id="SPOFContent" class="tab-pane">
                <g:render template="spofTab" model="${['job': job]}"/>
            </div>

            <div id="CustomContent" class="tab-pane">
                <g:render template="customTab" model="${['job': job]}"/>
            </div>
            <g:if test="${job.id != null}">
                <div id="ThresholdContent" class="tab-pane">
                    <g:render template="threshold/thresholdTab" model="${['job': job]}"/>
                </div>
            </g:if>
        </div>
    </div>

    <div class="col-sm-3" id="wptInfoPanel">
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
    </div>

</div>
