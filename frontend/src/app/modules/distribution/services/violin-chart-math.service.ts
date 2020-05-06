import {Injectable} from '@angular/core';
import * as d3 from 'd3';
import {DistributionDTO} from '../models/distribution.model';

@Injectable({
  providedIn: 'root'
})
export class ViolinChartMathService {

  histogramResolutionForTraceData = (() => {
    const getGreatestDomainTrace = (currentSeries: any) => {
      let maxDomainSize = -1;
      let greatestTrace = [];
      currentSeries.forEach(elem => {
        const curTrace = elem.data;
        const domainSize = d3.quantile(curTrace, 0.75) - d3.quantile(curTrace, 0.25);
        if (domainSize > maxDomainSize) {
          maxDomainSize = domainSize;
          greatestTrace = curTrace;
        }
      });
      return greatestTrace;
    };

    return (currentSeries: DistributionDTO[], traceData, mainDataResolution: number) => {
      const greatestDomainTrace = getGreatestDomainTrace(currentSeries);
      const quantile25 = d3.quantile(greatestDomainTrace, 0.25);
      const quantile75 = d3.quantile(greatestDomainTrace, 0.75);
      const binSize = (quantile75 - quantile25) / mainDataResolution;
      return Math.floor((traceData[traceData.length - 1] - traceData[0]) / binSize);
    };
  })();

  constructor() {
  }

  getMaxValue(currentSeries: DistributionDTO[]): number {
    const maxInSeries = currentSeries.map(elem => {
      return Math.max(...elem.data);
    });
    return d3.max(maxInSeries);
  }

  getDomain(maxValue: number, dataTrimValue: number): number[] {
    const trimValue = dataTrimValue || maxValue;

    return [0, Math.min(maxValue, trimValue)];
  }

  calculateMedian(arr: number[]): number {
    if (!arr || arr.length === 0) {
      return 0;
    }

    const arrCopy = [...arr].sort((a, b) => a - b);

    const i = Math.floor(arrCopy.length / 2);
    return (arrCopy.length % 2 === 0) ? arrCopy[i - 1] : (arrCopy[i - 1] + arrCopy[i]) / 2.0;
  }
}
