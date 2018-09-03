import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CsiInfoComponent } from './csi-info.component';
import {ApplicationDashboardService} from "../../services/application-dashboard.service";
import {SharedMocksModule} from "../../../testing/shared-mocks.module";

describe('CsiInfoComponent', () => {
  let component: CsiInfoComponent;
  let fixture: ComponentFixture<CsiInfoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        CsiInfoComponent
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
    fixture = TestBed.createComponent(CsiInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
