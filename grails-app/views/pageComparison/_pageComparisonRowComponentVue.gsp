<script type="text/x-template" id="page-comparison-vue">
<div id="measurandSeries-clone" class="row form-group addPageComparisonRow">
    <label class="col-sm-2 control-label">
        <g:message code="de.iteratec.osm.pageComparison.firstPageTitle"
                   default="First Page:"/>
    </label>
    <div class="col-sm-3">
        <select name="firstJobGroupSelect" v-model="comparisondata.jobGroupId1"
                class="form-control jobgroupselect">
            <option disabled value="-1">- choose a job group -</option>
            <option v-for="jobGroup in jobgroups" :value="jobGroup.id">
                {{jobGroup.name}}
            </option>
        </select>
        <select name="firstPageSelect" v-model="comparisondata.pageId1" class="form-control pageSelect">
            <option disabled value="-1">- choose a page -</option>
            <option v-for="page in getPages(comparisondata.jobGroupId1)" :value="page.id">
                {{page.name}}
            </option>
        </select>
    </div>
    <label class="col-sm-2 control-label">
        <g:message code="de.iteratec.osm.pageComparison.secondPageTitle"
                   default="Second Page:"/>
    </label>

    <div class="col-sm-3">
        <select name="secondJobGroupSelect" v-model="comparisondata.jobGroupId2"
                class="form-control jobgroupselect">
            <option disabled value="-1">- choose a job group -</option>
            <option v-for="jobGroup in jobgroups" :value="jobGroup.id">
                {{jobGroup.name}}
            </option>
        </select>
        <select name="secondPageSelect" v-model="comparisondata.pageId2" class="form-control pageSelect">
            <option disabled value="-1">- choose a page -</option>
            <option v-for="page in getPages(comparisondata.jobGroupId2)" :value="page.id">
                {{page.name}}
            </option>
        </select>

    </div>
    <div v-bind:class="[isOnlyRow()? 'hidden':'', 'col-sm-2']">
        <a href="#" role="button" v-bind:id="'removeComparisonRow'+index"   >
            <i class="fa fa-times" aria-hidden="true"></i>
        </a>
    </div>

</div>
</script>
