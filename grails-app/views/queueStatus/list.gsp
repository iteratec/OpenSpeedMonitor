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
        <!-- <input type="checkbox" id="autoRefresh" /> <label for="autoRefresh"><g:message code="queue.autorefresh.label"/></label>  -->

        <div id="allQueues">
            <g:render template="allQueues" bean="${servers}" />
        </div>
        <content tag="include.bottom">
            <asset:javascript src="timeago/jquery.timeago.js"/>
            <g:if test="${org.springframework.web.servlet.support.RequestContextUtils.getLocale(request).language.equals('de')}">
                <asset:javascript src="timeago/timeagoDe.js"/>
            </g:if>

            <asset:script type="text/javascript">
                /*
                function refreshQueues() {
                        var rowVisible = []
                        jQuery.ajax({
                            type : 'POST',
                            url : '${createLink(action: 'refresh')}',
                        success: function(template) {
                            $('#serverdown').hide();
                            $('.datarows > tr.jobsRow').map(function (index, tr) { rowVisible[$(tr).attr('data-queue')] = $(tr).is(":visible"); });
                            $('#allQueues').html(template);
                            $('.datarows > tr.jobsRow').map(function (index, tr) { $(tr).toggle(rowVisible[$(tr).attr('data-queue')]); });
                            $('abbr.timeago').timeago();
                        },
                        error: function() {
                            $('#serverdown').show();
                        }
                    });
                }

                var timer = null;
                */

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

                                $('.arrow').toggleClass('glyphicon-chevron-down', false);
                                $('.arrow').toggleClass('glyphicon-chevron-up', true);

                                $('.arrow', this).toggleClass('glyphicon-chevron-down', true);
                                $('.arrow', this).toggleClass('glyphicon-chevron-up', false);
                                $(nextRow).toggle(true);
                            } else {
                                // hide row
                                $('.arrow', this).toggleClass('glyphicon-chevron-down', false);
                                $('.arrow', this).toggleClass('glyphicon-chevron-up', true);
                                $(nextRow).toggle(false);
                            }
                        }
                        //return false;
                    });
                    /*
                    $(document).on('click', 'tr.queueRow', function () {
                        var thisRow = this;
                        if ($(this).next().hasClass('jobsRow')) {
                            $('.arrow', thisRow).removeClass('glyphicon-chevron-down');
                            $('.arrow', thisRow).addClass('glyphicon-chevron-up');
                            $(this).next().hide();
                        }
                    });
                    */
                    $('tr.jobsRow').hide();

                    /*
                    $('#autoRefresh').click(function () {
                        if ($(this).prop('checked')) {
                            refreshQueues();
                            timer = setInterval(refreshQueues, 5000);
                        } else {
                            clearInterval(timer);
                            $('tr.jobsRow').hide();
                            $('#serverdown').hide();
                        }
                    });
                    */
                });
            </asset:script>
        </content>
    </body>
</html>
