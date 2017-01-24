<%--
A row with 3 cards to select the measured variables for first view, repeat view, and to filter them by min/max values
--%>
<div class="row">
    <div class="col-md-4">
        <g:render template="/_resultSelection/firstViewCard" model="[
                selectedAggrGroupValuesUnCached: selectedAggrGroupValuesUnCached,
                aggrGroupValuesUnCached        : aggrGroupValuesUnCached,
                selectedAggrGroupValuesUnCached: selectedAggrGroupValuesUnCached
        ]"/>
    </div>

    <div class="col-md-4">
        <g:render template="/_resultSelection/repeatedViewCard" model="[
                aggrGroupValuesCached        : aggrGroupValuesCached,
                selectedAggrGroupValuesCached: selectedAggrGroupValuesCached
        ]"/>
    </div>

    <div class="col-md-4">
        <g:render template="/_resultSelection/trimValuesCard" model="[
                trimBelowLoadTimes    : trimBelowLoadTimes,
                trimAboveLoadTimes    : trimAboveLoadTimes,
                trimBelowRequestCounts: trimBelowRequestCounts,
                trimAboveRequestCounts: trimAboveRequestCounts,
                trimBelowRequestSizes : trimBelowRequestSizes,
                trimAboveRequestSizes : trimAboveRequestSizes
        ]"/>
    </div>
</div>
