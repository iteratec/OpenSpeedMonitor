//= require node_modules/elastic-apm-js-base/dist/bundles/elastic-apm-js-base.umd.min
//= require node_modules/tti-polyfill/tti-polyfill
//= require_self

"use strict";

var OpenSpeedMonitor = OpenSpeedMonitor || {};

OpenSpeedMonitor.apmRum = (function () {

  var APM_ENDPOINT_URI = 'http://localhost:8200';
  var APM_SERVICE_NAME = 'osm';
  var APM_LOG_LEVEL = 'info';

  var APM = elasticApm.init({
    serviceName: APM_SERVICE_NAME,
    serviceVersion: '5.1.0',
    serverUrl: APM_ENDPOINT_URI,
    // Set APM as active. If the toggle for sampling is set, this will only apply APM for
    // a subset of the clients
    active: true,
    logLevel: APM_LOG_LEVEL,
    // This option sets the flush interval in milliseconds
    flushInterval: 500,
    transactionSampleRate: 0
  });
  elasticApm.setUserContext({
    id: 'osm-application',
    username: 'osm',
    email: 'osm@iteratec.de'
  });
  elasticApm.setInitialPageLoadName('PageLoad');

  var sendMetric = function(metric) {
    var transaction = APM.startTransaction('metricWrapper', 'custom')
    var span = APM.startSpan("metricWrapper", "custom");
    span.addTags({
      metricName: metric.name,
      value: metric.value
    });
    span.end();
    transaction.end();
  };

  return {
    sendMetric: sendMetric
  };
})();

ttiPolyfill.getFirstConsistentlyInteractive().then(function (tti) {
  if (!!tti && tti > 0) {
    OpenSpeedMonitor.apmRum.sendMetric({
      name: "tti",
      value: tti
    });
  }
});