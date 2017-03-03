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
          fill: "green",
          fillOpacity: trafficLightBarOpacity,
          name: "GOOD",
          cssClass: "good"
        },
        {
          id: "all_ok",
          lowerBoundary: goodLoadtime,
          upperBoundary: badLoadtime,
          fill: "yellow",
          fillOpacity: trafficLightBarOpacity,
          name: "OK",
          cssClass: "ok"
        },
        {
          id: "all_bad",
          lowerBoundary: badLoadtime,
          upperBoundary: absoluteMaxValue,
          fill: "red",
          fillOpacity: trafficLightBarOpacity,
          name: "BAD",
          cssClass: "bad"
        }
      ]
    } else if (absoluteMaxValue > goodLoadtime) {
      trafficLightData = [
        {
          id: "justGoodAndOk_good",
          lowerBoundary: 0,
          upperBoundary: goodLoadtime,
          fill: "green",
          fillOpacity: trafficLightBarOpacity,
          name: "GOOD",
          cssClass: "good"
        },
        {
          id: "justGoodAndOk_ok",
          lowerBoundary: goodLoadtime,
          upperBoundary: absoluteMaxValue,
          fill: "yellow",
          fillOpacity: trafficLightBarOpacity,
          name: "OK",
          cssClass: "ok"
        },
      ]
    } else if (absoluteMaxValue > 0) {
      trafficLightData = [
        {
          id: "justGood_good",
          lowerBoundary: 0,
          upperBoundary: absoluteMaxValue,
          fill: "green",
          fillOpacity: trafficLightBarOpacity,
          name: "GOOD",
          cssClass: "good"
        },
      ]
    }
    return trafficLightData;
  };

  return {
    getTimeData: getTimeData
  }

})();
