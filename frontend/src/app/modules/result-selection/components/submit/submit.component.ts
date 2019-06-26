import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {ResultSelectionStore} from "../../services/result-selection.store";
import {ResultSelectionCommand} from "../../models/result-selection-command.model";
import {RemainingResultSelection} from "../../models/remaing-result-selection.model";

@Component({
  selector: 'osm-result-selection-submit',
  templateUrl: './submit.component.html',
  styleUrls: ['./submit.component.scss']
})
export class SubmitComponent implements OnInit {

  applicationsSelected$: BehaviorSubject<boolean> = new BehaviorSubject(false);
  pagesSelected$: BehaviorSubject<boolean> = new BehaviorSubject(false);
  measuredEventsSelected$: BehaviorSubject<boolean> = new BehaviorSubject(false);
  measurandsSelected$: BehaviorSubject<boolean> = new BehaviorSubject(false);
  resultCount$: Observable<number>;

  @Input() applicationsRequired: boolean = false;
  @Input() pagesRequired: boolean = false;
  @Input() measurandsRequired: boolean = false;
  @Output() submit: EventEmitter<void> = new EventEmitter<void>();

  constructor(private resultSelectionStore: ResultSelectionStore) {
    this.resultCount$ = this.resultSelectionStore.resultCount$;
  }

  ngOnInit() {
    this.resultSelectionStore._resultSelectionCommand$.subscribe((next: ResultSelectionCommand) => {
      if (next.jobGroupIds) {
        this.applicationsSelected$.next(next.jobGroupIds.length > 0);
      }
      if (next.pageIds) {
        this.pagesSelected$.next(next.pageIds.length > 0);
      }
      if (next.measuredEventIds) {
        this.measuredEventsSelected$.next(next.measuredEventIds.length > 0);
      }
    });
    this.resultSelectionStore._remainingResultSelection$.subscribe((next: RemainingResultSelection) => {
      if (next.measurands) {
        this.measurandsSelected$.next(next.measurands.length > 0);
      }
    });
  }

  show(): void {
    this.submit.emit();
  }
}