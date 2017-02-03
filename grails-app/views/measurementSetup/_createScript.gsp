<h2>Write your measurement script</h2>

<div class="form-horizontal">

    <div class="row">

        <div class="col-sm-6">
            <div class="form-group">
                <label for="inputScriptName" class="col-sm-2 control-label" id="">
                    <g:message code="de.iteratec.osm.setupMeasurementWizard.inputScriptNameLabel" default="Script Name"/>
                </label>

                <div class="col-sm-10">
                    <input type="text" class="form-control" id="inputScriptName" required>
                </div>
            </div>

            <div class="form-group">
                <label for="inputScriptDescription"
                       class="col-sm-2 control-label">Description</label>

                <div class="col-sm-10">
                    <textarea class="form-control" id="inputScriptDescription"
                              placeholder="Optional" rows="3"></textarea>
                </div>
            </div>

            <div class="col-sm-offset-2 row">
                <p class="col-sm-12">
                    <g:message code="script.autoComplete.label"
                               default="Press Ctrl + Space to get a list of keywords or valid event names, respectively."/>
                </p>
            </div>

            <div class="form-group">
                <label for="navigationScript" class="col-sm-2 control-label">Code</label>

                <div class="col-sm-10">
                    <div class="fieldcontain ${hasErrors(bean: script, field: 'navigationScript', 'error')}">
                        <textarea id="navigationScript" class="form-control" style="display: none;">${code}</textarea>
                        <span id="setEventName-warning-clone" class="setEventName-warning-icon" style="display: none;"
                              rel="tooltip" data-html="true"></span>
                    </div>
                </div>
            </div>

            <div class="col-sm-offset-2 col-sm-10">
                <div class="form-group">
                    <div class="col-sm-12">
                        <input type="checkbox" id="lineBreakToggle" checked/>
                        <label for="lineBreakToggle" style="display: inline">
                            <g:message code="script.wrapLines.label"/>
                        </label>
                    </div>
                </div>

                <p id="usedVariables"
                   data-instructions="${HtmlUtils.htmlEscape(message(code: 'script.placeholdersInstructions.label'))}"
                   data-usedvars="${HtmlUtils.htmlEscape(message(code: 'codemirror.usedVariables.label'))}">
                </p>

                <div id="newPageOrMeasuredEventInfo" class="alert alert-info" style="display:none;">
                    <div id="newPagesContainer" style="display:none;">
                        ${message(code: 'script.newPage.info')}
                        <div id="newPages"></div>
                        <hr class="style-one">
                    </div>

                    <div id="newMeasuredEventsContainer" style="display:none;">
                        ${message(code: 'script.newMeasuredEvent.info')}
                        <div id="newMeasuredEvents"></div>
                    </div>
                </div>
            </div>

        </div>

        <div class="col-sm-6">
            <a href="https://sites.google.com/a/webpagetest.org/docs/using-webpagetest/scripting" target="_blank">
                <g:message code="de.iteratec.osm.measurement.script.wpt-dsl.link.text"
                           default="Documentation WebPagetest DSL"/>
            </a>

            <p>
                The script defines the steps of your measurement.

            </p>
            <img class="img-thumbnail" id="exampleScriptImg" src="${resource(dir: 'images', file: 'exampleScript.png')}"
                 alt="Example Script"/>
        </div>

    </div> %{-- row --}%

</div>

<div class="row">
    <div class="form-group">
        <div class="col-sm-6 text-right">
            <a data-toggle="tab" class="btn btn-default" id="createScriptTabPreviousButton">
                <i class="fa fa-caret-left" aria-hidden="true"></i>
                Previous
            </a>
            <a data-toggle="tab" class="btn btn-primary" id="createScriptTabNextButton">
                Next
                <i class="fa fa-caret-right" aria-hidden="true"></i>
            </a>
        </div>
    </div>
</div>