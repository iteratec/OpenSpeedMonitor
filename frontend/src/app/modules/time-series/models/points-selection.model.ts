import {TimeSeriesPoint} from './time-series-point.model';

export class PointsSelection {

  private selectedPoints: TimeSeriesPoint[] = [];

  public unselectAll() {
    this.selectedPoints = [];
  }

  public selectPoint(pointToSelect: TimeSeriesPoint) {
    this.selectedPoints.push(pointToSelect);
  }

  public isPointSelected(pointToCheck: TimeSeriesPoint): boolean {
    return this.selectedPoints.some(elem => elem.equals(pointToCheck));
  }

  public unselectPoint(pointToSelect: TimeSeriesPoint) {
    this.selectedPoints = this.selectedPoints.filter(elem => !elem.equals(pointToSelect));
  }

  public count(): number {
    return this.selectedPoints.length;
  }

  public getAll(): TimeSeriesPoint[] {
    return this.selectedPoints;
  }

  public getFirst(): TimeSeriesPoint {
    if (this.count() === 0) {
      return null;
    }
    return this.selectedPoints[0];
  }
}
