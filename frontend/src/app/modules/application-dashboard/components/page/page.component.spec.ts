import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {PageComponent} from './page.component';
import {ApplicationService} from '../../../../services/application.service';
import {PageMetricComponent} from "../page-metric/page-metric.component";
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {CsiValueMediumComponent} from "../../../shared/components/csi-value/csi-value-medium/csi-value-medium.component";
import {CsiValueBaseComponent} from "../../../shared/components/csi-value/csi-value-base.component";

describe('PageComponent', () => {

  let component: PageComponent;
  let fixture: ComponentFixture<PageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        PageComponent,
        CsiValueMediumComponent,
        CsiValueBaseComponent,
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
