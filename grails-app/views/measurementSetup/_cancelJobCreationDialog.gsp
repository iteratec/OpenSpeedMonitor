<!-- This is the dialog that informs the user that his job creation will be aborted -->

<div class="modal fade" id="cancelJobCreationDialog" tabindex="-1" role="dialog" aria-labelledby="cancelJobCreationDialogLabel">
    <div class="modal-dialog modal-sm" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title">
                    <i class="fa fa-exclamation-circle" aria-hidden="true"></i>
                    Caution
                </h4>
            </div>
            <div class="modal-body">
                Your progress will be lost if you proceed. Do you want to continue?
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">
                    No
                </button>
                <a type="button" class="btn btn-primary" href="/">
                    Yes
                </a>
            </div>
        </div>
    </div>
</div>