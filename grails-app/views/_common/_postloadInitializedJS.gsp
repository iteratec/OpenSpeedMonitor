<script>
    $( window ).load(function() {

        window.addEventListener("PostLoadedScriptArrived",function(){

            var idOfItemToBeDeleted = ${item ? item.id : params.id}

            POSTLOADED = new PostLoaded({
                i18n_duplicatePrompt: '${message(code: 'de.iteratec.actions.duplicate.prompt')}',
                i18n_duplicateSuffix: '${message(code: 'de.iteratec.actions.duplicate.copy')}',
                deletionConfirmMessage: '${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}',
                idOfItemToDelete: idOfItemToBeDeleted ? idOfItemToBeDeleted : 'not relevant on this page'
            });
        });

        var loader = new PostLoader();
        loader.loadJavascript('<g:assetPath src="postload/application-postload.js" absolute="true"/>');

    });
</script>