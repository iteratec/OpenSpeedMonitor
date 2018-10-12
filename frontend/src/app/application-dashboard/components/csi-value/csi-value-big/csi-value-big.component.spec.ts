import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CsiValueBigComponent} from './csi-value-big.component';
import {CalculationUtil} from "../../../../shared/utils/calculation.util";
import {CsiValueBaseComponent} from "../csi-value-base.component";
import {TranslateModule} from "@ngx-translate/core";

describe('CsiValueBigComponent', () => {
  let component: CsiValueBigComponent;
  let fixture: ComponentFixture<CsiValueBigComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [CsiValueBigComponent, CsiValueBaseComponent],
      imports: [
        TranslateModule.forRoot()
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CsiValueBigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('description should state "today" if value is from today', () => {
    component.csiDate = new Date().toISOString().substring(0, 10);
    component.ngOnInit();

    expect(component.description).toEqual("today");
    fixture.detectChanges();
    const descriptionEl: HTMLElement = fixture.nativeElement.querySelector('.csi-value-description');
    expect(descriptionEl.textContent).toEqual("today");
  });

  it('description should state date if value is not from today', () => {
    component.csiDate = new Date(new Date().setDate(new Date().getDate() - 1)).toISOString().substring(0, 10);
    component.ngOnInit();

    expect(component.description).toBe(CalculationUtil.toGermanDateFormat(component.csiDate));
    fixture.detectChanges();
    const descriptionEl: HTMLElement = fixture.nativeElement.querySelector('.csi-value-description');
    expect(descriptionEl.textContent).toEqual(CalculationUtil.toGermanDateFormat(component.csiDate));
  });

  it('should be N/A if no value is there', () => {
    component.csiValue = null;
    component.ngOnInit();
    expect(component.formattedCsiValue).toEqual("n/a");

    fixture.detectChanges();
    const descriptionEl: HTMLElement = fixture.nativeElement.querySelector('.csi-value-text');
    expect(descriptionEl.textContent).toEqual('n/a');
  });

  it('should be described as "loading..." if it is loading', () => {
    component.showLoading = true;
    component.ngOnInit();

    expect(component.formattedCsiValue).toEqual("loading...");

    fixture.detectChanges();
    const descriptionEl: HTMLElement = fixture.nativeElement.querySelector('.csi-value-text');
    expect(descriptionEl.textContent).toEqual('loading...');
  });
});
