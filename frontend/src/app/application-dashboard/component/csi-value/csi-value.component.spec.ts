import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CsiValueComponent } from './csi-value.component';

describe('CsiValueComponent', () => {
  let component: CsiValueComponent;
  let fixture: ComponentFixture<CsiValueComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CsiValueComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CsiValueComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
