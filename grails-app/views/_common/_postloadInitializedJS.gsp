<script type="text/javascript">
    OpenSpeedMonitor = OpenSpeedMonitor || {};
    $(window).on('load', function () {
        window.addEventListener("PostLoadedScriptArrived", function () {
            OpenSpeedMonitor.postLoaded.idOfItemToDelete = ${item ? item.id : params.id ?: 'null'};
        });

        OpenSpeedMonitor.postLoader.loadJavascript('<g:assetPath src="postload/application-postload.js"/>', "postload");
    });
</script>
