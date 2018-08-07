<div id="Content" class="content container-fluid">
    <g:if test="${entityName}">
        <h3>${entityName}</h3>

        <div class="card">
        <!-- print system messages (infos, warnings, etc) - not validation errors -->
            <g:if test="${flash.message && !layout_noflashmessage}">
                <div class="alert alert-info">${flash.message}</div>
            </g:if>

        <!-- Show page's content -->
            <g:layoutBody/>
            <g:pageProperty name="page.body"/>
        </div>
    </g:if>
    <g:else>
        <!-- print system messages (infos, warnings, etc) - not validation errors -->
        <g:if test="${flash.message && !layout_noflashmessage}">
            <div class="alert alert-info">${flash.message}</div>
        </g:if>

        <!-- Show page's content -->
        <g:layoutBody/>
        <g:pageProperty name="page.body"/>
    </g:else>
</div>
