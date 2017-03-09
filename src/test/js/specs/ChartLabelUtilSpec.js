/**
 * Created by nkuhn on 23.02.17.
 */
describe("BarchartUtil function buildShortestUniqueLabel", function () {

  beforeEach(function () {
    //nothing to do here
  });

  it("provides just page as label and correct header if jobGroups are equal in all seriesData entries", function () {
    var seriesDataJustPageDiffers = [
      {
        grouping: "MES | develop_Desktop",
        measurand: "docCompleteTimeInMillisecs",
        value: 909.413
      },
      {
        grouping: "ADS | develop_Desktop",
        measurand: "docCompleteTimeInMillisecs",
        value: 1040.8115
      },
      {
        grouping: "HP | develop_Desktop",
        measurand: "docCompleteTimeInMillisecs",
        value: 1351.7468
      },
      {
        grouping: "ADS_entry | develop_Desktop",
        measurand: "docCompleteTimeInMillisecs",
        value: 2693.3905
      }
    ];
    var expectedSeriesData = [
      {
        grouping: "MES | develop_Desktop",
        measurand: "docCompleteTimeInMillisecs",
        value: 909.413,
        label: "MES",
        page: "MES",
        jobGroup: "develop_Desktop"
      },
      {
        grouping: "ADS | develop_Desktop",
        measurand: "docCompleteTimeInMillisecs",
        value: 1040.8115,
        label: "ADS",
        page: "ADS",
        jobGroup: "develop_Desktop"
      },
      {
        grouping: "HP | develop_Desktop",
        measurand: "docCompleteTimeInMillisecs",
        value: 1351.7468,
        label: "HP",
        page: "HP",
        jobGroup: "develop_Desktop"
      },
      {
        grouping: "ADS_entry | develop_Desktop",
        measurand: "docCompleteTimeInMillisecs",
        value: 2693.3905,
        label: "ADS_entry",
        page: "ADS_entry",
        jobGroup: "develop_Desktop"
      }
    ];
    var expectedCommonLabelParts = "Measurand: docCompleteTimeInMillisecs | Job Group: develop_Desktop";

    var seriesLabels = OpenSpeedMonitor.ChartModules.ChartLabelUtil(seriesDataJustPageDiffers);
    expect(seriesLabels.getSeriesWithShortestUniqueLabels()).toEqual(expectedSeriesData)
    expect(seriesLabels.getCommonLabelParts()).toEqual(expectedCommonLabelParts)

  });

  it("provides just jobGroups as label and correct header if pages are equal in all seriesData entries", function () {
    var seriesDataJustJobGroupDiffers = [
      {
        grouping: "ADS | develop_Desktop",
        measurand: "firstByteInMillisecsUncached",
        value: 909.413
      },
      {
        grouping: "ADS | develop_Smartphone",
        measurand: "firstByteInMillisecsUncached",
        value: 1040.8115
      },
      {
        grouping: "ADS | develop_Tablet",
        measurand: "firstByteInMillisecsUncached",
        value: 1351.7468
      },
      {
        grouping: "ADS | live_Desktop",
        measurand: "firstByteInMillisecsUncached",
        value: 909.413
      },
      {
        grouping: "ADS | live_Smartphone",
        measurand: "firstByteInMillisecsUncached",
        value: 1040.8115
      },
      {
        grouping: "ADS | live_Tablet",
        measurand: "firstByteInMillisecsUncached",
        value: 1351.7468
      }
    ];
    var expectedSeriesData = [
      {
        grouping: "ADS | develop_Desktop",
        measurand: "firstByteInMillisecsUncached",
        value: 909.413,
        label: "develop_Desktop",
        page: "ADS",
        jobGroup: "develop_Desktop"
      },
      {
        grouping: "ADS | develop_Smartphone",
        measurand: "firstByteInMillisecsUncached",
        value: 1040.8115,
        label: "develop_Smartphone",
        page: "ADS",
        jobGroup: "develop_Smartphone"
      },
      {
        grouping: "ADS | develop_Tablet",
        measurand: "firstByteInMillisecsUncached",
        value: 1351.7468,
        label: "develop_Tablet",
        page: "ADS",
        jobGroup: "develop_Tablet"
      },
      {
        grouping: "ADS | live_Desktop",
        measurand: "firstByteInMillisecsUncached",
        value: 909.413,
        label: "live_Desktop",
        page: "ADS",
        jobGroup: "live_Desktop"
      },
      {
        grouping: "ADS | live_Smartphone",
        measurand: "firstByteInMillisecsUncached",
        value: 1040.8115,
        label: "live_Smartphone",
        page: "ADS",
        jobGroup: "live_Smartphone"
      },
      {
        grouping: "ADS | live_Tablet",
        measurand: "firstByteInMillisecsUncached",
        value: 1351.7468,
        label: "live_Tablet",
        page: "ADS",
        jobGroup: "live_Tablet"
      }
    ];
    var expectedCommonLabelParts = "Measurand: firstByteInMillisecsUncached | Page: ADS";

    var seriesLabels = OpenSpeedMonitor.ChartModules.ChartLabelUtil(seriesDataJustJobGroupDiffers);
    expect(seriesLabels.getSeriesWithShortestUniqueLabels()).toEqual(expectedSeriesData)
    expect(seriesLabels.getCommonLabelParts()).toEqual(expectedCommonLabelParts)

  });

  it("provides pages and jobGroups as label and correct header if pages and jobgroups differ", function () {
    var seriesDataPageAndJobGroupDiffer = [
      {
        grouping: "ADS | develop_Desktop",
        measurand: "firstByteInMillisecsUncached",
        value: 909.413
      },
      {
        grouping: "ADS_entry | develop_Desktop",
        measurand: "firstByteInMillisecsUncached",
        value: 1040.8115
      },
      {
        grouping: "ADS | develop_Tablet",
        measurand: "firstByteInMillisecsUncached",
        value: 1351.7468
      },
      {
        grouping: "ADS_entry | develop_Tablet",
        measurand: "firstByteInMillisecsUncached",
        value: 909.413
      }
    ];
    var expectedSeriesData = [
      {
        grouping: "ADS | develop_Desktop",
        measurand: "firstByteInMillisecsUncached",
        value: 909.413,
        page: "ADS",
        jobGroup: "develop_Desktop",
        label: "ADS | develop_Desktop"
      },
      {
        grouping: "ADS_entry | develop_Desktop",
        measurand: "firstByteInMillisecsUncached",
        value: 1040.8115,
        page: "ADS_entry",
        jobGroup: "develop_Desktop",
        label: "ADS_entry | develop_Desktop"
      },
      {
        grouping: "ADS | develop_Tablet",
        measurand: "firstByteInMillisecsUncached",
        value: 1351.7468,
        page: "ADS",
        jobGroup: "develop_Tablet",
        label: "ADS | develop_Tablet"
      },
      {
        grouping: "ADS_entry | develop_Tablet",
        measurand: "firstByteInMillisecsUncached",
        value: 909.413,
        page: "ADS_entry",
        jobGroup: "develop_Tablet",
        label: "ADS_entry | develop_Tablet"
      }
    ];
    var expectedCommonLabelParts = "Measurand: firstByteInMillisecsUncached";

    var seriesLabels = OpenSpeedMonitor.ChartModules.ChartLabelUtil(seriesDataPageAndJobGroupDiffer);
    expect(seriesLabels.getSeriesWithShortestUniqueLabels()).toEqual(expectedSeriesData)
    expect(seriesLabels.getCommonLabelParts()).toEqual(expectedCommonLabelParts)

  });

});