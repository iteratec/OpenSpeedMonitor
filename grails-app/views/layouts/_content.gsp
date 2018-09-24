<%@ defaultCodec="none" %>
<div id="Content" class="content container-fluid">

    <h3>${entityName}</h3>
<!-- print system messages (infos, warnings, etc) - not validation errors -->
    <g:if test="${flash.message && !layout_noflashmessage}">
        <div class="alert alert-info">${flash.message}</div>
    </g:if>
    <div class="card">
    <!-- Secondary menu in one row (e.g., actions for current controller) -->
        <g:if test="${!layout_nosecondarymenu}">
            <div class="row">
                <div class="col-md-12">
                    <g:render template="/_menu/submenubar"/>
                </div>
            </div>
        </g:if>


    <!-- Show page's content -->
        <g:layoutBody/>
        <g:pageProperty name="page.body"/>
    </div>
</div>
