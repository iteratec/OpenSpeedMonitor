<div class="modal fade" id="versionControlModal" tabindex="-1" role="dialog" aria-labelledby="versionControlModalLabel">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="versionControlModalLabel"><g:message code="script.versionControl.modal.title"/></h4>
            </div>
            <div class="row">
                <div class="col-md-4">
                    <div class="modal-body">
                        <div id="archivedNavigationScriptTableWrapper">
                            <div id="archivedNavigationScriptTableScroll">
                                <table class="table table-hover">
                                    <thead>
                                    <tr>
                                        <th><g:message code="script.versionControl.table.date"/></th>
                                        <th><g:message code="script.versionControl.table.versionDescription"/></th>
                                        <th> </th>
                                    </tr>
                                    </thead>
                                    <tbody id="archivedNavigationScriptTableBody">
                                    <g:each in="${archivedScripts}" status="i" var="archivedScript" >
                                        <tr id="archivedNavigationScriptTableRow-${archivedScript.id}">
                                            <td class="versionControlMinCellWidth"><g:formatDate date="${archivedScript.dateCreated}" /></td>
                                            <td><textarea id="versionDescriptionTextArea-${archivedScript.id}" rows='1' class="script-textarea autoExpand form-control" maxlength="255" defaultValue ="${archivedScript.versionDescription}">${archivedScript.versionDescription}</textarea>  </td>
                                            <td>
                                                <div class="script-save-abort-container">
                                                    <i id="versionDescriptionSaveButton-${archivedScript.id}"
                                                       class="fas fa-save script-icon" style="visibility: hidden;"></i>
                                                    <i id="versionDescriptionAbortButton-${archivedScript.id}"
                                                       class="fas fa-times script-icon" style="visibility: hidden;"></i>
                                                </div>
                                            </td>
                                        </tr>
                                    </g:each>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-md-8" id="archivedNavigationScriptColumn">
                    <label for="archivedNavigationScript">
                        <g:message code="script.navigationScript.label" default="Code" />
                    </label>
                    <textarea name="archivedNavigationScript" id="archivedNavigationScript"></textarea>
                    <span id="archived-setEventName-warning-clone" class="setEventName-warning-icon" style="display: none;" rel="tooltip" data-html="true"></span>
                    <p>
                        <input type="checkbox" id="archivedLineBreakToggle" checked />
                        <label for="archivedLineBreakToggle" style="display: inline">
                            <g:message code="script.wrapLines.label" />
                        </label>
                    </p>

                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal"><g:message code="script.versionControl.cancel.button"/></button>
                <g:actionSubmit class="btn btn-primary" action="loadArchivedScript"
                                value="${message(code: 'script.versionControl.load.button')}"/>
            </div>
        </div>
    </div>
</div>