import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CsiValueMediumComponent} from './csi-value-medium.component';
import {CsiValueBaseComponent} from "../csi-value-base.component";
import {TranslateModule} from "@ngx-translate/core";

describe('CsiValueMediumComponent', () => {
  let component: CsiValueMediumComponent;
  let fixture: ComponentFixture<CsiValueMediumComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [CsiValueMediumComponent, CsiValueBaseComponent],
      imports: [
        TranslateModule.forRoot()
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CsiValueMediumComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
