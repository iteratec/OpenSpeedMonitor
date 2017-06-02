<div id="spof" class="form-group">
    <label for="inputField-spof" class="col-md-3 control-label">
        <g:message code="job.spof.label" default="Single Point Of Failure"/>
    </label>
    <div class="col-md-7">
        <textarea class="form-control" name="spof" rows="3" id="inputField-spof">${job?.spof?.trim()}</textarea>
        <p style="margin-top: 5px"><g:message code="job.spof.info" default="Simulate failure of specified domains. This is done by re-routing all requests for the domains to blackhole.webpagetest.org which will silently drop all requests. "/></p>
    </div>
</div>
