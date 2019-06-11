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
import {MetricFinderComponent} from "../../../metric-finder/metric-finder.component";
import {LineChartComponent} from "../../../metric-finder/components/line-chart/line-chart.component";
import {ComparableFilmstripsComponent} from "../../../metric-finder/components/comparable-filmstrips/comparable-filmstrips.component";
import {MetricSelectionComponent} from "../../../metric-finder/components/metric-selection/metric-selection.component";
import {FilmstripComponent} from "../../../metric-finder/components/filmstrip/filmstrip.component";
import {MetricFinderService} from "../../../metric-finder/services/metric-finder.service";
import {FilmstripService} from "../../../metric-finder/services/filmstrip.service";

describe('EditAspectMetricsComponent', () => {
  let component: EditAspectMetricsComponent;
  let fixture: ComponentFixture<EditAspectMetricsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        EditAspectMetricsComponent,
        AspectMetricsComponent,
        MetricFinderComponent,
        LineChartComponent,
        ComparableFilmstripsComponent,
        FilmstripComponent,
        MetricSelectionComponent
      ],
      providers: [
        EditAspectMetricsComponent,
        {
          provide: ApplicationService,
          useValue: {
            selectedApplication$: new ReplaySubject<Application>(1),
            selectedPage$: new ReplaySubject<Page>(1),
            performanceAspectsForPage$: new BehaviorSubject<PerformanceAspect[]>([]),
            setSelectedApplication: (applicationId: string) => {
            }
          }
        },
        AspectConfigurationService,
        MetricFinderService,
        FilmstripService
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
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
