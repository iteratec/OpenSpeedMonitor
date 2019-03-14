import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ResultSelectionApplicationComponent } from './result-selection-application.component';

describe('ResultSelectionApplicationComponent', () => {
  let component: ResultSelectionApplicationComponent;
  let fixture: ComponentFixture<ResultSelectionApplicationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ResultSelectionApplicationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ResultSelectionApplicationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
