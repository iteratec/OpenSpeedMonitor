FutureOnlyTimeago = function() {
	var nextExecutionServerUrl = '';
	var timeouts = [];
	
	jQuery.timeago.settings.allowFuture = true;
	
	function futureHandler(cronExpression, elem, parentElem) {
		delete FutureOnlyTimeago.timeouts[elem];
		jQuery.ajax({
			type: 'POST', 
			url: FutureOnlyTimeago.nextExecutionServerUrl,
			data: { value: cronExpression, noprepend: true },
			success : function(newNextExecutionHtml) {
				// replace past date with new template
				$(parentElem).html(newNextExecutionHtml);
				var newElem = $('abbr.timeago', parentElem);
				makeFutureOnlyTimeago(newElem);
			}
		});
	};

	function makeFutureOnlyTimeago(elem) {
		var fromNowInMs = $(elem).attr('data-date-diff-ms');
		var cronExpression = $(elem).attr('data-cronstring');
		var parentElem = $(elem).parent();
		
		if (cronExpression && fromNowInMs < 1000 * 60 * 60 * 24 * 7) {
			var updateTimeout = setTimeout(futureHandler.bind(this, cronExpression, elem, parentElem), fromNowInMs);
			FutureOnlyTimeago.timeouts[elem] = updateTimeout;
			
			// cut off second as PrettyCron can only handle Cron expressions where minutes are the smallest unit
			//var prettyCronString = prettyCron.toString(cronExpression.substr(cronExpression.indexOf(' ') + 1));
			//$(parentElem).append('(<abbr class="cronExpression">' + prettyCronString + '</abbr>)');
		}
		$(elem).timeago();
	}
	
	// ${createLink(action: 'nextExecution', absolute: true)}
	function initializeTimeagos(timeagos, nextExecutionServerUrl) {
		this.nextExecutionServerUrl = nextExecutionServerUrl;
		this.timeouts = [];
		$.each(timeagos, function (index, elem) {
			makeFutureOnlyTimeago(elem);
		});
	}
	
	return {
		init: initializeTimeagos
	}
}();