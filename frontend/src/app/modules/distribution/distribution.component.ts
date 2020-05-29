import {Component, OnInit} from '@angular/core';
import {DistributionDataDTO, DistributionData} from './models/distribution-data.model';
import {URL} from '../../enums/url.enum';
import {ResultSelectionStore} from '../result-selection/services/result-selection.store';
import {BehaviorSubject} from 'rxjs';
import {ViolinchartDataService} from './services/violinchart-data.service';
import {TitleService} from '../../services/title.service';

@Component({
  selector: 'osm-distribution',
  templateUrl: './distribution.component.html',
  styleUrls: ['./distribution.component.scss']
})
export class DistributionComponent implements OnInit {

  showChartCard = false;
  results$ = new BehaviorSubject<DistributionDataDTO>(new DistributionData());

  constructor(private violinChartService: ViolinchartDataService,
              private resultSelectionStore: ResultSelectionStore,
              private titleService: TitleService) {
  }

  ngOnInit() {
    this.titleService.setTitle('frontend.de.iteratec.osm.distribution.title');
  }

  getDistributionChartData() {
    this.results$.next(null);
    this.showChartCard = true;

    this.violinChartService.fetchDistributionData<DistributionDataDTO>(
      this.resultSelectionStore.resultSelectionCommand,
      this.resultSelectionStore.remainingResultSelection,
      URL.DISTRIBUTION_VIOLINCHART_DATA
    ).subscribe(next => {
      this.results$.next(next);
    });
  }
}
