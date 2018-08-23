import {Injectable} from '@angular/core';
import {ThresholdGroup} from '../models/threshold-for-job.model';
import {Threshold} from '../models/threshold.model';
import {BehaviorSubject} from 'rxjs/index';
import {MeasuredEvent} from '../models/measured-event.model';
import {ThresholdRestService} from './threshold-rest.service';

@Injectable({
  providedIn: 'root'
})

export class ThresholdService {
  thresholdGroups$= new BehaviorSubject<ThresholdGroup[]>([]);
  newThresholdGroup$ = new BehaviorSubject<ThresholdGroup>(null);

  constructor(private thresholdRestService: ThresholdRestService) {
  }

  fetchThresholds(jobId: number): void {
    this.thresholdRestService.getThresholdGroups(jobId).subscribe((thresholdGroups: ThresholdGroup[]) => {
      thresholdGroups.forEach(element => {
        element.thresholds.forEach(threshold => {
          threshold.isNew = false;
          threshold.measurand.translationsKey = 'frontend.de.iteratec.isr.measurand.' + threshold.measurand.name; // format from messages.properties
        });
      });
      this.thresholdGroups$.next(thresholdGroups);
    });
  }

  updateThreshold(updatedThreshold: Threshold): void {
    this.thresholdRestService.updateThreshold(updatedThreshold).subscribe(() => {
      const newThresholdGroups = this.thresholdGroups$.getValue().map(thresholdGroup => {
        if (thresholdGroup.measuredEvent.id === updatedThreshold.measuredEvent.id) {
          return this.replaceThresholdInGroup(thresholdGroup, updatedThreshold);
        } else {
          return thresholdGroup;
        }
      });
      this.thresholdGroups$.next(newThresholdGroups);
    });
  }


  deleteThreshold(deletedThreshold: Threshold): void {
    this.thresholdRestService.deleteThreshold(deletedThreshold).subscribe(() => {
      const newThresholdGroups = this.thresholdGroups$.getValue().map(thresholdGroup => {
        if (thresholdGroup.measuredEvent.id === deletedThreshold.measuredEvent.id) {
          return {
            ...thresholdGroup,
            thresholds: thresholdGroup.thresholds.filter(threshold => threshold.id != deletedThreshold.id)
          };
        } else {
          return thresholdGroup;
        }
      }).filter(thresholdGroup => !!thresholdGroup.thresholds.length);
      this.thresholdGroups$.next(newThresholdGroups);
    });
  }

  addThreshold(newThreshold: Threshold) {
    this.thresholdRestService.addThreshold(newThreshold).subscribe(id => {
      newThreshold.id = id;
      const thresholdGroups = [...this.thresholdGroups$.getValue()];
      const thresholdGroupIndex = thresholdGroups.findIndex(thresholdGroup =>
        thresholdGroup.measuredEvent.id == newThreshold.measuredEvent.id
      );

      newThreshold.isNew = false;
      if (thresholdGroupIndex < 0) {
        this.insertThresholdGroup(thresholdGroups, newThreshold);
        this.newThresholdGroup$.next(null);
      } else {
        this.addThresholdToGroup(thresholdGroups, thresholdGroupIndex, newThreshold);
      }
      this.thresholdGroups$.next(thresholdGroups);
    });
  }

  createNewThresholdGroup(measuredEvent: MeasuredEvent) {
    const newThresholdGroup: ThresholdGroup = {
      thresholds: [],
      measuredEvent: measuredEvent,
      isNew: true
    };
    this.newThresholdGroup$.next(newThresholdGroup);
  }

  cancelNew(threshold: Threshold) {
    const newThresholdGroup = this.newThresholdGroup$.getValue();
    if (newThresholdGroup && newThresholdGroup.measuredEvent == threshold.measuredEvent) {
      this.newThresholdGroup$.next(null);
    }
  }

  downloadScript(jobId: number) {
    this.thresholdRestService.getScript().subscribe(
      result => this.download(result, jobId)
    );
  }

  private replaceThresholdInGroup(thresholdGroup: ThresholdGroup, updatedThreshold: Threshold): ThresholdGroup {
    const newThresholds = thresholdGroup.thresholds.map(threshold => {
      return threshold.id == updatedThreshold.id ? updatedThreshold : threshold;
    });
    return {
      ...thresholdGroup,
      thresholds: newThresholds
    };
  }

  private addThresholdToGroup(thresholdGroups, thresholdGroupIndex, newThreshold: Threshold) {
    const thresholdGroup = thresholdGroups[thresholdGroupIndex];
    thresholdGroups[thresholdGroupIndex] = {
      ...thresholdGroup,
      thresholds: [...thresholdGroup.thresholds, newThreshold]
    }
  }

  private insertThresholdGroup(thresholdGroups, newThreshold: Threshold) {
    thresholdGroups.push({
      measuredEvent: newThreshold.measuredEvent,
      thresholds: [newThreshold],
      isNew: false
    });
  }

  private download(data: string, jobId: number) {
    let fileName = 'CI_Script_' + jobId + '.groovy';
    let blob = new Blob([data], {type: 'text/plain;charset=utf-8'});
    this.saveData(blob, fileName);
  }

  private saveData(blob, fileName) {
    let a = document.createElement('a');
    document.body.appendChild(a);
    let url = window.URL.createObjectURL(blob);
    a.href = url;
    a.download = fileName;
    a.click();
    window.URL.revokeObjectURL(url);
  }
}
