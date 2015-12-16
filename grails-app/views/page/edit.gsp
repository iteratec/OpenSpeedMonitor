<%@ page import="de.iteratec.osm.csi.Page" %>
<!doctype html>
<html>

    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="kickstart" />
        <g:set var="entityName" value="${message(code: 'page.label', default: 'Page')}" />
        <title><g:message code="default.edit.label" args="[entityName]" /></title>
        <style>
        %{--Styles for multi line chart--}%
            .axis path,
            .axis line {
                fill: none;
                stroke: black;
                shape-rendering: crisp-edges ;
            }
            .line {
                fill: none;
                stroke-width: 2px;
            }
            .verticalLine,
            .horizontalLine {
                opacity: 0.3;
                stroke-dasharray: 3,3;
                stroke: blue;
            }
            .xTextContainer,
            .tooltipTextContainer{
                opacity: 0.5;
            }
        </style>
    </head>

    <body>

        <section id="edit-page" class="first">

            <g:hasErrors bean="${pageInstance}">
            <div class="alert alert-error">
                <g:renderErrors bean="${pageInstance}" as="list" />
            </div>
            </g:hasErrors>

            <g:form method="post" class="form-horizontal" >
                <g:hiddenField name="id" value="${pageInstance?.id}" />
                <g:hiddenField name="version" value="${pageInstance?.version}" />
                <fieldset class="form">
                    <g:render template="form"/>
                </fieldset>
                <div class="form-actions">
                    <g:actionSubmit class="btn btn-primary" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
                    <g:render template="/_common/modals/deleteSymbolLink"/>
                    <g:render template="/_common/modals/chooseCsiMapping" />
                    <button class="btn" type="reset"><g:message code="default.button.reset.label" default="Reset" /></button>
                </div>
            </g:form>

        </section>

        <content tag="include.bottom">
            <asset:script type="text/javascript">

                function startSpinner(spinnerElement){
                    var opts = {
                        lines: 15, // The number of lines to draw
                        length: 20, // The length of each line
                        width: 10, // The line thickness
                        radius: 30, // The radius of the inner circle
                        corners: 1, // Corner roundness (0..1)
                        rotate: 0, // The rotation offset
                        direction: 1, // 1: clockwise, -1: counterclockwise
                        color: '#000', // #rgb or #rrggbb or array of colors
                        speed: 1, // Rounds per second
                        trail: 60, // Afterglow percentage
                        shadow: true, // Whether to render a shadow
                        hwaccel: false, // Whether to use hardware acceleration
                        className: 'spinner', // The CSS class to assign to the spinner
                        zIndex: 2e9, // The z-index (defaults to 2000000000)
                        top: '50%', // Top position relative to parent in px
                        left: '50%' // Left position relative to parent in px
                    };
                    return new Spinner(opts).spin(spinnerElement);
                }

                function copyDefaultMappingToPageAsynchronously(){

                    var  spinner = startSpinner(document.getElementById('spinner-position'));

                    var selectedMapping = document.querySelector('select[name=selectedDefaultMapping]').value;
                    $.ajax({
                        type: 'POST',
                        url: '${createLink(controller: 'page', action: 'applyMappingToPage', absolute: true)}',
                    data: {page: '${pageInstance}', selectedDefaultMapping: selectedMapping},
                    success : function(data) {
                        spinner.stop();
                        //TODO: updating underlying chart doesn't word for now :(
                        //console.log(data)
                        //graphBuilder_choose_default_csi.setData(data);
                        $('#CsiMappingModal').modal('hide');
                    },
                    error: function(result) {
                        spinner.stop();
                        $('#CsiMappingModal').modal('hide');
                        return false;
                    }
                });
            }

            </asset:script>
        </content>

    </body>

</html>
