import {Injectable} from '@angular/core';
import {TimeSeries} from '../../models/time-series.model';
import {
  scaleLinear as d3ScaleLinear,
  ScaleLinear as D3ScaleLinear,
  scaleTime as d3ScaleTime,
  ScaleTime as D3ScaleTime
} from 'd3-scale';
import {TimeSeriesPoint} from '../../models/time-series-point.model';
import {max as d3Max, min as d3Min} from 'd3-array';

@Injectable({
  providedIn: 'root'
})
export class LineChartScaleService {

  constructor() {
  }

  /**
   * Determine the xScale for the given data
   */
  getXScale(data: { [key: string]: TimeSeries[] }, width: number): D3ScaleTime<number, number> {
    return d3ScaleTime()               // Define a scale for the X-Axis
      .range([0, width])  // Display the X-Axis over the complete width
      .domain([this.getMinDate(data), this.getMaxDate(data)]);
  }

  /**
   * Determine the yScales for the given data
   */
  getYScales(dataList: { [key: string]: TimeSeries[] },
             height: number,
             dataTrimValues: { [key: string]: { [key: string]: number } }
  ): { [key: string]: D3ScaleLinear<number, number> } {
    const yScales: { [key: string]: D3ScaleLinear<number, number> } = {};
    Object.keys(dataList).forEach((key: string) => {
      const min: number = dataTrimValues.min[key] ? Math.max(dataTrimValues.min[key], 0) : 0;
      const max: number = this.getMaxValue(dataList[key], dataTrimValues.max[key]);
      yScales[key] = d3ScaleLinear()              // Linear scale for the numbers on the Y-Axis
        .range([height, 0])  // Display the Y-Axis over the complete height - origin is top left corner, so height comes first
        .domain([min, max])
        .nice();
    });

    return yScales;
  }

  /**
   * Determine the yScales for the given data in time range
   */
  getYScalesInTimeRange(dataList: { [key: string]: TimeSeries[] },
                        minDate: Date,
                        maxDate: Date,
                        height: number,
                        dataTrimValues: { [key: string]: { [key: string]: number } }
  ): { [key: string]: D3ScaleLinear<number, number> } {
    const yScales: { [key: string]: D3ScaleLinear<number, number> } = {};
    Object.keys(dataList).forEach((key: string) => {
      const min: number = dataTrimValues.min[key] ? Math.max(dataTrimValues.min[key], 0) : 0;
      const max: number = this.getMaxValueInTime(dataList[key], minDate, maxDate, dataTrimValues.max[key]);
      yScales[key] = d3ScaleLinear()              // Linear scale for the numbers on the Y-Axis
        .range([height, 0])  // Display the Y-Axis over the complete height - origin is top left corner, so height comes first
        .domain([min, max])
        .nice();
    });

    return yScales;
  }

  getMinDate(data: { [key: string]: TimeSeries[] }): Date {
    return this.getDate(data, d3Min);
  }

  getMaxDate(data: { [key: string]: TimeSeries[] }): Date {
    return this.getDate(data, d3Max);
  }

  private getMaxValue(data: TimeSeries[], dataTrimValue: number): number {
    const maxValue: number = d3Max(data, (dataItem: TimeSeries) => {
      return d3Max(dataItem.values, (point: TimeSeriesPoint) => {
        return point.value;
      });
    });

    return this.getMaxValueInTrimmedData(maxValue, dataTrimValue);
  }

  private getMaxValueInTime(data: TimeSeries[], minDate: Date, maxDate: Date, dataTrimValue: number): number {
    const maxValueInTime = d3Max(data, (dataSeries: TimeSeries) => {
      const valuesInRange = dataSeries.values.filter(value => value.date <= maxDate && value.date >= minDate);
      return d3Max(valuesInRange, (point: TimeSeriesPoint) => {
        return point.value;
      });
    });

    return this.getMaxValueInTrimmedData(maxValueInTime, dataTrimValue);
  }

  private getDate(dataList: { [key: string]: TimeSeries[] }, f: Function): Date {
    return f(Object.keys(dataList), (key: string) => {
      return f(dataList[key], (dataItem: TimeSeries) => {
        return f(dataItem.values, (point: TimeSeriesPoint) => {
          return point.date;
        });
      });
    });
  }

  private getMaxValueInTrimmedData(maxValue: number, dataTrimValue: number): number {
    if (maxValue && dataTrimValue) {
      return Math.min(maxValue, dataTrimValue);
    } else if (maxValue && (dataTrimValue === undefined || dataTrimValue === null)) {
      return maxValue;
    } else if (dataTrimValue && (maxValue === undefined || maxValue === null)) {
      return dataTrimValue;
    } else {
      return 0;
    }
  }
}
