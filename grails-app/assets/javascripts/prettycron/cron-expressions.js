/* 
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* 	http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License.
*/

function getPrettyCron(cronExpression) {
  if (cronExpression == "") {
    return "";
  } else {
    return prettyCron.toString(cronExpression);
  }
}

function updatePrettyCrons() {
  $.each($('abbr.cronExpression'), function(index, elem) {
     if(!$(elem).attr('data-seconds-cutted') ) {
       $(elem).attr('data-seconds-cutted', 'true');
      var cronExpression = elem.innerHTML;
      var cronExpressionNoSeconds = cronExpression.substr(cronExpression.indexOf(' ') + 1);
      $(elem).attr('title', getPrettyCron(cronExpressionNoSeconds));
      $(elem).html(cronExpressionNoSeconds);
     }
  });
}


$(function () {
	updatePrettyCrons();
});