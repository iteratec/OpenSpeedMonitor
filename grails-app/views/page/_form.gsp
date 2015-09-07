<%@ page import="de.iteratec.osm.csi.Page" %>



			<div class="control-group fieldcontain ${hasErrors(bean: pageInstance, field: 'name', 'error')} ">
				<label for="name" class="control-label"><g:message code="page.name.label" default="Name" /></label>
				<div class="controls">
					<g:textArea name="name" cols="40" rows="5" maxlength="255" value="${pageInstance?.name}"/>
				</div>
			</div>

			<div class="control-group fieldcontain ${hasErrors(bean: pageInstance, field: 'weight', 'error')} required">
				<label for="weight" class="control-label"><g:message code="page.weight.label" default="Weight" /><span class="required-indicator">*</span></label>
				<div class="controls">
					<g:field type="number" name="weight" step="any" min="0.0" required="" value="${pageInstance.weight}"/>
				</div>
			</div>

            <div class="control-group">
                <div class="control-label"><g:message code="page.csimapping.existence.label" default="CSI-Mapping" /></div>
                <div class="controls">
                    <g:if test="${mappingsOfPage}">
                        <g:render template="/chart/csi-mappings"
                                  model="${['transformableMappings': mappingsOfPage, 'chartIdentifier': 'edit_page',
                                            'bottomOffsetXAxis': 216, 'yAxisRightOffset': 989, 'chartBottomOffset': 170,
                                            'yAxisTopOffset': 5, 'bottomOffsetLegend': 130]}"/>
                        <a href="#CsiMappingModal" role="button" class="btn btn-primary" data-toggle="modal">${message(code: 'de.iteratec.osm.csi.edit-mapping.label', default: 'Edit CSI mapping')}</a>
                    </g:if>
                    <g:else>
                        <p><g:message code="page.csimapping.nonexistence" default="None associated"/></p>
                        <a href="#CsiMappingModal" role="button" class="btn btn-primary" data-toggle="modal">${message(code: 'de.iteratec.osm.csi.edit-mapping.label', default: 'Add CSI mapping')}</a>
                    </g:else>

                </div>

            </div>

