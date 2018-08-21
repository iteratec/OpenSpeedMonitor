import {Injectable} from "@angular/core";
import {ThresholdGroup} from "../models/threshold-for-job.model";
import {Threshold} from "../models/threshold.model";
import {BehaviorSubject} from "rxjs/index";

@Injectable({
  providedIn: 'root'
})

export class ThresholdService {

  thresholdGroups$= new BehaviorSubject<ThresholdGroup[]>([]);

  constructor() { }

  setThresholdGroups(thresholdGroups: ThresholdGroup[]): void {
    /*State Initialization*/
    thresholdGroups.forEach(element => {
      element.measuredEvent.state = "normal";
      element.thresholds.forEach(threshold => {
        threshold.state = "normal";
        threshold.measurand.translationsKey = "frontend.de.iteratec.isr.measurand." + threshold.measurand.name; // format from messages.properties
      })
    });
    this.thresholdGroups$.next(thresholdGroups);
  }

  updateThreshold(updatedThreshold: Threshold): void {
    const newThresholdGroups = this.thresholdGroups$.getValue().map(thresholdGroup => {
      if (thresholdGroup.measuredEvent.id === updatedThreshold.measuredEvent.id){
        return this.replaceThresholdInGroup(thresholdGroup, updatedThreshold);
      } else {
        return thresholdGroup;
      }
    });
    this.thresholdGroups$.next(newThresholdGroups);
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

  deleteFromActualThresholdsforJob(deletedThreshold: Threshold) {
    const newThresholdGroups = this.thresholdGroups$.getValue().map(thresholdGroup => {
      if (thresholdGroup.measuredEvent.id === deletedThreshold.measuredEvent.id) {
        return {
          ...thresholdGroup,
          thresholds: thresholdGroup.thresholds.filter(threshold => threshold.id != deletedThreshold.id)
        }
      } else {
        return thresholdGroup;
      }
    }).filter(thresholdGroup => !!thresholdGroup.thresholds.length);
    this.thresholdGroups$.next(newThresholdGroups);
  }

  addThreshold(newThreshold: Threshold) {
    const thresholdGroups = [...this.thresholdGroups$.getValue()];
    const thresholdGroupIndex = thresholdGroups.findIndex(thresholdGroup =>
       thresholdGroup.measuredEvent.id == newThreshold.measuredEvent.id
    );

    newThreshold.state = "normal";
    newThreshold.measuredEvent.state = "normal";
    if (thresholdGroupIndex < 0) {
      this.insertThresholdGroup(thresholdGroups, newThreshold);
    } else {
      this.addThresholdToGroup(thresholdGroups, thresholdGroupIndex, newThreshold);
    }
    this.thresholdGroups$.next(thresholdGroups);
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
      thresholds: [newThreshold]
    });
  }
}
