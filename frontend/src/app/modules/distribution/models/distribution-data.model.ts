import {DistributionDTO} from './distribution.model';

export interface DistributionDataDTO {
  series: DistributionDTO[];
  measurandGroup: string;
  dimensionalUnit: string;
  sortingRules: {desc: number[], asc: number[]};
  filterRules: {[key: string]: string[]};
}

export class DistributionData implements DistributionDataDTO {
  series: DistributionDTO[];
  measurandGroup: string;
  dimensionalUnit: string;
  sortingRules: {desc: number[], asc: number[]};
  filterRules: {[key: string]: string[]};

  constructor() {
    this.series = [];
    this.measurandGroup = '';
    this.dimensionalUnit = '';
    this.sortingRules = {desc: [], asc: []};
    this.filterRules = {};
  }
}
