/**
 *
 * Created by nkuhn on 03.03.17.
 */

OpenSpeedMonitor.ChartModules.TrafficLightDataProvider = (function () {

  var trafficLightBarOpacity = 0.4;

  var getTimeData = function (absoluteMaxValue) {

    var trafficLightData = [],
      goodLoadtime = 1000,
      badLoadtime = 3000;

    if (absoluteMaxValue > badLoadtime) {
      trafficLightData = [
        {
          id: "all_good",
          lowerBoundary: 0,
          upperBoundary: goodLoadtime,
          fill: "#5cb85c",
          fillOpacity: trafficLightBarOpacity,
          name: "GOOD",
          cssClass: "d3chart-good"
        },
        {
          id: "all_ok",
          lowerBoundary: goodLoadtime,
          upperBoundary: badLoadtime,
          fill: "#f0ad4e",
          fillOpacity: trafficLightBarOpacity,
          name: "OK",
          cssClass: "d3chart-ok"
        },
        {
          id: "all_bad",
          lowerBoundary: badLoadtime,
          upperBoundary: absoluteMaxValue,
          fill: "#d9534f",
          fillOpacity: trafficLightBarOpacity,
          name: "BAD",
          cssClass: "d3chart-bad"
        }
      ]
    } else if (absoluteMaxValue > goodLoadtime) {
      trafficLightData = [
        {
          id: "justGoodAndOk_good",
          lowerBoundary: 0,
          upperBoundary: goodLoadtime,
          fill: "#5cb85c",
          fillOpacity: trafficLightBarOpacity,
          name: "GOOD",
          cssClass: "d3chart-good"
        },
        {
          id: "justGoodAndOk_ok",
          lowerBoundary: goodLoadtime,
          upperBoundary: absoluteMaxValue,
          fill: "#f0ad4e",
          fillOpacity: trafficLightBarOpacity,
          name: "OK",
          cssClass: "d3chart-ok"
        },
      ]
    } else if (absoluteMaxValue > 0) {
      trafficLightData = [
        {
          id: "justGood_good",
          lowerBoundary: 0,
          upperBoundary: absoluteMaxValue,
          fill: "#5cb85c",
          fillOpacity: trafficLightBarOpacity,
          name: "GOOD",
          cssClass: "d3chart-good"
        },
      ]
    }
    return trafficLightData;
  };

  return {
    getTimeData: getTimeData,
    initialBarWidth: 20
  }

})();
