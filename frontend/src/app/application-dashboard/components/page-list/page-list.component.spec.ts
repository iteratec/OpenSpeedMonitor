import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {PageListComponent} from './page-list.component';
import {ApplicationDashboardService} from '../../services/application-dashboard.service';
import {PageComponent} from '../page/page.component';
import {CsiValueComponent} from '../csi-value/csi-value.component';
import {SharedMocksModule} from '../../../testing/shared-mocks.module';

describe('PageListComponent', () => {
  let component: PageListComponent;
  let fixture: ComponentFixture<PageListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        PageListComponent,
        PageComponent,
        CsiValueComponent
      ],
      imports: [
        SharedMocksModule
      ],
      providers: [
        ApplicationDashboardService
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PageListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
