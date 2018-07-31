<div class="row form-horizontal">
    <div class="col-md-7" id="all_inputs" >
        %{------------------------------------------------------------------------------ select server type--}%
        <div class="row">
            <div class="col-md-12">
                <div class="panel-body">
                    <g:message code="de.iteratec.osm.ui.setupwizards.infra.selectServer"
                               default="Select server type:"></g:message>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-8">
                <g:select  name="serverSelect"
                           class="form-control chosen-select"
                           from="${serverCreationOptions}"
                           id="serverSelect"
                           value="${serverSelect}"
                           valueMessagePrefix="de.iteratec.osm.ui.setupwizards.infra">
                </g:select>
            </div>
        </div>

        %{------------------------------------------------------------------------------ setup wpt server --}%
        <div class="form-group has-feedback">
            <div class="">
                <label for="serverName" class="col-md-2 control-label">
                    <g:message code="de.iteratec.osm.ui.setupwizards.infra.name" default="name"></g:message>:
                </label>

                <div class="col-md-6">
                    <input name="serverName" class="form-control" id="serverName" value="${serverName}"
                           placeholder="${g.message(code: 'de.iteratec.osm.ui.setupwizards.infra.enterServerName', default: 'e.g. My WPT Server')}"
                           required
                           data-error="${g.message(code: 'de.iteratec.osm.ui.setupwizards.infra.errors.missingName', default: 'Please set a name.')}"/>
                    <span class="fas form-control-feedback" aria-hidden="true"></span>
                </div>
            </div>
        </div>

        <div class="form-group has-feedback">
            <div class="">
                <label for="serverUrl" class="col-md-2 control-label">
                    <g:message code="de.iteratec.osm.ui.setupwizards.infra.address" default="URL"></g:message>:
                </label>

                <div class="col-md-6">
                    <input name="serverUrl" class="form-control" id="serverUrl" value="${serverUrl}"
                           placeholder="${g.message(code: 'de.iteratec.osm.ui.setupwizards.infra.enterServerAddress', default: 'e.g. http://wpt.mycompany.org')}"
                           type="url" required pattern="((https?:\/\/)[\-a-zA-Z\d]+(\.[a-zA-Z\d]+)*)(:\d+)?(\/[a-zA-Z&=\d_?]+)*\/?"
                           data-error="${g.message(code: 'de.iteratec.osm.ui.setupwizards.infra.errors.missingUrl', default: 'Please set a valid URL.')}"/>
                    <span class="fas form-control-feedback" aria-hidden="true"></span>
                </div>
            </div>

            <div class="">
                <div id = "serverUrl_errors" class="col-md-offset-2 col-md-6 help-block with-errors">
                </div>
            </div>
        </div>

        <div class="form-group has-feedback">
            <div class="" id="row_serverApiKey">
                <label for="serverApiKey" class="col-md-2 control-label">
                    <g:message code="de.iteratec.osm.ui.setupwizards.infra.key" default="API key"></g:message>:
                </label>

                <div class="col-md-6">
                    <input name="serverApiKey" class="form-control" id="serverApiKey" value="${serverApiKey}"
                           data-error="${g.message(code: 'de.iteratec.osm.ui.setupwizards.infra.errors.missingKey', default: 'Please set a key.')}"
                           required/>
                    <span class="fas form-control-feedback" aria-hidden="true"></span>
                </div>
            </div>

            <div class="">
                <div class="col-md-offset-2 col-md-6" id="apiKeyPrompt">
                    <g:message code="de.iteratec.osm.ui.setupwizards.infra.keyPrompt"></g:message>
                </div>
            </div>
        </div>

    </div>

    <div class="col-md-4">
        %{------------------------------------------------------------------------------ webpagetest.org info box --}%
        <div id="wptKeyInfo">
            <div class="panel panel-info" id="wptKeyInfoPanel">
                <div class="panel-heading">
                    <i class="fas fa-info-circle" aria-hidden="true"></i>
                    <g:message code="default.info.title" default="Information"/>
                </div>

                <div class="panel-body">
                    <g:message code="de.iteratec.osm.ui.setupwizards.infra.keyInfo"
                               default="Apply for a key on this website"></g:message>
                    <img class="infoImage thumbnail" id="keyApplicationImg"
                         src="${resource(dir: 'images', file: 'WPT.png')}"
                         alt="Key Application"/>
                </div>
            </div>
        </div>

        %{------------------------------------------------------------------------------ custom server info box --}%
        <div class="hidden" id="customServerInfo">
            <div class="panel panel-info" id="customServerInfoPanel">
                <div class="panel-heading">
                    <i class="fas fa-info-circle" aria-hidden="true"></i>
                    <g:message code="default.info.title" default="Information"/>
                </div>

                <div class="panel-body">
                    <g:message code="de.iteratec.osm.ui.setupwizards.infra.customInfo"
                               default="Setup your own server"></g:message>
                </div>
            </div>
        </div>
    </div>

</div>

%{------------------------------------------------------------------------------ navigation row --}%
<div class="row navigationRow form-horizontal">
    <div class="col-md-12">
        %{-- cancel button --}%
        <a class="btn btn-default" href="/InfrastructureSetup/cancel" id="cancelSetup">
            <i class="fas fa-times" aria-hidden="true"></i>
            <g:message code="script.versionControl.cancel.button" default=" Cancel"/>
        </a>
        %{-- done button --}%
        <button class="btn btn-primary" type="submit" id="finishButton">
            <g:message code="de.iteratec.osm.ui.setupwizards.infra.submit" default="submit"></g:message>
        </button>
    </div>
</div>
