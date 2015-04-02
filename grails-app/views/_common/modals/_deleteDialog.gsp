<!-- 
This is the standard dialog that initiates the delete action.
-->

<!-- Modal dialog -->
<r:require modules="spin"/>
<div id="DeleteModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="DeleteModalLabel" aria-hidden="true" onshow="changeText();">
<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal" ria-hidden="true">×</button>
		<h3 id="DeleteModalLabel"><g:message code="default.button.delete.confirm.title" default="Delete Item"/></h3>
	</div>
	<div class="modal-body">
		<p><g:message code="default.button.delete.confirm.message" args="[entityName]" default="Do you really want to delete this item?"/></p>
        <div id = "spinner-position"></div>
	</div>
	<div class="modal-footer">
		<g:form>
			<button class="btn" data-dismiss="modal" aria-hidden="true"><g:message code="default.button.cancel.label" default="Cancel"/></button>
			<span class="button">
                <g:actionSubmit class="btn btn-danger" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}"/>
            </span>
		</g:form>
		
	</div>
</div>

<script>
    function changeText(){
        setTimeout(function(){
            var opts = {
                lines: 10, // The number of lines to draw
                length: 3, // The length of each line
                width: 2, // The line thickness
                radius: 5, // The radius of the inner circle
                corners: 1, // Corner roundness (0..1)
                rotate: 0, // The rotation offset
                direction: 1, // 1: clockwise, -1: counterclockwise
                color: '#000', // #rgb or #rrggbb or array of colors
                speed: 1, // Rounds per second
                trail: 60, // Afterglow percentage
                shadow: true, // Whether to render a shadow
                hwaccel: false, // Whether to use hardware acceleration
                className: 'spinner', // The CSS class to assign to the spinner
                zIndex: 2e9, // The z-index (defaults to 2000000000)
                top: 'auto', // Top position relative to parent in px
                left: '50%' // Left position relative to parent in px
            };
            var spinner = new Spinner(opts).spin(document.getElementById('spinner-position'));
            var text = domainDeleteConfirmation('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}',${item ? item.id : params.id});
            spinner.stop()
            $('#DeleteModal').find('p').html(text);
        },1);
    }
</script>