import {Injectable} from "@angular/core";
import {ThresholdForJob} from "./model/threshold-for-job.model";
import {Threshold} from "./model/threshold.model";
import {Subject} from "rxjs/index";

@Injectable({
  providedIn: 'root'
})

export class ActualThresholdsForJobService {

  thresholdsforJobList: ThresholdForJob[];
  actualThresholdsforJobList$= new Subject<ThresholdForJob[]>();


  constructor() { }

  setActualThresholdsforJobList(thresholdsforJob: ThresholdForJob[]) {
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
    this.thresholdsforJobList.map(thresholdForJob => {
      thresholdForJob.thresholds[thresholdForJob.thresholds.indexOf(editedThreshold)] = editedThreshold;
    })

  }

  deleteFromActualThresholdsforJob(deletedThreshold: Threshold) {
    let thresholdIndex: number;
    let thresholdForJobIndex: number;

    this.thresholdsforJobList.map(thresholdForJob => {
      if (deletedThreshold.measuredEvent.id==thresholdForJob.measuredEvent.id) {
        thresholdIndex= thresholdForJob.thresholds.findIndex((threshold => threshold.id == deletedThreshold.id))
      }
    });
    thresholdForJobIndex = this.thresholdsforJobList.findIndex((thresholdForJob => thresholdForJob.measuredEvent.id == deletedThreshold.measuredEvent.id));

    /*If is the last threshold of the measure*/
    if (this.thresholdsforJobList[thresholdForJobIndex].thresholds.length >1) {
      this.thresholdsforJobList[ thresholdForJobIndex].thresholds.splice( thresholdIndex, 1);
      this.actualThresholdsforJobList$.next(this.thresholdsforJobList);
    }else {
      this.thresholdsforJobList.splice(thresholdForJobIndex, 1);
    }
  }

  addToActualThresholdsforJob(measuredEventId: number, addedThreshold: Threshold) {
    let index: number;

    /*New Measure was added*/
    if(addedThreshold.measuredEvent.state=="new") {
      let newThresholdForJob = {} as ThresholdForJob;
      addedThreshold.state = "normal";
      addedThreshold.measuredEvent.state= "normal";
      let newMeasuredEvent = addedThreshold.measuredEvent;
      newMeasuredEvent.state = "normal"; //?
      newThresholdForJob.measuredEvent = newMeasuredEvent;
      newThresholdForJob.thresholds = [];
      newThresholdForJob.thresholds.push(addedThreshold);
      this.thresholdsforJobList[this.thresholdsforJobList.length -1] = newThresholdForJob;

      /*New Threshold was added*/
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
    this.actualThresholdsforJobList$.next(this.thresholdsforJobList);

  }
}
