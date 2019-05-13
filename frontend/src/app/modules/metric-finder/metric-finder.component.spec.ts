import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MetricFinderComponent } from './metric-finder.component';

describe('MetricFinderComponent', () => {
  let component: MetricFinderComponent;
  let fixture: ComponentFixture<MetricFinderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MetricFinderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MetricFinderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
