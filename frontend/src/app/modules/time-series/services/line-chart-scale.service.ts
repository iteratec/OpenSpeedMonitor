import {Injectable} from '@angular/core';
import {TimeSeries} from '../models/time-series.model';
import {
  scaleLinear as d3ScaleLinear,
  ScaleLinear as D3ScaleLinear,
  scaleTime as d3ScaleTime,
  ScaleTime as D3ScaleTime
} from 'd3-scale';
import {TimeSeriesPoint} from '../models/time-series-point.model';
import {max as d3Max, min as d3Min} from 'd3-array';

@Injectable({
  providedIn: 'root'
})
export class LineChartScaleService {

  constructor() {
  }

  public getMinDate(data: TimeSeries[][]): Date {
    return this.getDate(data, d3Min);
  }

  public getMaxDate(data: TimeSeries[][]): Date {
    return this.getDate(data, d3Max);
  }

  /**
   * Determine the xScale for the given data
   */
  public getXScale(data: TimeSeries[][], width: number): D3ScaleTime<number, number> {
    return d3ScaleTime()               // Define a scale for the X-Axis
      .range([0, width])  // Display the X-Axis over the complete width
      .domain([this.getMinDate(data), this.getMaxDate(data)]);
  }

  /**
   * Determine the yScales for the given data
   */
  public getYScales(dataList: TimeSeries[][], height: number): D3ScaleLinear<number, number>[] {
    const yScales: D3ScaleLinear<number, number>[] = [];
    dataList.forEach((data: TimeSeries[]) => {
      const yScale: D3ScaleLinear<number, number> = d3ScaleLinear()              // Linear scale for the numbers on the Y-Axis
        .range([height, 0])  // Display the Y-Axis over the complete height - origin is top left corner, so height comes first
        .domain([0, this.getMaxValue(data)])
        .nice();
      yScales.push(yScale);
    });

    return yScales;
  }

  /**
   * Determine the yScales for the given data in time range
   */
  public getYScalesInRange(dataList: TimeSeries[][], minDate: Date, maxDate: Date, height: number): D3ScaleLinear<number, number>[] {
    const yScales: D3ScaleLinear<number, number>[] = [];
    dataList.forEach((data: TimeSeries[]) => {
      const yScale: D3ScaleLinear<number, number> = d3ScaleLinear()              // Linear scale for the numbers on the Y-Axis
        .range([height, 0])  // Display the Y-Axis over the complete height - origin is top left corner, so height comes first
        .domain([0, this.getMaxValueInTime(data, minDate, maxDate)])
        .nice();
      yScales.push(yScale);
    });

    return yScales;
  }

  private getDate(dataList: TimeSeries[][], f: Function): Date {
    return f(dataList, (data: TimeSeries[]) => {
      return f(data, (dataItem: TimeSeries) => {
        return f(dataItem.values, (point: TimeSeriesPoint) => {
          return point.date;
        });
      });
    });
  }

  private getMaxValue(data: TimeSeries[]): number {
    return d3Max(data, (dataItem: TimeSeries) => {
      return d3Max(dataItem.values, (point: TimeSeriesPoint) => {
        return point.value;
      });
    });
  }

  private getMaxValueInTime(data: TimeSeries[], minDate: Date, maxDate: Date): number {
    return d3Max(data, (dataSeries: TimeSeries) => {
      const valuesInRange = dataSeries.values.filter(value => value.date <= maxDate && value.date >= minDate);
      return d3Max(valuesInRange, (point: TimeSeriesPoint) => {
        return point.value;
      });
    });
  }
}
