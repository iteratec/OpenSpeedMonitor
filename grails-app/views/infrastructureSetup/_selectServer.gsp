<div class="row form-horizontal">
    %{-- select server type--}%
    <div class="col-sm-3">
        <p></p>
        <select class="form-control chosen-select" id="serverSelect">
            <option value="WPTServer">
                WebPagetest Server verwenden
            </option>
            <option value="CustomServer">
                Eigenen Server verwenden
            </option>
        </select>
        <div class="panel-body">
            Wählen Sie aus, ob Sie standardmäßig den WebPagetest Server verwenden, oder einen eigenen einrichten wollen.
        </div>
    </div>

    %{-- select key --}%
    <div class="col-sm-5" id="WPTKeyFields">
        <div class="panel-body">
            Beantragen Sie einen <a href="http://www.webpagetest.org/getkey.php"> Key für die WebPagetest-API</a> und geben Sie ihn hier ein:
        </div>

        <div class="col-sm-10">
            <div class="form-group" id="WPTKeyForm">
                %{-- label --}%
                <div class="col-sm-3">
                <label for="inputWPTKey" class="col-sm-2 control-label">
                    WPT-Key:
                </label>
                </div>

                %{-- input field --}%
                <div class="col-sm-8">
                    <div class="form-group">
                        <input type="text" class="form-control" id="inputWPTKey"
                           aria-describedby="jobGroupNameHelpBlock"
                           placeholder="API-Key hier eingeben">
                    </div>
                </div>
            </div>
        </div>
    </div>

    %{-- setup custom server --}%
    <div class="col-sm-5 hidden" id="CustomServerFields">
        <div class="panel-body">
            Geben Sie Addresse und Name Ihres Servers ein:
        </div>

        <div class="col-sm-10">
            <div class="form-group" id="CustomServerForm">
                %{-- label --}%
                <div class="col-sm-3">
                    <div class="form-group">
                        <label for="inputWPTKey" class="col-sm-2 control-label">
                            Name:
                        </label>
                    </div>
                <div class="form-group">
                        <label for="inputWPTKey" class="col-sm-2 control-label">
                            Addresse:
                        </label>
                    </div>
                </div>

                %{-- input field --}%
                <div class="col-sm-8">
                    <div class="form-group">
                        <input type="text" class="form-control" id="inputServerName"
                               aria-describedby="jobGroupNameHelpBlock"
                               placeholder="Servername hier eingeben">
                    </div>
                    <div class="form-group">
                        <input type="text" class="form-control" id="inputServerAddress"
                               aria-describedby="jobGroupNameHelpBlock"
                               placeholder="Serveraddresse hier eingeben">
                    </div>
                </div>
            </div>
        </div>
    </div>

    %{-- wpt key info box --}%
    <div class="col-sm-3" id="WPTKeyInfo">
        <p></p>
        <div class="panel panel-info">
            <div class="panel-heading">
                <i class="fa fa-info-circle" aria-hidden="true"></i>
                <g:message code="default.info.title" default="Information"/>
            </div>

            <div class="panel-body">
                Um einen Key zu erhalten füllen Sie einfach die benötigten Felder aus. Der Key wird benötigt um Messungen von den WebPagetest Servern laufen zu lassen. Wenn sie stattdessen unsere Server benutzen möchten, <a href="www.webpagetest.org/getkey.php">Setzen Sie sich mit uns in Verbindung</a>
                <h1> </h1>
                <img class="infoImage thumbnail" id="KeyApplicationImg"
                     src="${resource(dir: 'images', file: 'WPT.png')}"
                     alt="Key Application"/>
            </div>
        </div>
    </div>

    %{-- wpt key info box --}%
    <div class="col-sm-3 hidden" id="CustomServerInfo">
        <p></p>
        <div class="panel panel-info">
            <div class="panel-heading">
                <i class="fa fa-info-circle" aria-hidden="true"></i>
                <g:message code="default.info.title" default="Information"/>
            </div>

            <div class="panel-body">
                custom server info</a>
                <h1> </h1>
                <img class="infoImage thumbnail" id="CustomServerImg"
                     src="${resource(dir: 'images', file: 'WPT.png')}"
                     alt="Key Application"/>
            </div>
        </div>
    </div>
</div>

<div class="row navigationRow">
    <div class="form-group">
        <div class="col-sm-12 text-right">
            %{-- cancel button --}%
            <a class="btn btn-default pull-left" data-toggle="modal" data-target="#cancelSetup">
                <i class="fa fa-times" aria-hidden="true"></i>
                <g:message code="script.versionControl.cancel.button" default="Cancel"/>
            </a>
            %{-- done button --}%
            <button data-toggle="tab" href="#createScript" class="btn btn-primary" id="finishSetup" >
                Fertigstellen
                <i class="fa fa-caret-right" aria-hidden="true"></i>
            </button>
        </div>
    </div>
</div>
