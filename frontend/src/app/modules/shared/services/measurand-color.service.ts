import {Injectable} from '@angular/core';
import * as d3 from 'd3';
import {scaleOrdinal} from 'd3';

@Injectable({
  providedIn: 'root'
})
export class MeasurandColorService {

  private measurandGroupColorCombination = MeasurandColorService.initColors();

  private static initColors() {
    const loadingTimeColors = [
        '#1660A7',
        '#558BBF',
        '#95b6d7',
        '#d4e2ef'
      ],
      countOfRequestColors = [
        '#E41A1C',
        '#eb5859',
        '#f29697',
        '#fad5d5'
      ],
      sizeOfRequestColors = [
        '#F18F01',
        '#f4ad46',
        '#f8cc8b',
        '#fcead0'
      ],
      csiColors = [
        '#59B87A',
        '#86cb9e',
        '#b3dec2',
        '#e0f2e6'
      ];

    return {
      'ms': loadingTimeColors,
      's': loadingTimeColors,
      '#': countOfRequestColors,
      'KB': sizeOfRequestColors,
      'MB': sizeOfRequestColors,
      '%': csiColors,
      '': loadingTimeColors
    };
  }

  private static createDomain(arrayLength: number): string[] {
    return Array.from({length: arrayLength}, (_, index) => index.toString());
  }

  getColorScaleForMeasurandGroup(measurandUnit: string, skipFirst: boolean = false): d3.ScaleOrdinal<string, any> {
    const colors = this.measurandGroupColorCombination[measurandUnit].slice(skipFirst ? 1 : 0);
    return d3.scaleOrdinal(colors).domain(MeasurandColorService.createDomain(colors.length));
  }

  getColorScaleForTrafficLight() {
    const trafficColors = [
      '#5cb85c',
      '#f0ad4e',
      '#d9534f'
    ];

    return scaleOrdinal()
      .domain(['good', 'ok', 'bad'])
      .range(trafficColors);
  }
}
