<g:if test="${job.id != null}">
    <div class="row">
        <div class="col-md-10">
            <app-job-threshold data-job-id="${job?.id}"
                               data-job-scriptId="${job?.script?.id}"
                               data-module-path="src/app/job-threshold/job-threshold.module#ThresholdModule"></app-job-threshold>
        </div>
    </div>
</g:if>

