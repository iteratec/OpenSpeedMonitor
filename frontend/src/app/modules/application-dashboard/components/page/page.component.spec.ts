import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {PageComponent} from './page.component';
import {CsiValueComponent} from '../csi-value/csi-value.component';
import {ApplicationService} from '../../../../services/application.service';
import {SharedMocksModule} from '../../../../testing/shared-mocks.module';
import {PageMetricComponent} from "../page-metric/page-metric.component";

describe('PageComponent', () => {
  let component: PageComponent;
  let fixture: ComponentFixture<PageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        PageComponent,
        CsiValueComponent,
        PageMetricComponent
      ],
      imports: [
        SharedMocksModule
      ],
      providers: [
        ApplicationService
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
