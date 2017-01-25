<sec:ifLoggedIn>
    <sec:ifAnyGranted roles="ROLE_ADMIN, ROLE_SUPER_ADMIN">
        <a href="javascript:JobStatusUpdater.cancelJobRun(${jobId}, '${testId}');">(${message(code: 'de.iteratec.isj.job.cancel')})</a>
    </sec:ifAnyGranted>
</sec:ifLoggedIn>
