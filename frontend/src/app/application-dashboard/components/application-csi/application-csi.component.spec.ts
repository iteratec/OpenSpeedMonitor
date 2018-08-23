import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ApplicationCsiComponent} from './application-csi.component';
import {CsiValueComponent} from '../csi-value/csi-value.component';
import {SharedMocksModule} from '../../../testing/shared-mocks.module';
import {ApplicationDashboardService} from '../../services/application-dashboard.service';
import {CsiGraphComponent} from '../csi-graph/csi-graph.component';
import {CsiInfoComponent} from "../csi-info/csi-info.component";

describe('ApplicationCsiComponent', () => {
  let component: ApplicationCsiComponent;
  let fixture: ComponentFixture<ApplicationCsiComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        ApplicationCsiComponent,
        CsiValueComponent,
        CsiGraphComponent,
        CsiInfoComponent
      ],
      providers: [
        ApplicationDashboardService
      ],
      imports: [
        SharedMocksModule
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationCsiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
