%{--Included in main layout kickstart_osm--}%
<div id="modal-p13n" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3 id="myModalLabel"><g:message code="de.iteratec.osm.p13n.cookiebased.label.short" default="Personalization"/></h3>
    </div>
    <div class="modal-body">
        %{--description--}%
        <p id="modal-p13n-message"></p>
        <p id="modal-p13n-warning"></p>
        %{--csi dashboard default chart title--}%
        <label for="input-default-csi-dashboard-title">
            <g:message code="de.iteratec.osm.p13n.cookiebased.default-csi-dashboard-title.label" default="Default Title CSI-Dashboard"/>:
        </label>
        <input type="text" name="defaultCsiDashboardTitle" id="input-default-csi-dashboard-title" class="input-xlarge"
               placeholder="${g.message([code:'de.iteratec.isocsi.csi.defaultdashboard.chart.title', default: 'Customer Satisfaction Index (CSI)'])}">
        %{--charting library to use--}%
        <label for="select-charting-libraries">
            <g:message code="de.iteratec.osm.p13n.cookiebased.select-charting-libraries.label" default="Charting library to use"/>:
        </label>
        <g:select from="${grailsApplication.config.grails.de.iteratec.osm.report.chart.availableChartTagLibs}" id="select-charting-libraries" name="selectedChartingLibraries"></g:select>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">
            <g:message code="default.button.cancel.label" default="Cancel"/>
        </button>
        <button id="modal-p13n-save-btn" class="btn" data-dismiss="modal" aria-hidden="true">
            <g:message code="default.button.save.label" default="Save"/>
        </button>
    </div>
</div>
<asset:script type="text/javascript">
    $(document).ready(function(){

        var cookieKeyCsiDashboardTitle = '${de.iteratec.osm.util.Constants.COOKIE_KEY_CSI_DASHBOARD_TITLE}';
        var cookieKeyChartingLibToUse = '${de.iteratec.osm.util.Constants.COOKIE_KEY_CHARTING_LIB_TO_USE}';

        //defining handlers
        $('#modal-p13n').bind("shown", function(){
            //p13n message
            $('#modal-p13n-message').text('${g.message(code: 'de.iteratec.osm.p13n.cookiebased.description')}');
            if (!navigator.cookieEnabled){
                $('#modal-p13n-warning').append('<div class="alert alert-error">'+'${g.message(code: 'de.iteratec.osm.p13n.cookiebased.warning')}'+'</div>');
            }
            //csi dashboard default chart title
            $('#input-default-csi-dashboard-title').val(getCookie(cookieKeyCsiDashboardTitle));
            //charting lib to use
            $('#select-charting-libraries').val(getCookie(cookieKeyChartingLibToUse));
        });
        $('#modal-p13n-save-btn').bind("click", function(){
            setCookie(cookieKeyCsiDashboardTitle, $('#input-default-csi-dashboard-title').val(), '/', (365*24*60*60*1000));
            setCookie(cookieKeyChartingLibToUse, $('#select-charting-libraries').val(), '/', (365*24*60*60*1000));
        });

    });
</asset:script>
<asset:deferredScripts/>