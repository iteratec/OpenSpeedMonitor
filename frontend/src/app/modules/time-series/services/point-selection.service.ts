import { Injectable } from '@angular/core';
import {TimeSeriesPoint} from "../models/time-series-point.model";

@Injectable({
  providedIn: 'root'
})
export class PointSelectionService {

  private selectedPoints: TimeSeriesPoint[] = [];

  public unselectAllPoints() {
    this.selectedPoints = [];
  }

  public selectPoint(pointToSelect: TimeSeriesPoint) {
    this.selectedPoints.push(pointToSelect);
  }

  public isPointSelected(pointToCheck: TimeSeriesPoint): boolean {
    return this.selectedPoints.some(elem => elem.equals(pointToCheck));
  }

  public unselectPoint(pointToSelect: TimeSeriesPoint) {
    this.selectedPoints = this.selectedPoints.filter(elem => !elem.equals(pointToSelect))
  }

  public countSelectedDots() {
    return this.selectedPoints.length;
  }
}
