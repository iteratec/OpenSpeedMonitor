import * as d3 from "d3";

export default class ChartColorProvider {
  private static measurandGroupColorCombination = ChartColorProvider.initColors();

  private static initColors() {
    const loadingTimeColors = [
        "#1660A7",
        "#558BBF",
        "#95b6d7",
        "#d4e2ef"
      ],
      countOfRequestColors = [
        "#E41A1C",
        "#eb5859",
        "#f29697",
        "#fad5d5"
      ],
      sizeOfRequestColors = [
        "#F18F01",
        "#f4ad46",
        "#f8cc8b",
        "#fcead0"
      ],
      csiColors = [
        "#59B87A",
        "#86cb9e",
        "#b3dec2",
        "#e0f2e6"
      ];

    return {
      "ms": loadingTimeColors,
      "s": loadingTimeColors,
      "#": countOfRequestColors,
      "KB": sizeOfRequestColors,
      "MB": sizeOfRequestColors,
      "%": csiColors,
      "": loadingTimeColors
    }
  }

  static getColorscaleForMeasurandGroup(measurandUnit: string, skipFirst: boolean = false) {
    const colors = ChartColorProvider.measurandGroupColorCombination[measurandUnit].slice(skipFirst ? 1 : 0);
    return d3.scaleOrdinal(colors)
      .domain(ChartColorProvider.createDomain(colors.length));
  }

  private static createDomain(arrayLength: number): string[] {
    return Array.from(Array(arrayLength).keys())
      .map(elem => elem.toString());
  }
}
