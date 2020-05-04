import {DistributionDTO} from './distribution.model';

export interface DistributionDataDTO {
  i18nMap: {};
  series: DistributionDTO[];
  sortingRules: any;
}

export class DistributionData implements DistributionDataDTO {
  i18nMap: {};
  series: DistributionDTO[];
  sortingRules: any;

  constructor() {
    this.i18nMap = {};
    this.series = [];
    this.sortingRules = {};
  }
}
