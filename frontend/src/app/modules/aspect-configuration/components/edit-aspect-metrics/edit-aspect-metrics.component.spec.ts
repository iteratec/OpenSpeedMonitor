import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EditAspectMetricsComponent } from './edit-aspect-metrics.component';

describe('EditAspectMetricsComponent', () => {
  let component: EditAspectMetricsComponent;
  let fixture: ComponentFixture<EditAspectMetricsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EditAspectMetricsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditAspectMetricsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
