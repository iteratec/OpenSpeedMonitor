import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CsiValueMediumComponent } from './csi-value-medium.component';

describe('CsiValueMediumComponent', () => {
  let component: CsiValueMediumComponent;
  let fixture: ComponentFixture<CsiValueMediumComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CsiValueMediumComponent ]
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
