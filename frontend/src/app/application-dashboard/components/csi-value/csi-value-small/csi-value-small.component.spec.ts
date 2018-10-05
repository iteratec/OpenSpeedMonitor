import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CsiValueSmallComponent} from './csi-value-small.component';
import {CsiValueBaseComponent} from "../csi-value-base.component";
import {TranslateModule} from "@ngx-translate/core";

describe('CsiValueSmallComponent', () => {
  let component: CsiValueSmallComponent;
  let fixture: ComponentFixture<CsiValueSmallComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [CsiValueSmallComponent, CsiValueBaseComponent],
      imports: [
        TranslateModule.forRoot()
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CsiValueSmallComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should be described as "..." if it is loading', () => {
    component.showLoading = true;
    component.ngOnInit();

    expect(component.formattedCsiValue).toEqual("...");

    fixture.detectChanges();
    const descriptionEl: HTMLElement = fixture.nativeElement.querySelector('.csi-value-small');
    expect(descriptionEl.textContent).toEqual('...');
  });
});
