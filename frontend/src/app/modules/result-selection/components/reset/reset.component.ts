import {Component, OnInit} from '@angular/core';
import {ResultSelectionStore} from '../../services/result-selection.store';

@Component({
  selector: 'osm-result-selection-reset',
  templateUrl: './reset.component.html',
  styleUrls: ['./reset.component.scss']
})
export class ResetComponent implements OnInit {

  constructor(private resultSelectionStore: ResultSelectionStore) {
  }

  ngOnInit() {
  }

  emitResetEventToComponent() {
    this.resultSelectionStore.setResultSelectionCommand({
      ...this.resultSelectionStore.resultSelectionCommand,
      jobGroupIds: [],
      pageIds: [],
      measuredEventIds: [],
      browserIds: [],
      locationIds: [],
      selectedConnectivities: []
    });
    this.resultSelectionStore.reset$.next();
  }
}
