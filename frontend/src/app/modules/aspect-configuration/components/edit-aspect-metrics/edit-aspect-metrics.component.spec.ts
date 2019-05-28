import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {EditAspectMetricsComponent} from './edit-aspect-metrics.component';
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {ApplicationService} from "../../../../services/application.service";
import {AspectConfigurationService} from "../../services/aspect-configuration.service";
import {AspectMetricsComponent} from "../aspect-metrics/aspect-metrics.component";
import {BehaviorSubject, ReplaySubject} from "rxjs";
import {Application} from "../../../../models/application.model";
import {Page} from "../../../../models/page.model";
import {PerformanceAspect} from "../../../../models/perfomance-aspect.model";

fdescribe('EditAspectMetricsComponent', () => {
  let component: EditAspectMetricsComponent;
  let fixture: ComponentFixture<EditAspectMetricsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        EditAspectMetricsComponent,
        AspectMetricsComponent
      ],
      providers: [
        EditAspectMetricsComponent,
        {
          provide: ApplicationService,
          useValue: {
            selectedApplication$: new ReplaySubject<Application>(1),
            selectedPage$: new ReplaySubject<Page>(1),
            performanceAspectsForPage$: new BehaviorSubject<PerformanceAspect[]>([])
          }
        },
        AspectConfigurationService
      ],
      imports: [
        SharedMocksModule,
        FormsModule,
        ReactiveFormsModule
      ],
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditAspectMetricsComponent);
    component = fixture.componentInstance;
    component.aspectType = {name: 'aspect', icon: 'aspect-icon'};
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
