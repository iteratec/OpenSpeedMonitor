/**
 * Created by nkuhn on 23.02.17.
 */
describe("BarchartUtil function buildShortestUniqueLabel", function () {
  beforeEach(function () {

  });
  it("provides just page as label and correct header if jobGroups and measurands are equal in all seriesData entries", function () {
    var seriesDataJustPageDiffers = [
      {
        identifier: "MES | develop_Desktop | docCompleteTimeInMillisecs",
        measurand: "docCompleteTimeInMillisecs",
        value: 909.413
      },
      {
        identifier: "ADS | develop_Desktop | docCompleteTimeInMillisecs",
        measurand: "docCompleteTimeInMillisecs",
        value: 1040.8115
      },
      {
        identifier: "HP | develop_Desktop | docCompleteTimeInMillisecs",
        measurand: "docCompleteTimeInMillisecs",
        value: 1351.7468
      },
      {
        identifier: "ADS_entry | develop_Desktop | docCompleteTimeInMillisecs",
        measurand: "docCompleteTimeInMillisecs",
        value: 2693.3905
      }
    ];
    var expectedSeriesData = [
      {
        identifier: "MES | develop_Desktop | docCompleteTimeInMillisecs",
        measurand: "docCompleteTimeInMillisecs",
        value: 909.413,
        label: "MES"
      },
      {
        identifier: "ADS | develop_Desktop | docCompleteTimeInMillisecs",
        measurand: "docCompleteTimeInMillisecs",
        value: 1040.8115,
        label: "ADS"
      },
      {
        identifier: "HP | develop_Desktop | docCompleteTimeInMillisecs",
        measurand: "docCompleteTimeInMillisecs",
        value: 1351.7468,
        label: "HP"
      },
      {
        identifier: "ADS_entry | develop_Desktop | docCompleteTimeInMillisecs",
        measurand: "docCompleteTimeInMillisecs",
        value: 2693.3905,
        label: "ADS_entry"
      }
    ];

    OpenSpeedMonitor.ChartModules.LabelUtils.buildShortestUniqueLabel(seriesDataJustPageDiffers);
    expect(seriesDataJustPageDiffers).toBeDefined();
    expect(expectedSeriesData).toBeDefined();
    expect(
      seriesDataJustPageDiffers.toEqual(expectedSeriesData)
    )

  });

});