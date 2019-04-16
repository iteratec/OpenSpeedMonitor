import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable()
export class sharedService {

  private dateSource= new BehaviorSubject<Date[]>([new Date(),new Date()]);
  currentMessage = this.dateSource.asObservable();

  constructor() { 

  }

  change(selectedDates: Date[]) {
    this.dateSource.next(selectedDates);
  }

}