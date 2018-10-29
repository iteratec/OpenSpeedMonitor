import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplicationJobStatusComponent } from './application-job-status.component';
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {ApplicationService} from "../../../../services/application.service";
import {Application} from "../../../../models/application.model";

describe('ApplicationJobStatusComponent', () => {
  let component: ApplicationJobStatusComponent;
  let fixture: ComponentFixture<ApplicationJobStatusComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        ApplicationJobStatusComponent
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
    fixture = TestBed.createComponent(ApplicationJobStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
