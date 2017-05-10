<div class="row form-horizontal">

    %{-- select server type--}%
    <div class="col-sm-6">
        <div class="col-sm-10">
            <div class="panel-body">
                <g:message code="de.iteratec.osm.ui.setupwizards.infra.selectServer" default="Select server type:"></g:message>
            </div>
        <g:select name = "serverSelect"
                  class="form-control chosen-select"
                  from="${["WPTServer","CustomServer"]}"
                  id="serverSelect"
                  value="${params.serverSelect}"
                  valueMessagePrefix="de.iteratec.osm.ui.setupwizards.infra">
        </g:select>
            <div style="line-height:100%;">
                <br>
            </div>
        </div>

    %{-- select key --}%
    <div class="col-sm-8" id="wptKeyFields">
        <div class="panel-body">
            <g:message code="de.iteratec.osm.ui.setupwizards.infra.keyPrompt" default="Input key below:"></g:message>
        </div>

        <div class="col-sm-12">
            <div class="form-group" id="wptKeyForm">
                %{-- label --}%
                <div class="col-sm-3">
                    <label for="inputWPTKey" class="col-sm-3 control-label">
                        <g:message code="de.iteratec.osm.ui.setupwizards.infra.WPTKey" default="WPT Key:"></g:message>
                    </label>
                </div>

                %{-- input field --}%
                <div class="col-sm-8">
                    <div class="form-group">
                        <g:textField name="inputWptKey" class="form-control" id="inputWptKey" value="${params.inputWptKey}"> </g:textField>
                    </div>
                </div>
            </div>
        </div>
    </div>

    %{-- setup custom server --}%
    <div class="col-sm-8 hidden" id="customServerFields">
        <div class="panel-body">
            <g:message code="de.iteratec.osm.ui.setupwizards.infra.customPrompt" default="customPrompt"></g:message>
        </div>

        <div class="col-sm-10">

                <form class="form-horizontal" id="customServerForm">
                    <div class="form-group">
                        <label for="inputServerName" class="col-sm-4 control-label">
                            <g:message code="de.iteratec.osm.ui.setupwizards.infra.name" default="name"></g:message>
                        </label>
                        <div class="col-sm-8">
                            <g:textField name="inputServerName" class="form-control" id="inputServerName" value="${params.inputServerName}"
                                placeholder="${g.message(code: 'de.iteratec.osm.ui.setupwizards.infra.enterServerName', default:'enterServerName')}">
                            </g:textField>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="inputServerAddress" class="col-sm-4 control-label">
                            <g:message code="de.iteratec.osm.ui.setupwizards.infra.address" default="address"></g:message>
                        </label>
                        <div class="col-sm-8">
                            <g:textField name="inputServerAddress" class="form-control" id="inputServerAddress" value="${params.inputServerAddress}"
                                placeholder="${g.message(code: 'de.iteratec.osm.ui.setupwizards.infra.enterServerAddress', default:'enterServerAddress')}">
                            </g:textField>
                        </div>
                    </div>
                    <div class="form-group" id="inputServerKeyForm">
                        <label for="inputServerAddress" class="col-sm-4 control-label">
                            <g:message code="de.iteratec.osm.ui.setupwizards.infra.key" default="key"></g:message>
                        </label>
                        <div class="col-sm-8">
                            <g:textField name="inputServerKey" class="form-control" id="inputServerKey" value="${params.inputServerKey}">
                            </g:textField>
                        </div>
                    </div>
                    <div class="form-group"  id="invalidAddressForm">
                        <label for="inputServerAddress" class="col-sm-4 control-label">

                        </label>
                        <div class="col-sm-8">
                            <span class="help-block hidden" id="invalidAddress">
                                <font color="red"><g:message code="de.iteratec.osm.ui.setupwizards.infra.invalidAddress" default="invalidAddress"></g:message></font>
                            </span>
                        </div>
                    </div>
                </form>
        </div>
    </div>

    </div>

    %{-- wpt key info box --}%
    <div class="col-sm-6" id="wptKeyInfo">
        <div class="col-sm-offset-2 col-sm-10">
        <div class="panel panel-info" id="wptKeyInfoPanel">
            <div class="panel-heading">
                <i class="fa fa-info-circle" aria-hidden="true"></i>
                <g:message code="default.info.title" default="Information"/>
            </div>

            <div class="panel-body">
                <g:message code="de.iteratec.osm.ui.setupwizards.infra.keyInfo" default="Apply for a key on this website"></g:message>
                <img class="infoImage thumbnail" id="keyApplicationImg"
                     src="${resource(dir: 'images', file: 'WPT.png')}"
                     alt="Key Application"/>
            </div>
        </div>
        </div>
    </div>

    %{-- custom server info box --}%
    <div class="col-sm-6 hidden" id="customServerInfo">
        <div class="col-sm-offset-2 col-sm-10">
        <div class="panel panel-info" id="customServerInfoPanel">
            <div class="panel-heading">
                <i class="fa fa-info-circle" aria-hidden="true"></i>
                <g:message code="default.info.title" default="Information"/>
            </div>

            <div class="panel-body">
                <g:message code="de.iteratec.osm.ui.setupwizards.infra.customInfo" default="Setup your own server"></g:message>
            </div>
        </div>
        </div>
    </div>
</div>

<div class="row navigationRow">
    <div class="form-group">
        <div class="col-sm-12 text-right">
            %{-- cancel button --}%
            <a  class="btn btn-default pull-left" href="/InfrastructureSetup/cancel" data-toggle="modal" id="cancelSetup">
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
