<html>

<head>
<title>Manual of REST API</title>
<meta name="layout" content="kickstart_osm" />
</head>
<body>
%{--<body data-spy="scroll" data-target="#spied-nav">--}%
	<div class="container">

		<div class="row">

			<div class="span3" id="spied-nav">
				<ul class="nav nav-pills nav-stacked affix">
					<li><a href="#rest_base_path">REST API OpenSpeedMonitor</a></li>
					<li><a href="#resultsbetween">&raquo;&nbsp;<strong>GET</strong>&nbsp;Results&nbsp;between</a></li>
					<li><a href="#csi">&raquo;&nbsp;<strong>GET</strong>&nbsp;CSI</a></li>
					<li><a href="#translateToCustomerSatisfaction">&raquo;&nbsp;<strong>GET</strong>&nbsp;Translate to Customer Satisfaction</a></li>
					<li><a href="#csi-frustrations">&raquo;&nbsp;<strong>GET</strong>&nbsp;CSI frustrations</a></li>
					<li><a href="#get-result-urls">&raquo;&nbsp;<strong>GET</strong>&nbsp;Result URL's of Job</a></li>
					<li><a href="#job-activation">&raquo;&nbsp;<strong>PUT</strong>&nbsp;Job activation</a></li>
					<li><a href="#job-deactivation">&raquo;&nbsp;<strong>PUT</strong>&nbsp;Job deactivation</a></li>
					<li><a href="#job-set-schedule">&raquo;&nbsp;<strong>PUT</strong>&nbsp;Set execution schedule</a></li>
				</ul>
			</div>
			<div class="span9 content">

				<h1 id="rest_base_path">Manual of REST API</h1>
				<p>(English version only)</p>
				<p>Any request URL of the REST-API of the openSpeedMonitor starts with the application path followed by the keyword <code>/rest</code>.
				<br>In the following manual we call this <strong>REST-base-path</strong>.
				<br>All successfully handled requests return their result in JSON notation. Default output format is not pretty-printed (small in size).
				A pretty-printed output can be forced with an URL query argument <code>pretty=true</code>.</p>

				<hr>

				<div id="resultsbetween">

					<h2>GET Method:&nbsp;<span class="text-info">Results between</span></h2>

					<h3>Request signature</h3>
					<p><code><abbr title="[application path]/rest">[REST-base-path]</abbr>/[system]/resultsbetween/[timestampFrom]/[timestampTo]</code></p>
					<p>
						The request URL starts with the <a href="#rest_base_path">REST-base-path</a>
						followed by the system name (remember special chars like whitespace need to be URL escaped which was left out here for readability),
						followed by the methods name <em>/resultsbetween</em>,
						followed by the requested time-frame in the ISO 8601 format given as <em>/start/end</em>
					</p>
					<p>
						Example URI for the system &quot;live&quot; and the time-frame 1st January 2014, 0 AM to the 1st January 2014, 11 PM (UTC):
						<code><abbr title="[application path]/rest">[REST-base-path]</abbr>/live/resultsbetween/20140101T000000Z/20140101T230000Z</code>
					</p>
					<p>
					   You can obtain a list of all systems by sending a request to <code><abbr title="[application path]/rest">[REST-base-path]</abbr>/allSystems</code>.
					   As result a JSON list of system names is returned.
					</p>
					<h4>Additional parameters</h4>
					<p>
					   To make a more concrete request, you may add one or more of the following arguements as URL query arguments.
					</p>
					   <dl>
					   <dt>page</dt>
						<dd>
							The name of a page. If specified on results belonging to this page are returned.
							<br/>
							You can obtain a list of all pages by sending a request to <code><abbr title="[application path]/rest">[REST-base-path]</abbr>/allPages</code>.
							As result a JSON list of page names is returned.
						</dd>
						<dt>step</dt>
						<dd>
							The name of a (measured) step. If specified on results belonging to this step are returned.
							<br/>
							You can obtain a list of all steps by sending a request to <code><abbr title="[application path]/rest">[REST-base-path]</abbr>/allSteps</code>.
							As result a JSON list of step names is returned.
						</dd>
						<dt>browser</dt>
						<dd>
							The name of a browser. If specified on results belonging to this browser are returned.
							<br/>
							You can obtain a list of all browsers by sending a request to <code><abbr title="[application path]/rest">[REST-base-path]</abbr>/allBrowsers</code>.
							As result a JSON list of browser names is returned.
						</dd>
						<dt>location</dt>
						<dd>
							The location-address of a location. If specified on results belonging to this location are returned.
							<br/>
							You can obtain a list of all location-addresses by sending a request to <code><abbr title="[application path]/rest">[REST-base-path]</abbr>/allLocations</code>.
							As result a JSON list of location-addresses is returned.
						</dd>
					   </dl>

					<p>
					An example with all parameters:<br>
					<code><abbr title="[application path]/rest">[REST-base-path]</abbr>/live/resultsbetween/20140101T000000Z/20140101T230000Z?
						<br>browser=IE&page=WK&location=Hetzner01-IE&step=IE8_BV1_Step06_Warenkorb - hetzner</code>
					</p>

					<h3>Potential outcomes of a request</h3>
					<dl>
						<dt>HTTP status 200 OK</dt>
						<dd>
							The request handled successfully, a result in JSON notation is
							returned. It contains a list of results matching the given request-parameters.<br><br>
							Response example:
							<pre>
	[{
		"executionTime": "2014-07-02T00:01:12Z",
		"csiValue": "0,93",
		"docCompleteTimeInMillisecs": 2448,
		"numberOfWptRun": 1,
		"cachedView": "UNCACHED",
		"page": "HP",
		"step": "Homepage",
		"browser": "Firefox",
		"location": "prod-rz1:Firefox",
		"detailUrl": "http://wptserver-url.com/result/test-id",
		"httpArchiveUrl": "http://wptserver-url.com/export.php?test=test-id"
	},
	{
		"executionTime": "2014-07-02T00:01:12Z",
		"csiValue": "1,00",
		"docCompleteTimeInMillisecs": 1310,
		"numberOfWptRun": 1,
		"cachedView": "CACHED",
		"page": "HP",
		"step": "Homepage",
		"browser": "Firefox",
		"location": "prod-rz2:Firefox",
		"detailUrl": "http://wptserver-url.com/result/test-id",
		"httpArchiveUrl": "http://wptserver-url.com/export.php?test=test-id"
	}]</pre>
							<br />
							The response is of type application/json (encoding UTF-8) as described in <a href="http://tools.ietf.org/html/rfc4627">RFC4627</a>.<br>
						</dd>
						<dt>HTTP status 400 Bad Request</dt>
						<dd>The end of the requested time frame is before the start of
							it. For sure, this is invalid. The end of the time-frame need to be
							after its start. An error message with details is attached
							as response.
							<br />
							The response is of type text/plain (encoding UTF-8).
						</dd>
						<dt>HTTP status 413 Request Entity Too Large</dt>
						<dd>The requested time-frames duration in days is wider than 2
							days. An error message with details is attached as response.
							<br />
							The response is of type text/plain (encoding UTF-8).</dd>
						<dt>HTTP status 404 Not Found</dt>
						<dd>If at least one of the requested elements was not found. If
							no further parameters specified, this need to be the specified system
							otherwise it could be any of them. An error message with
							details is attached as response.
							<br />
							The response is of type text/plain (encoding UTF-8).</dd>
					</dl>

					<hr>

				</div>

				<div id="csi">

					<h2>GET Method:&nbsp;<span class="text-info">CSI</span></h2>

					<h3>Request signature</h3>
					<p><code><abbr title="[application path]/rest">[REST-base-path]</abbr>/[system]/csi/[timestampFrom]/[timestampTo]</code></p>
					<p>
						The request URL starts with the <a href="#rest_base_path">REST-base-path</a>
						followed by the system name (remember special chars like whitespace need to be URL escaped which was left out here for readability),
						followed by the methods name <em>/csi</em>,
						followed by the requested time-frame in the ISO 8601 format given as <em>/start/end</em>
					</p>
					<p>
						Example URI for the system &quot;live&quot; and the time-frame 1st January 2014, 0 AM to the 1st January 2014, 11 PM (UTC):
						<code><abbr title="[application path]/rest">[REST-base-path]</abbr>/live/csi/20140101T000000Z/20140101T230000Z</code>
					</p>
					<p>
					   You can obtain a list of all systems by sending a request to <code><abbr title="[application path]/rest">[REST-base-path]</abbr>/allSystems</code>.
					   As result a JSON list of system names is returned.
					</p>

					<h3>Potential outcomes of a request</h3>
					<dl>
						<dt>HTTP status 200 OK</dt>
						<dd>
							The request handled successfully, a result in JSON notation is
							returned. It contains the over-all customer satisfaction index for the requested system and period. <br><br>
							Response example:
							<pre>
	{
		"csiValueAsPercentage":90.5265687342499,
		"targetCsiAsPercentage":90,
		"delta":0.5265687342499064,
		"countOfMeasurings":174
	}</pre>
							<br />
							The response is of type application/json (encoding UTF-8) as described in <a href="http://tools.ietf.org/html/rfc4627">RFC4627</a>.
						</dd>
						<dt>HTTP status 400 Bad Request</dt>
						<dd>The end of the requested time frame is before the start of
							it. For sure, this is invalid. The end of the time-frame need to be
							after its start. An error message with details is attached
							as response.
							<br />
							The response is of type text/plain (encoding UTF-8).
						</dd>
						<dt>HTTP status 413 Request Entity Too Large</dt>
						<dd>The requested time-frames duration in days is wider than 8
							days. An error message with details is attached as response.
							<br />
							The response is of type text/plain (encoding UTF-8).</dd>
						<dt>HTTP status 404 Not Found</dt>
						<dd>If the specified system was not found. An error message with
							details is attached as response.
							<br />
							The response is of type text/plain (encoding UTF-8).</dd>
					</dl>

					<hr>

				</div>

				<div id="translateToCustomerSatisfaction">

					<h2>GET Method:&nbsp;<span class="text-info">Translate to Customer Satisfaction</span></h2>

					<h3>Request signature</h3>
					<p><code><abbr title="[application path]/rest">[REST-base-path]</abbr>/csi/translateToCustomerSatisfaction</code></p>
					<p>
						The request URL consists of <a href="#rest_base_path">REST-base-path</a> the word <em>csi</em> and the name of the method <em>translateToCustomerSatisfaction</em>.
					</p>
					<h4>Parameters</h4>
					   <dl>
						<dt>pageName</dt>
						   <dd>
							   The name of the page the doc complete time was measured for.
						   </dd>
						   <dt>docCompleteTimeInMillisecs</dt>
						   <dd>
							   Doc complete time to translate to customer satisfaction.
						   </dd>
						  </dl>

					<h3>Potential outcomes of a request</h3>
					<dl>
						<dt>HTTP status 200 OK</dt>
						<dd>
							The request handled successfully, a result in JSON notation is
							returned. It contains the calculated customer satisfaction for the given doc complete time. <br><br>
							Response example:
							<pre>
	{"target":
		{
			"docCompleteTimeInMillisecs":3500,
			"customerSatisfactionInPercent":0.75837
		}
	}</pre>
							<br />
							The response is of type application/json (encoding UTF-8) as described in <a href="http://tools.ietf.org/html/rfc4627">RFC4627</a>.
						</dd>
						<dt>HTTP status 400 Bad Request</dt>
						<dd>One of the query arguments is missing or no page could be found for given pageName. An error message with details is attached as response.
							<br />
							The response is of type text/plain (encoding UTF-8).
						</dd>
					</dl>

					<hr>

				</div>

				<div id="csi-frustrations">

					<h2>GET Method:&nbsp;<span class="text-info">CSI frustrations</span></h2>

					<h3>Request signature</h3>
					<p><code><abbr title="[application path]/rest">[REST-base-path]</abbr>/csi/frustrations</code></p>
					<p>
						The request URL consists of <a href="#rest_base_path">REST-base-path</a> the word <em>csi</em> and the name of the method <em>frustrations</em>.
					</p>
					<h4>Parameters</h4>
					   <dl>
						<dt>pageName</dt>
						   <dd>
							   The name of the page the customer frustration load times should be delivered for.
						   </dd>
						  </dl>

					<h3>Potential outcomes of a request</h3>
					<dl>
						<dt>HTTP status 200 OK</dt>
						<dd>
							The request handled successfully, a result in JSON notation is returned.
							It contains the complete list of customer frustration load times for page with given pageName. <br><br>
							Response example:
							<pre>
	{"target":
		{
			"page":"MES",
			"cachedFrustrations":[966,1076,1186,1220,1446,1457,1582,1605,1607,...],
			"count":212
		}
	}</pre>
							<br />
							The response is of type application/json (encoding UTF-8) as described in <a href="http://tools.ietf.org/html/rfc4627">RFC4627</a>.
						</dd>
						<dt>HTTP status 400 Bad Request</dt>
						<dd>Query argument pageName is missing or no page could be found for given pageName. An error message with details is attached as response.
							<br />
							The response is of type text/plain (encoding UTF-8).
						</dd>
					</dl>

					<hr>

				</div>

				<div id="get-result-urls">

					<h2>GET Method:&nbsp;<span class="text-info">Result URL's of job</span></h2>

					<h3>Request signature</h3>
					<p><code><abbr title="[application path]/rest">[REST-base-path]</abbr>/job/[job-id]/resultUrls/[timestampFrom]/[timestampTo]</code></p>
					<p>
						The request URL starts with the <a href="#rest_base_path">REST-base-path</a>
						followed by <em>job/</em>, the ID of the measurement job to get result url's for, method name <em>/resultUrls</em>
						and the two timestamps for start and end of the period of interest. Timestamps have to match ISO 8601 format.
					</p>
					<p>
						Example URI to get URL's to visualize results of measurement job with ID 42 from 23rd of july 2014, 09:00 AM to
						24th of july 2014, 02:23 AM:
						<code><abbr title="[application path]/rest">[REST-base-path]</abbr>/job/42/resultUrls/20140723T090000Z/20140724T022300Z</code>
					</p>

					<h3>Potential outcomes of a request</h3>
					<dl>
						<dt>HTTP status 200 OK</dt>
						<dd>
							The request handled successfully, a result in JSON notation is returned. It contains three URL's of OpenSpeedMonitor
							that visualize results of requested measurement job for period between the two given timestamps.<br><br>
							Response example:
							<pre>
	{
		"job": "my-measurement-job",
		"from": "2014-11-01T01:00:00+01:00",
		"to": "2014-11-01T03:00:00+01:00",
		"results-chart-url":"[URL to result dashboard of OpenSpeedMonitor]",
		"csi-chart-url":"[URL to csi dashboard of OpenSpeedMonitor]",
		"results-tabular-url": "[URL to tabular view in OpenSpeedMonitor]"
	}</pre>
							<br />
							The response is of type application/json (encoding UTF-8) as described in <a href="http://tools.ietf.org/html/rfc4627">RFC4627</a>.
						</dd>
						<dt>HTTP status 400 Bad Request</dt>
						<dd>The end of the requested time frame is before the start of it. For sure, this is invalid. The end of the time-frame need to be
						after its start. An error message with details is attached as response.
							<br />
							The response is of type text/plain (encoding UTF-8).
						</dd>
						<dt>HTTP status 404 Not found</dt>
						<dd>No job with requested job ID can be found.<br />
							The response is of type text/plain (encoding UTF-8).</dd>
					</dl>

					<hr>

				</div>

				<div id="job-activation">

					<h2>PUT Method:&nbsp;<span class="text-info">Job activation</span></h2>

					<i class="icon-info-sign">&nbsp;${protectedFunctionHint}</i>
					<h3>Request signature</h3>
					<p><code><abbr title="[application path]/rest">[REST-base-path]</abbr>/job/[job-id]/activate</code></p>
					<p>
						The request URL starts with the <a href="#rest_base_path">REST-base-path</a>
						followed by <em>job/</em>, the ID of the measurement job to activate and the methods name <em>/activate</em>.
					</p>
					<p>
						Example URI to activate measurement job with ID 42 would be
						<code><abbr title="[application path]/rest">[REST-base-path]</abbr>/job/42/activate</code>
					</p>
					<h4>Parameters</h4>
					<dl>
						<dt>apiKey</dt>
						<dd>
							An api key for OpenSpeedMonitor. Provides permissions for api functions.
						</dd>
					</dl>

					<h3>Potential outcomes of a request</h3>
					<dl>
						<dt>HTTP status 200 OK</dt>
						<dd>
							The request handled successfully, a result in JSON notation is
							returned. It contains the json representation of the activated measurement job.
							<br />
							The response is of type application/json (encoding UTF-8) as described in <a href="http://tools.ietf.org/html/rfc4627">RFC4627</a>.
						</dd>
						<dt>HTTP status 400 Bad Request</dt>
						<dd>This function requires an api key. You missed to submit one as parameter. An error message with details is attached as response.
							<br />
							The response is of type text/plain (encoding UTF-8).
						</dd>
						<dt>HTTP status 404 Not Found</dt>
						<dd>Either submitted api key or job with submitted id don't exist. An error message with details is attached as response.
							<br />
							The response is of type text/plain (encoding UTF-8).
						</dd>
						<dt>HTTP status 403 Forbidden</dt>
						<dd>Submitted api key doesn't have permission to activate measurement jobs or is generally invalid. An error message with details is attached as response.
							<br />
							The response is of type text/plain (encoding UTF-8).
						</dd>
					</dl>

					<hr>

				</div>

				<div id="job-deactivation">

					<h2>PUT Method:&nbsp;<span class="text-info">Job deactivation</span></h2>

					<i class="icon-info-sign">&nbsp;${protectedFunctionHint}</i>
					<h3>Request signature</h3>
					<p><code><abbr title="[application path]/rest">[REST-base-path]</abbr>/job/[job-id]/deactivate</code></p>
					<p>
						The request URL starts with the <a href="#rest_base_path">REST-base-path</a>
						followed by <em>job/</em>, the ID of the measurement job to activate and the methods name <em>/deactivate</em>.
					</p>
					<p>
						Example URI to deactivate measurement job with ID 42 would be
						<code><abbr title="[application path]/rest">[REST-base-path]</abbr>/job/42/deactivate</code>
					</p>
					<h4>Parameters</h4>
					<dl>
						<dt>apiKey</dt>
						<dd>
							An api key for OpenSpeedMonitor. Provides permissions for api functions.
						</dd>
					</dl>

					<h3>Potential outcomes of a request</h3>
					<dl>
						<dt>HTTP status 200 OK</dt>
						<dd>
							The request handled successfully, a result in JSON notation is
							returned. It contains the json representation of the deactivated measurement job.
							<br>
							The response is of type application/json (encoding UTF-8) as described in <a href="http://tools.ietf.org/html/rfc4627">RFC4627</a>.
						</dd>
						<dt>HTTP status 400 Bad Request</dt>
						<dd>This function requires an api key. You missed to submit one as parameter. An error message with details is attached as response.
							<br />
							The response is of type text/plain (encoding UTF-8).
						</dd>
						<dt>HTTP status 404 Not Found</dt>
						<dd>Either submitted api key or job with submitted id don't exist. An error message with details is attached as response.
							<br />
							The response is of type text/plain (encoding UTF-8).
						</dd>
						<dt>HTTP status 403 Forbidden</dt>
						<dd>Submitted api key doesn't have permission to deactivate measurement jobs or is generally invalid. An error message with details is attached as response.
							<br />
							The response is of type text/plain (encoding UTF-8).
						</dd>
					</dl>

					<hr>

				</div>

				<div id="job-set-schedule">

					<h2>PUT Method:&nbsp;<span class="text-info">Set Execution Schedule</span></h2>

					<i class="icon-info-sign">&nbsp;${protectedFunctionHint}</i>

					<h3>Request signature</h3>
					<p><code><abbr title="[application path]/rest">[REST-base-path]</abbr>/job/[job-id]/setExecutionSchedule</code></p>
					<p>
						The request URL starts with the <a href="#rest_base_path">REST-base-path</a>
						followed by <em>job/</em>, the ID of the measurement job to activate and the methods name <em>/setExecutionSchedule</em>.
					</p>
					<p>
						Example URI to set execution schedule of measurement job with ID 42 would be
						<code><abbr title="[application path]/rest">[REST-base-path]</abbr>/job/42/setExecutionSchedule</code>
					</p>
					<h4>Body</h4>
					<p>
						The body of the PUT request must consist of a json containing the execution schedule to set.<br>
						Example body to let respective job run every two minutes:<br>
						<pre>{"executionSchedule": "* */2 * * * ? *"}</pre>
						See <a  href="http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger" target="_blank">Quartz documentation</a> for cron syntax of schedule.
					</p>
					<h4>Parameters</h4>
					<dl>
						<dt>apiKey</dt>
						<dd>
							An api key for OpenSpeedMonitor. Provides permissions for api functions.
						</dd>
					</dl>

					<h3>Potential outcomes of a request</h3>
					<dl>
						<dt>HTTP status 200 OK</dt>
						<dd>
							The request handled successfully, a result in JSON notation is
							returned. It contains the json representation of the measurement job with the new execution schedule.
							<br />
							The response is of type application/json (encoding UTF-8) as described in <a href="http://tools.ietf.org/html/rfc4627">RFC4627</a>.
						</dd>
						<dt>HTTP status 400 Bad Request</dt>
						<dd>Reasons can be the absence of an api key as parameter or of an execution schedule in the body of the request. Also an invalid schedule will
							result in a 400.  An error message with details is attached as response.
							<br />
							The response is of type text/plain (encoding UTF-8).
						</dd>
						<dt>HTTP status 404 Not Found</dt>
						<dd>Either submitted api key or job with submitted id don't exist. An error message with details is attached as response.
							<br />
							The response is of type text/plain (encoding UTF-8).
						</dd>
						<dt>HTTP status 403 Forbidden</dt>
						<dd>Submitted api key doesn't have permission to set execution schedule of measurement jobs or is generally invalid. An error message with details is attached as response.
							<br />
							The response is of type text/plain (encoding UTF-8).
						</dd>
					</dl>

					<hr>

				</div>

			</div>

		</div>

	</div>

<%--

	the following block can be pasted for new rest-methods
	(please leave this block untouched for further methods ;)

	<div id="[id-for-METHOD NAME]">

		<h2>Method:&nbsp;<span class="text-info">[METHOD NAME]</span></h2>

		<h3>Request signature</h3>
		<p>
			The request URL starts with the <a href="#rest_base_path">REST-base-path</a>
			followed by ...
			(remember special chars like whitespace need to be URL escaped which was left out here for readability)
		</p>
		<p>
			Example URI for ...
			<code><abbr title="[application path]/rest">[REST-base-path]</abbr>/...</code>
		</p>
		<h4>Additional parameters</h4>
		<p>
		   To make a more concrete request, you may add one or more of the following arguements as URL query arguments.
		</p>
		   <dl>
			<dt>[NAME OF QUERY PARAM]</dt>
			<dd>
				[DESCRIPTION OF QUERY PARAM]
			</dd>
		   </dl>

		<h3>Potential outcomes of a request</h3>
		<dl>
			<dt>HTTP status 200 OK</dt>
			<dd>
				The request handled successfully, a result in JSON notation is
				returned. It contains ... <br>Response example:
				<code>{"[KEY 1]":[VALUE 1],"[KEY 2]":[VALUE 2], ...}</code>
				<br />
				The response is of type application/json (encoding UTF-8) as described in <a href="http://tools.ietf.org/html/rfc4627">RFC4627</a>.
			</dd>
			<dt>HTTP status 400 Bad Request</dt>
			<dd>[DETAILED ERROR DESCRIPTION] An error message with details is attached as response.
				<br />
				The response is of type text/plain (encoding UTF-8).
			</dd>
			<dt>[FURTHER HTTP STATUS]</dt>
			<dd>[DETAILED DESCRIPTION]<br />
				The response is of type text/plain (encoding UTF-8).</dd>
		</dl>

		<hr>

	</div>

	--%>
	
	<r:script>
		$(document).ready(function(){
			$('body').scrollspy({
				target: '#spied-nav',
				offset: 100
			});
			var offset = 60;
			$('#spied-nav ul li a').click(function(event) {
				event.preventDefault();
				$($(this).attr('href'))[0].scrollIntoView();
				scrollBy(0, -offset);
			});
		});
	</r:script>

</body>
</html>