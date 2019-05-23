import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AspectMetricsComponent} from './aspect-metrics.component';
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";

describe('AspectMetricsComponent', () => {
  let component: AspectMetricsComponent;
  let fixture: ComponentFixture<AspectMetricsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        AspectMetricsComponent
      ],
      imports: [
        SharedMocksModule,
        FormsModule,
        ReactiveFormsModule
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AspectMetricsComponent);
    component = fixture.componentInstance;
    component.aspects = [];
    component.aspectType = {name: '', icon: ''};
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
