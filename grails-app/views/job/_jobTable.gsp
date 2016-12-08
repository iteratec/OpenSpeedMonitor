<%@ defaultCodec="none" %>
<g:set var="jobService" bean="jobService"/>
<tbody>
	<g:each var="job" in="${jobs}">
		<g:set var="tags" value="${jobsWithTags.findAll { it.jobId == job.id }.collect { it.tag }}" />
		<tr 
			class="${ massExecutionResults ? (massExecutionResults[job.id] ? 'highlight ' + massExecutionResults[job.id].status : '' ) : '' }"
			<g:if test="${tags}"> data-tags="${tags.join(',')}" </g:if>>
			<td><g:checkBox name="selected.${job.id}" value="on"
					checked="${ selectedIds?.contains(job.id) }"
					class="jobCheckbox" /></td>
			<td>
				<div class="show-chart-buttons">
				<g:if test="${job.lastRun}" >
					<a href="${jobService.createResultLinkForJob(job)}" class="show-chart"><i class="fa fa-line-chart"></i></a><br/>
					<a href="${jobService.createPageAggregationLinkForJob(job)}" class="show-chart"><i class="fa fa-bar-chart"></i></a>
				</g:if>
				</div>
				<strong><a
					href="${createLink(action: 'edit', id: job.id, absolute: true)}" class="jobName ${job.active==false?'inactiveJob':''}"> ${job.label}
				</a></strong>
				<g:if test="${tags}">
					<br/>
					<ul class="tags">
						<g:each in="${tags}">
							<li>${it}</li>
						</g:each>
					</ul>
				</g:if>
				<g:if test="${job.script}">
					<br />
					<a
						href="${createLink(controller: 'script', action: 'edit', id: job.script.id, absolute: true)}" class="skript">
						${job.script.label}
					</a>
					<span title="${message(code: 'script.measuredEventsCount.label')}">(${job.script.measuredEventsCount})</span>
				</g:if>
				<span class="status" id="runningstatus-${job.id}"> </span> 
				<span class="status"> ${ massExecutionResults ? (massExecutionResults[job.id]?.message ? "<br />" + massExecutionResults[job.id].message : '' ) : '' }</span>
			</td>
			<td class="jobgroup">${job.jobGroup.name}</td>
            <td class="location">${job.location.removeBrowser(job.location.uniqueIdentifierForServer ?: job.location.location)}</td>
            <td class="browser">${job.location.browser.name != de.iteratec.osm.measurement.environment.Browser.UNDEFINED ? job.location.browser.name : ''}</td>
            <td>
                <g:render template="timeago"
                          model="${['date': job.lastRun, 'defaultmessage': message(code: 'job.lastRun.label.never', default: 'Noch nie'),
                                    'url': createLink(controller: 'tabularResultPresentation', action: 'listResultsForJob', params: ['job.id': job.id], absolute: true)]}" />
                <br>
                <a href="#" data-toggle="popover" title="${g.message(code: 'de.iteratec.osm.job.status.description.title', default: 'State') + ': ' + job.label}"
                   data-placement="bottom" data-trigger="hover" data-html="true" data-content="${render(template: "jobStatusBarHoverInfo")}">
                    <g:render template="jobStatusBar"
                              model="${[
                                      'status5CssClass': (job.jobStatistic == null ? 'job-status-nodata' : job.jobStatistic.getJobStatusLast5CssClass()),
                                      'status25CssClass': (job.jobStatistic == null ? 'job-status-nodata' : job.jobStatistic.getJobStatusLast25CssClass()),
                                      'status150CssClass': (job.jobStatistic == null ? 'job-status-nodata' : job.jobStatistic.getJobStatusLast150CssClass())]}"/>
                </a>
            </td>
			<td>
				<g:if test="${job.active}">
					<g:hiddenField name="job_active" class="job_active" value="true" />
					<g:render template="timeago"
						model="['cronstring': job.executionSchedule]" />
				</g:if>
				<g:else><g:hiddenField name="job_active" class="job_active" value="false"/></g:else>
			</td>
			<td>
				<g:render template="cronExpression" bean="${job.executionSchedule}" />
			</td>
			<td>
				${job.runs}
			</td>
			<td>
				${ job.firstViewOnly ? '' : message(code: 'job.2x.label') }
			</td>
		</tr>
	</g:each>
</tbody>