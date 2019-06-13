import {Component, Input} from '@angular/core';
import {Unit} from "../../../../enums/unit.enum";
import {PerformanceAspectType} from "../../../../models/perfomance-aspect.model";

@Component({
  selector: 'osm-page-metric',
  templateUrl: './page-metric.component.html',
  styleUrls: ['./page-metric.component.scss']
})

export class PageMetricComponent {
  @Input() metric: PerformanceAspectType;
  @Input() value: string;

  Unit: typeof Unit = Unit;

  public isAvailable(): boolean {
    return this.value !== '';
  }

  determineUnit(unit: string): string {
    if (unit === 'ms') {
      return Unit.SECONDS
    }
  }
}
