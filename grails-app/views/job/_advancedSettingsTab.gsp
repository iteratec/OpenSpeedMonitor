
<div class="row">
    <div class="col-md-2">
        <ul class="nav nav-pills nav-stacked">
            <li class="active">
                <a data-toggle="pill" href="#AdvancedContent">
                    <g:message code="job.tab.advanced.label" default="Advanced"/>
                </a>
            </li>
            <li>
                <a data-toggle="pill" id="chromeTabLink" href="#ChromeContent">
                    <g:message code="job.tab.chrome.label" default="Chrome"/>
                </a>
            </li>
            <li>
                <a data-toggle="pill" href="#AuthContent">
                    <g:message code="job.tab.auth.label" default="Authentication"/>
                </a>
            </li>
            <li>
                <a data-toggle="pill" href="#BlockContent">
                    <g:message code="job.tab.block.label" default="Block URLs"/>
                </a>
            </li>
            <li>
                <a data-toggle="pill" href="#SPOFContent">
                    <g:message code="job.tab.spof.label" default="SPOF"/>
                </a>
            </li>
            <li>
                <a data-toggle="pill" href="#CustomContent">
                    <g:message code="job.tab.custom.label" default="Custom Metrics"/>
                </a>
            </li>
            <g:if test="${job.id != null}">
                <li>
                    <a data-toggle="pill" href="#ThresholdContent">
                        <g:message code="job.tab.threshold.label" default="Thresholds"/>
                    </a>
                </li>
            </g:if>
        </ul>
    </div>

    <div class="col-md-10">
        <div class="tab-content" style="margin-top: 5px">
            <div id="AdvancedContent" class="tab-pane active">
                <g:render template="advancedSettingsTabContent" model="${['targetTabTemplate': 'advancedTab', 'job': job, 'showWptInfoPanel': true]}" />
            </div>

            <div id="ChromeContent" class="tab-pane">
                <g:render template="advancedSettingsTabContent" model="${['targetTabTemplate': 'chromeTab', 'job': job, 'showWptInfoPanel': true]}" />
            </div>

            <div id="AuthContent" class="tab-pane">
                <g:render template="advancedSettingsTabContent" model="${['targetTabTemplate': 'authTab', 'job': job, 'showWptInfoPanel': true]}" />
            </div>

            <div id="BlockContent" class="tab-pane">
                <g:render template="advancedSettingsTabContent" model="${['targetTabTemplate': 'blockTab', 'job': job, 'showWptInfoPanel': true]}" />
            </div>

            <div id="SPOFContent" class="tab-pane">
                <g:render template="advancedSettingsTabContent" model="${['targetTabTemplate': 'spofTab', 'job': job, 'showWptInfoPanel': true]}" />
            </div>

            <div id="CustomContent" class="tab-pane">
                <g:render template="advancedSettingsTabContent" model="${['targetTabTemplate': 'customTab', 'job': job, 'showWptInfoPanel': true]}" />
            </div>
            <g:if test="${job.id != null}">
                <div id="ThresholdContent" class="tab-pane">
                    <g:render template="advancedSettingsTabContent" model="${['targetTabTemplate': 'threshold/thresholdTab', 'job': job, 'showWptInfoPanel': false]}" />
                </div>
            </g:if>
        </div>
    </div>

</div>
