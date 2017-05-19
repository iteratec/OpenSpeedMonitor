<%@ page contentType="text/html;charset=UTF-8"%>
<html>
    <head>
        <meta name="layout" content="kickstart_osm" />
        <title><g:message code="queue.status.label" /></title>
        <asset:stylesheet src="queueStatus/list.css"/>
    </head>
    <body>
        <h1>
            <g:message code="queue.status.label"/>
        </h1>
        <div class="alert alert-warning" id="serverdown">
          <a class="close" data-dismiss="alert">×</a>
          <g:message code="queue.noconnection.label"/>
        </div>
        <div id="allQueues">
            <g:render template="allQueues" bean="${servers}" />
        </div>
        <content tag="include.bottom">
            <asset:javascript src="timeago/jquery.timeago.js"/>
            <g:if test="${org.springframework.web.servlet.support.RequestContextUtils.getLocale(request).language.equals('de')}">
                <asset:javascript src="timeago/timeagoDe.js"/>
            </g:if>

            <asset:script type="text/javascript">
                $(document).ready(function() {
                    $('#serverdown').hide();
                    $('abbr.timeago').timeago();

                    $(document).on('click', 'a.jobDetail', function (e) {
                        var status = $(this).attr('data-status');
                        var thisRow = $(this).parent().parent();
                        var nextRow = thisRow.next();
                        if ($(nextRow).hasClass('jobsRow')) {
                            var jobsRowVisible = $(nextRow).is(":visible");
                            if (!jobsRowVisible || $(nextRow).attr('status') != status) {
                                $.each($('tbody tr', nextRow), function() {
                                    $(this).parent().parent().toggle($(this).attr('data-statuscode') == status);
                                    $(this).parent().parent().prev().toggle($(this).attr('data-statuscode') == status);
                                });
                                $(nextRow).attr('status', status);

                                $('.arrow').toggleClass('fa-chevron-down', false);
                                $('.arrow').toggleClass('fa-chevron-up', true);

                                $(nextRow).toggle(true);
                            } else {
                                // hide row
                                $('.arrow', this).toggleClass('fa-chevron-down', true);
                                $('.arrow', this).toggleClass('fa-chevron-up', false);
                                $(nextRow).toggle(false);
                            }
                        }
                    });
                    $('tr.jobsRow').hide();
                });
            </asset:script>
        </content>
    </body>
</html>
