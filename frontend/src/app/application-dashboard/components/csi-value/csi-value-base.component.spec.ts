import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CsiValueBaseComponent} from './csi-value-base.component';
import {DebugElement} from '@angular/core';
import {By} from '@angular/platform-browser';
import {TranslateModule} from '@ngx-translate/core';

describe('CsiValueBaseComponent', () => {
  let component: CsiValueBaseComponent;
  let fixture: ComponentFixture<CsiValueBaseComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [CsiValueBaseComponent],
      imports: [
        TranslateModule.forRoot()
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CsiValueBaseComponent);
    component = fixture.componentInstance;
    component.csiValue = 0;
    component.ngOnInit();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should be bad if csi value is bad', () => {
    const badValue: number = 69.4;
    component.csiValue =  badValue;
    component.ngOnInit();
    expect(component.csiValueClass).toEqual('bad');

    fixture.detectChanges();
    const circleDe: DebugElement = fixture.debugElement;
    const svgDe: DebugElement = circleDe.query(By.css('svg'));
    expect(svgDe.classes.bad).toBeTruthy();
    expect(svgDe.classes.okay).toBeFalsy();
    expect(svgDe.classes.good).toBeFalsy();
  });
  it('should be okay if csi value is okay', () => {
    const okayValue: number = 70;
    component.csiValue =  okayValue;
    component.ngOnInit();
    expect(component.csiValueClass).toEqual('okay');

    fixture.detectChanges();
    const circleDe: DebugElement = fixture.debugElement;
    const svgDe: DebugElement = circleDe.query(By.css('svg'));
    expect(svgDe.classes.bad).toBeFalsy();
    expect(svgDe.classes.okay).toBeTruthy();
    expect(svgDe.classes.good).toBeFalsy();
  });
  it('should be good if csi value is good', () => {
    const goodValue: number = 90;
    component.csiValue =  goodValue;
    component.ngOnInit();
    expect(component.csiValueClass).toEqual('good');

    fixture.detectChanges();
    const circleDe: DebugElement = fixture.debugElement;
    const svgDe: DebugElement = circleDe.query(By.css('svg'));
    expect(svgDe.classes.bad).toBeFalsy();
    expect(svgDe.classes.okay).toBeFalsy();
    expect(svgDe.classes.good).toBeTruthy();
  });
  it('check if padding is applied', () => {
    component.circleDiameter = 150;
    component.ngOnInit();
    const expectedSize: number = 160;
    fixture.detectChanges();

    expect(component.size).toBe(expectedSize);
    const containerEl: HTMLElement = fixture.nativeElement.querySelector('svg');
    expect(containerEl.clientWidth).toBe(expectedSize);
  });
  it('should be grey if the csi is outdated', () => {
    const goodValue: number = 90;
    component.csiValue = goodValue;
    component.csiDate = new Date("01.02.2017").toISOString();
    component.lastResultDate = new Date("01.03.2017").toISOString();
    component.ngOnInit();

    expect(component.isOutdated).toBe(true);
    expect(component.csiValueClass).toEqual('neutral');

    fixture.detectChanges();
    const circleDe: DebugElement = fixture.debugElement;
    const svgDe: DebugElement = circleDe.query(By.css('svg'));
    expect(svgDe.classes.bad).toBeFalsy();
    expect(svgDe.classes.okay).toBeFalsy();
    expect(svgDe.classes.good).toBeFalsy();
    expect(svgDe.classes.neutral).toBeTruthy();
  });
  it('should be good if csi value is good and up-to-date', () => {
    const goodValue: number = 90;
    component.csiValue = goodValue;
    component.csiDate = new Date("01.02.2017").toISOString();
    component.lastResultDate = new Date("01.02.2017").toISOString();
    component.ngOnInit();

    expect(component.isOutdated).toBe(false);
    expect(component.csiValueClass).toEqual('good');

    fixture.detectChanges();
    const circleDe: DebugElement = fixture.debugElement;
    const svgDe: DebugElement = circleDe.query(By.css('svg'));
    expect(svgDe.classes.bad).toBeFalsy();
    expect(svgDe.classes.okay).toBeFalsy();
    expect(svgDe.classes.good).toBeTruthy();
    expect(svgDe.classes.neutral).toBeFalsy();
  });
  it('should be described by "CSI" and be "outdated" (grey) if the csi value is loading', () => {
    component.showLoading = true;
    component.ngOnInit();
    fixture.detectChanges();

    const circleDe: DebugElement = fixture.debugElement;
    const svgDe: DebugElement = circleDe.query(By.css('svg'));
    expect(svgDe.classes.bad).toBeFalsy();
    expect(svgDe.classes.okay).toBeFalsy();
    expect(svgDe.classes.good).toBeFalsy();
    expect(svgDe.classes.neutral).toBeTruthy();
  });

  it('should be N/A if no value is there', () => {
    component.csiValue = null;
    component.ngOnInit();
    expect(component.isNA).toBe(true);

    fixture.detectChanges();
    const circleDe: DebugElement = fixture.debugElement;
    const svgDe: DebugElement = circleDe.query(By.css('svg'));
    expect(svgDe.classes.bad).toBeFalsy();
    expect(svgDe.classes.okay).toBeFalsy();
    expect(svgDe.classes.good).toBeFalsy();
    expect(svgDe.classes.neutral).toBeTruthy();
  });
});
