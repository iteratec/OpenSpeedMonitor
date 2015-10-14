<asset:stylesheet src="d3Charts/treemap.css"/>
<asset:javascript src="d3/treemap.js"/>
%{--Data to inject:
                    data : de.iteratec.osm.d3data.TreemapData as JSON
                    design : one of 'browser', 'rect'
                    id : the id of the barChart on this page (unique)--}%

<div class="row">
    <div class="span11" id="treemapSpan">
        <div class="treemap" id= ${id}></div>


        <div id="tooltip" class="hidden">
            <p><strong id="heading"></strong></p>

            <p><span id="info"></span></p>
        </div>
    </div>
    <div class="span1" id="zeroWeightSpan">
    </div>
</div>
<asset:script type="text/javascript">
    $(document).ready (createTreemap(1200, 750, ${data}, "${design}","${id}"));
</asset:script>
