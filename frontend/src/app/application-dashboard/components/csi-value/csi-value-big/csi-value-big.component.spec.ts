import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CsiValueBigComponent } from './csi-value-big.component';

describe('CsiValueBigComponent', () => {
  let component: CsiValueBigComponent;
  let fixture: ComponentFixture<CsiValueBigComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CsiValueBigComponent ]
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
});
