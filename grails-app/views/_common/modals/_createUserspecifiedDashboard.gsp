<div id="CreateUserspecifiedDashboardModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="CreateUserspecifiedDashboardModalLabel">
<div class="modal-header">
    <button type="button" class="close" data-dismiss="modal" ria-hidden="true">×</button>
    <h3 id="CreateUserspecifiedDashboardModalLabel"><g:message code="de.iteratec.isocsi.dashBoardControllers.custom.title" default="To save as custom dashboard, please enter additional information!"/></h3>
  </div>
  <div class="modal-body">
    <div class="control-group">
      <label class="control-label" for="dashboardNameFromModal">${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.dashboardName.label', default: 'Dashboard name')}</label>
      <div class="controls">
        <input type="text" class="span3" name="dashboardNameFromModal" id="dashboardNameFromModal" placeholder="${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.dashboardName.label', default: 'Dashboard name')}">
      </div>
    </div>
    <div class="control-group">
      <div class="controls">
        <label class="checkbox" for="publiclyVisibleFromModal">
          <input type="checkbox" name="publiclyVisibleFromModal" id="publiclyVisibleFromModal" >
          ${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.publiclyVisible.label', default: 'Everybody is entitled to view this custom dashboard.')}
        </label>
      </div>
    </div>
  </div>
  <div class="modal-footer">
    <g:form>
      <button class="btn" data-dismiss="modal"><g:message code="default.button.cancel.label" default="Cancel"/></button>
      <g:hiddenField name="id" value="${item ? item.id : params.id}" />
      <g:hiddenField name="_method" value="POST" />
      <span class="button">
        <g:actionSubmit class="btn btn-primary" action="delete" value="${message(code: 'de.iteratec.ism.ui.labels.save', default: 'Save')}" onClick="saveCustomDashboard();return false;"/>
      </span>
    </g:form>
    
  </div>
</div>
<script>
    function saveCustomDashboard(){
      //feldeingaben validieren
      if (document.getElementById("dashboardNameFromModal").value.trim() !== "") {
        //abschicken füllt versteckte inputs
        document.getElementById("dashboardName").value = document.getElementById("dashboardNameFromModal").value;
        if (document.getElementById('publiclyVisibleFromModal').checked) {
          document.getElementById("publiclyVisible").checked = true;
        } else {
          document.getElementById("publiclyVisible").checked = false;
        }        
        $('#CreateUserspecifiedDashboardModal').modal('hide');
        //abschicken klickt speichern an
        $('#hiddenTriggerToStoreCustomDashboard').click();
        return false;
      } else {
        alert("${message(code: 'de.iteratec.isocsi.dashBoardControllers.custom.dashboardName.error', default: 'Please enter a (non-empty) name.')}");
        return false;
      }
    }
</script>