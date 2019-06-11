import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {AspectConfigurationComponent} from './aspect-configuration.component';
import {SharedMocksModule} from "../../testing/shared-mocks.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AspectMetricsComponent} from "./components/aspect-metrics/aspect-metrics.component";
import {ApplicationService} from "../../services/application.service";
import {AspectConfigurationService} from "./services/aspect-configuration.service";

describe('AspectConfigurationComponent', () => {
  let component: AspectConfigurationComponent;
  let fixture: ComponentFixture<AspectConfigurationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        AspectConfigurationComponent,
        AspectMetricsComponent
      ],
      imports: [
        SharedMocksModule,
        FormsModule,
        ReactiveFormsModule
      ],
      providers: [
        AspectConfigurationComponent,
        ApplicationService,
        AspectConfigurationService
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AspectConfigurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

});
