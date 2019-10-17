import { Injectable } from '@angular/core';
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class SpinnerService {

  activeSpinner$ = new BehaviorSubject<Set<string>>(new Set<string>());

  constructor() { }

  showSpinner(spinnerId: string): void {
    let activeSpinner: Set<string> = this.activeSpinner$.getValue();
    activeSpinner.add(spinnerId);
    this.activeSpinner$.next(activeSpinner);
  }

  hideSpinner(spinnerId: string): void {
    let activeSpinner: Set<string> = this.activeSpinner$.getValue();
    activeSpinner.delete(spinnerId);
    this.activeSpinner$.next(activeSpinner);
  }
}
