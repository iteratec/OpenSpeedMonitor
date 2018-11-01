import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {PageMetricComponent} from './page-metric.component';
import {ApplicationService} from "../../../../services/application.service";
import {By} from "@angular/platform-browser";
import {Unit} from "../../../../enums/unit.enum";
import {DebugElement} from "@angular/core";
import {Metric} from "../../../../enums/metric.enum";
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";

describe('PageMetricComponent', () => {
  let component: PageMetricComponent;
  let fixture: ComponentFixture<PageMetricComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        SharedMocksModule
      ],
      declarations: [
        PageMetricComponent
      ],
      providers: [
        ApplicationService
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PageMetricComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it("should show the value if the value is available", () => {
    const value: string = "2.34";
    const metric: Metric = new Metric("SpeedIndex", Unit.SECONDS, 'far fa-eye');
    component.value = value;
    component.metric = metric;

    expect(component.isAvailable()).toBe(true);

    fixture.detectChanges();
    const descriptionEl: HTMLElement = fixture.debugElement.query(By.css('.metric-value')).nativeElement;
    expect(descriptionEl.textContent).toEqual(value);
    const unitEl: HTMLElement = fixture.debugElement.query(By.css('.metric-unit')).nativeElement;
    expect(unitEl.textContent).toEqual(Unit.SECONDS);
  });
  it("should be 'n/a' and without unit if the value is empty", () => {
    const value: string = "";
    const metric: Metric = new Metric("SpeedIndex", Unit.SECONDS, 'far fa-eye');
    component.value = value;
    component.metric = metric;

    expect(component.isAvailable()).toBe(false);

    fixture.detectChanges();
    const valueEl: HTMLElement = fixture.debugElement.query(By.css('.metric-value')).nativeElement;
    expect(valueEl.textContent).toEqual('n/a');
    const unitEl: DebugElement = fixture.debugElement.query(By.css('.metric-unit'));
    expect(unitEl).toEqual(null);
  });
});
