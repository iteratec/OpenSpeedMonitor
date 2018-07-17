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
    this.thresholdsforJobList.map(thresholdForJob => {
      if (deletedThreshold.measuredEvent.id==thresholdForJob.measuredEvent.id) {
        thresholdIndex= thresholdForJob.thresholds.findIndex((threshold => threshold.id == deletedThreshold.id))
      }
    });
    thresholdForJobIndex = this.thresholdsforJobList.findIndex((thresholdForJob => thresholdForJob.measuredEvent.id == deletedThreshold.measuredEvent.id));

    //last threshold ?
    if (this.thresholdsforJobList[thresholdForJobIndex].thresholds.length >1) {
      this.thresholdsforJobList[ thresholdForJobIndex].thresholds.splice( thresholdIndex, 1);
      this.actualThresholdsforJobList$.next(this.thresholdsforJobList);
    }else {
      this.thresholdsforJobList.splice(thresholdForJobIndex, 1);
    }
  }

 /* deleteMeasureFromActualThresholdsforJob(deletedThreshold: Threshold) {
    console.log("SERVICE deleteMeasureFromActualThresholdsforJob thresholdId: " + deletedThreshold.id);
    let thresholdForJobIndex: number;
    thresholdForJobIndex= this.thresholdsforJobList.findIndex((thresholdForJob => thresholdForJob.measuredEvent.id == deletedThreshold.measuredEvent.id));
    this.thresholdsforJobList.splice(thresholdForJobIndex, 1);
  }*/

  addThresholdToActualThresholdsforJob(measuredEventId: number, addedThreshold: Threshold) {
    console.log("SERVICE addThresholdToActualThresholdsforJob");
    addedThreshold.state = "normal";
    addedThreshold.measuredEvent.state= "normal";

    this.thresholdsforJobList.map(thresholdforJob => {
      if (thresholdforJob.measuredEvent.id == measuredEventId) {
        let index = thresholdforJob.thresholds.findIndex((threshold => threshold.id == addedThreshold.id));
        thresholdforJob.thresholds[index] = addedThreshold;
      }
    })
    this.actualThresholdsforJobList$.next(this.thresholdsforJobList);
  }

  addThresholdForJobToActualThresholdsforJob(measuredEventId: number, threshold:Threshold) {
    console.log("SERVICE addThresholdForJobToActualThresholdsforJob");
    let newThresholdForJob = {} as ThresholdForJob;
    threshold.state = "normal";
    threshold.measuredEvent.state= "normal";
    let newMeasuredEvent = threshold.measuredEvent;

    newThresholdForJob.measuredEvent = newMeasuredEvent;
    newThresholdForJob.thresholds = [];
    newThresholdForJob.thresholds.push(threshold);

    let index = this.thresholdsforJobList.findIndex((thresholdForJob => thresholdForJob.measuredEvent.id == threshold.id));

    this.thresholdsforJobList.map(thresholdForJob => {
      if(thresholdForJob.measuredEvent.id == measuredEventId) {
        this.thresholdsforJobList[index] = newThresholdForJob;
      }
    });

    //this.thresholdsforJobList.push(newThresholdForJob);
    this.actualThresholdsforJobList$.next(this.thresholdsforJobList);
  }

}
