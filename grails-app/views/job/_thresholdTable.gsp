<div id="list-threshold" class="content scaffold-list" role="main">
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message}</div>
    </g:if>
    <f:table collection="${thresholdList}" />

    <div>
        <bs:paginate total="${thresholdCount ?: 0}" />
    </div>
</div>