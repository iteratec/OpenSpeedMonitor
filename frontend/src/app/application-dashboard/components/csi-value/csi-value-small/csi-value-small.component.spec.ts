import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CsiValueSmallComponent } from './csi-value-small.component';

describe('CsiValueSmallComponent', () => {
  let component: CsiValueSmallComponent;
  let fixture: ComponentFixture<CsiValueSmallComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CsiValueSmallComponent ]
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
});
