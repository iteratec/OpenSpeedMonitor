<%@ page import="de.iteratec.osm.report.chart.ChartingLibrary"%>

<div class="span12 accordion" id="accordion2">
	<div class="accordion-group">
		<div class="accordion-heading">
			<a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#collapseAdjustment">
			<g:message code="de.iteratec.chart.adjustment.name"/>
			</a>
		</div>
		<div id="collapseAdjustment" class="accordion-body collapse in">
			<div class="accordion-inner">
				<div class="span11">
					<!-- Diagram-title -->
					<div class ="row">
						<div class="span2 text-right"><g:message code="de.iteratec.chart.title.name"/></div>
						<div class="span9"><input id="dia-title" class="input-xxlarge" type="text" value="${chartTitle}"></div>
					</div>
					<!-- diagram-size -->
					<div class ="row">
						<div class="span2 text-right"><g:message code="de.iteratec.chart.size.name"/></div>
						<div class="span9">
							<div class="input-prepend">
								<span class="add-on"><g:message code="de.iteratec.chart.width.name"/></span>
								<input class="span1 content-box" id="dia-width" type="text" value="${initialChartWidth}">
							</div>
							<div class="input-prepend">
								<span class="add-on"><g:message code="de.iteratec.chart.height.name"/></span>
								<input class="span1 content-box" id="dia-height" type="text" value="${initialChartHeight}">
							</div>
							<button class="btn" id="dia-change-chartsize" style="vertical-align: top;"><g:message code="de.iteratec.ism.ui.button.apply.name"/></button>
						</div>
					</div>
					<!-- Y-Axis -->
					<g:if test="${grailsApplication.config.grails.de.iteratec.osm.report.chart.chartTagLib == ChartingLibrary.HIGHCHARTS}">
						<!-- highchart -->
						<g:if test="${singleYAxis}">
							<div id="adjust_chart_y_axis" class ="row">
								<div class="span2 text-right"><g:message code="de.iteratec.chart.axis.y.name"/></div>
								<div class="span9">
									<div class="input-prepend">
										<span class="add-on"><g:message code="de.iteratec.chart.axis.y.minimum.name"/></span>
										<input class="span1 content-box" id="dia-y-axis-min" type="text" value="${yAxisMin?:'' }">
									</div>
									<div class="input-prepend">
										<span class="add-on"><g:message code="de.iteratec.chart.axis.y.maximum.name"/></span>
										<input class="span1 content-box" id="dia-y-axis-max" type="text" value="${yAxisMax?:'' }">
									</div>
									<button class="btn" id="dia-change-yaxis" style="vertical-align: top;"><g:message code="de.iteratec.ism.ui.button.apply.name"/></button>
								</div>
							</div>
						</g:if>
							<!-- Show data-markers -->
							<div class ="row">
								<div class="span2 text-right"><g:message code="de.iteratec.isocsi.csi.show.datamarkers" default="Punkte anzeigen"/></div>
								<div class="span9"><g:checkBox id="to-enable-marker" name="toEnableMarker" checked="${markerShouldBeEnabled}" /></div>
							</div>
					</g:if>
					<g:else>
					<!-- rickshaw -->
						<div id="adjust_chart_y_axis" class ="row">
							<div class="span2 text-right"><g:message code="de.iteratec.chart.axis.y.name"/></div>
							<div class="span9">
								<div class="input-prepend">
									<span class="add-on"><g:message code="de.iteratec.chart.axis.y.minimum.name"/></span>
									<input class="span1 content-box" id="dia-y-axis-min" type="text" value="${yAxisMin?:'' }">
								</div>
								<div class="input-prepend">
									<span class="add-on"><g:message code="de.iteratec.chart.axis.y.maximum.name"/></span>
									<input class="span1 content-box" id="dia-y-axis-max" type="text" value="${yAxisMax?:'' }">
								</div>
								<button class="btn" id="dia-change-yaxis" style="vertical-align: top;"><g:message code="de.iteratec.ism.ui.button.apply.name"/></button>
							</div>
						</div>
						<div class ="row">
								<div class="span2 text-right"><g:message code="de.iteratec.isocsi.csi.show.datamarkers" default="Punkte anzeigen"/></div>
								<div class="span9"><g:checkBox id="to-enable-marker" name="toEnableMarker" checked="${markerShouldBeEnabled}" /></div>
							</div>
					</g:else>
				</div>
			</div>
		</div>
	</div>
</div>