<div class="row form-horizontal">

    %{-- select server type--}%
    <div class="col-sm-6">
        <div class="col-sm-10">
            <div class="panel-body">
                <g:message code="de.iteratec.osm.ui.setupwizards.infra.selectServer" default="selectServer"></g:message>
            </div>
        <g:select name = "serverSelect"
                  class="form-control chosen-select"
                  from="${["WPTServer","CustomServer"]}"
                  id="serverSelect"
                  value="${serverSelect}"
                  valueMessagePrefix="de.iteratec.osm.ui.setupwizards.infra">
        </g:select>
            <div style="line-height:100%;">
                <br>
            </div>
        </div>

    %{-- select key --}%
    <div class="col-sm-8" id="WPTKeyFields">
        <div class="panel-body">
            <g:message code="de.iteratec.osm.ui.setupwizards.infra.keyPrompt" default="keyPrompt"></g:message>
        </div>

        <div class="col-sm-12">
            <div class="form-group" id="WPTKeyForm">
                %{-- label --}%
                <div class="col-sm-3">
                    <label for="inputWPTKey" class="col-sm-3 control-label">
                        <g:message code="de.iteratec.osm.ui.setupwizards.infra.WPTKey" default="WPTKey"></g:message>
                    </label>
                </div>

                %{-- input field --}%
                <div class="col-sm-8">
                    <div class="form-group">
                        <g:textField name="inputWPTKey" class="form-control" id="inputWPTKey" value="${inputWPTKey}"
                                     placeholder="${g.message(code: 'de.iteratec.osm.ui.setupwizards.infra.enterAPIKey', default:'enterAPIKey')}">
                        </g:textField>
                    </div>
                </div>
            </div>
        </div>
    </div>

    %{-- setup custom server --}%
    <div class="col-sm-8 hidden" id="CustomServerFields">
        <div class="panel-body">
            <g:message code="de.iteratec.osm.ui.setupwizards.infra.customPrompt" default="customPrompt"></g:message>
        </div>

        <div class="col-sm-10">
            <div class="form-group" id="CustomServerForm">
                %{-- labels --}%
                <div class="col-sm-3">
                    <div class="form-group">
                        <label for="inputServerName" class="col-sm-2 control-label">
                            <g:message code="de.iteratec.osm.ui.setupwizards.infra.name" default="name"></g:message>
                        </label>
                    </div>
                    <div style="line-height:40%;">
                        <br>
                    </div>
                    <div class="form-group">
                        <label for="inputServerAddress" class="col-sm-2 control-label">
                            <g:message code="de.iteratec.osm.ui.setupwizards.infra.address" default="address"></g:message>
                        </label>
                    </div>
                </div>

                %{-- input fields --}%
                <div class="col-sm-8">
                    <div class="form-group">
                        <g:textField name="inputServerName" class="form-control" id="inputServerName" value="${inputServerName}"
                                     placeholder="${g.message(code: 'de.iteratec.osm.ui.setupwizards.infra.enterServerName', default:'enterServerName')}">
                        </g:textField>
                    </div>
                    <div class="form-group">
                        <g:textField name="inputServerAddress" class="form-control" id="inputServerAddress" value="${inputServerAddress}"
                                     placeholder="${g.message(code: 'de.iteratec.osm.ui.setupwizards.infra.enterServerAddress', default:'enterServerAddress')}">
                        </g:textField>
                    </div>
                    <span class="help-block hidden" id="invalidAddress">
                        <font color="red"><g:message code="de.iteratec.osm.ui.setupwizards.infra.invalidAddress" default="invalidAddress"></g:message></font>
                    </span>
                </div>
            </div>
        </div>
    </div>

    </div>

    %{-- wpt key info box --}%
    <div class="col-sm-6" id="WPTKeyInfo">
        <div class="col-sm-offset-2 col-sm-10">
        <p></p>
        <div class="panel panel-info">
            <div class="panel-heading">
                <i class="fa fa-info-circle" aria-hidden="true"></i>
                <g:message code="default.info.title" default="Information"/>
            </div>

            <div class="panel-body">
                <g:message code="de.iteratec.osm.ui.setupwizards.infra.keyInfo" default="keyInfo"></g:message>
                <h1> </h1>
                <img class="infoImage thumbnail" id="KeyApplicationImg"
                     src="${resource(dir: 'images', file: 'WPT.png')}"
                     alt="Key Application"/>
            </div>
        </div>
        </div>
    </div>

    %{-- custom server info box --}%
    <div class="col-sm-6 hidden" id="CustomServerInfo">
        <div class="col-sm-offset-2 col-sm-10">
        <p></p>
        <div class="panel panel-info">
            <div class="panel-heading">
                <i class="fa fa-info-circle" aria-hidden="true"></i>
                <g:message code="default.info.title" default="Information"/>
            </div>

            <div class="panel-body">
                <g:message code="de.iteratec.osm.ui.setupwizards.infra.customInfo" default="customInfo"></g:message>
            </div>
        </div>
        </div>
    </div>
</div>

<div class="row navigationRow">
    <div class="form-group">
        <div class="col-sm-12 text-right">
            %{-- cancel button --}%
            <a  class="btn btn-default pull-left" href="http://localhost:8080/InfrastructureSetup/cancel" data-toggle="modal" id="cancelSetup">
                <i class="fa fa-times" aria-hidden="true"></i>
                <g:message code="script.versionControl.cancel.button" default="Cancel"/>
            </a>
            %{-- done button --}%
            <button class="btn btn-primary" type="submit" id="finishButton" >
                <g:message code="de.iteratec.osm.ui.setupwizards.infra.submit" default="submit"></g:message>
            </button>
        </div>
    </div>
</div>
