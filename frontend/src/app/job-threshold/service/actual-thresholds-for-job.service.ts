import { Injectable } from '@angular/core';
import {MeasuredEvent} from "./model/measured-event.model";
import {ThresholdForJob} from "./model/threshold-for-job.model";
import {Measurand} from './model/measurand.model'
import {Threshold} from "./model/threshold.model";
import {Observable, ObservableLike, Subject} from "rxjs/index";
import {ActualMeasuredEventsService} from "./actual-measured-events.service";
import {log} from "util";

@Injectable({
  providedIn: 'root'
})
export class ActualThresholdsForJobService {

  thresholdsforJobList: ThresholdForJob[];
  actualThresholdsforJobList$= new Subject<ThresholdForJob[]>();


  constructor() { }

  setActualThresholdsforJobList(thresholdsforJob: ThresholdForJob[]) {
    console.log("SERVICE setActualThresholdsforJobList");
    /*State Initialization*/
    thresholdsforJob.map(element => {
      element.measuredEvent.state = "normal";
      element.thresholds.map(threshold => {
        threshold.state = "normal";
      })
    });

    this.thresholdsforJobList = thresholdsforJob;
    this.actualThresholdsforJobList$.next(this.thresholdsforJobList);
  }

  getActualThresholdsforJob() {
    return this.thresholdsforJobList;
  }

  editThresholdOfActualThresholdsforJob(thresholdId: number, editedThreshold:Threshold) {
    let index: number;
    /*this.thresholdsforJobList.map(thresholdForJob => {
      index = thresholdForJob.thresholds.map(threshold => threshold.id).indexOf(threshold.id)
    })*/
    this.thresholdsforJobList.map(thresholdForJob => {
      thresholdForJob.thresholds[thresholdForJob.thresholds.indexOf(editedThreshold)] = editedThreshold;
    })

  }

  deleteFromActualThresholdsforJob(deletedThreshold: Threshold) {
    let thresholdIndex: number;
    let thresholdForJobIndex: number;
    console.log("SERVICE deleteFromActualThresholdsforJob thresholdId: " + deletedThreshold.id);
    console.log("SERVICE deleteFromActualThresholdsforJob threshold: "+ JSON.stringify(deletedThreshold));

    this.thresholdsforJobList.map(thresholdForJob => {
      if (deletedThreshold.measuredEvent.id==thresholdForJob.measuredEvent.id) {
        thresholdIndex= thresholdForJob.thresholds.findIndex((threshold => threshold.id == deletedThreshold.id))
      }
    });
    thresholdForJobIndex = this.thresholdsforJobList.findIndex((thresholdForJob => thresholdForJob.measuredEvent.id == deletedThreshold.measuredEvent.id));

    //If is the last threshold of the measure
    if (this.thresholdsforJobList[thresholdForJobIndex].thresholds.length >1) {
      this.thresholdsforJobList[ thresholdForJobIndex].thresholds.splice( thresholdIndex, 1);
      this.actualThresholdsforJobList$.next(this.thresholdsforJobList);
    }else {
      this.thresholdsforJobList.splice(thresholdForJobIndex, 1);
    }
  }

  addToActualThresholdsforJob(measuredEventId: number, addedThreshold: Threshold) {
    console.log("SERVICE addToActualThresholdsforJob addedThreshold.measuredEvent.state: " + addedThreshold.measuredEvent.state);
    console.log("SERVICE addToActualThresholdsforJob addedThreshold id: "+ addedThreshold.id);
    let index: number;

    //If a new Measure was added
    if(addedThreshold.measuredEvent.state=="new") {
      let newThresholdForJob = {} as ThresholdForJob;
      addedThreshold.state = "normal";
      addedThreshold.measuredEvent.state= "normal";
      let newMeasuredEvent = addedThreshold.measuredEvent;
      newMeasuredEvent.state = "normal"; //?
      newThresholdForJob.measuredEvent = newMeasuredEvent;
      newThresholdForJob.thresholds = [];
      newThresholdForJob.thresholds.push(addedThreshold);
      console.log("SERVICE newThresholdForJob: "+ JSON.stringify(newThresholdForJob));
      this.thresholdsforJobList[this.thresholdsforJobList.length -1] = newThresholdForJob;

      //If a new Threshold was added
    } else {
      addedThreshold.state = "normal";
      addedThreshold.measuredEvent.state= "normal";
      this.thresholdsforJobList.map(thresholdforJob => {
        if (thresholdforJob.measuredEvent.id == measuredEventId) {
          index = thresholdforJob.thresholds.findIndex((threshold => threshold.id == addedThreshold.id));
          thresholdforJob.thresholds[index] = addedThreshold;
        }
      });
    }
    console.log("SERVICE addToActualThresholdsforJob: " + JSON.stringify(this.thresholdsforJobList));
    this.actualThresholdsforJobList$.next(this.thresholdsforJobList);

  }
}
