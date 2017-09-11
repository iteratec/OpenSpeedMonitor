<div id="list-threshold" class="content scaffold-list" role="main">
    <table>
        <f:table collection="${thresholds}" />

        <div>
            <bs:paginate total="${thresholds ?: 0}" />
        </div>
    </table>
</div>