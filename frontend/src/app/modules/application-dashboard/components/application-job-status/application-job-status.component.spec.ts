import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplicationJobStatusComponent } from './application-job-status.component';
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {ApplicationService} from "../../../../services/application.service";
import {Application} from "../../../../models/application.model";
import {FailingJobStatistic} from "../../models/failing-job-statistic.model";

describe('ApplicationJobStatusComponent', () => {
  let component: ApplicationJobStatusComponent;
  let fixture: ComponentFixture<ApplicationJobStatusComponent>;
  let applicationService: ApplicationService;

  const noInformationAvailable = 'frontend.de.iteratec.osm.applicationDashboard.jobStatus.noInformationAvailable';
  const oneFailingJob = 'frontend.de.iteratec.osm.applicationDashboard.jobStatus.oneFailingJob';
  const multipleFailingJobs = 'frontend.de.iteratec.osm.applicationDashboard.jobStatus.multipleFailingJobs';
  const noFailingJobs = 'frontend.de.iteratec.osm.applicationDashboard.jobStatus.allJobsRunning';

  const questionCircleIcon = 'far fa-question-circle';
  const exclamationCircleIcon = 'fas fa-exclamation-circle';
  const checkCircleIcon = 'fas fa-check-circle';

  const allJobStatusIconCssCLasses = '.clickable-list i.fas.fa-exclamation-circle, i.far.fa-question-circle, i.fas.fa-check-circle';
  const allJobStatusCssClasses = '.job-status-warning, .job-status-good, .job-status-error';

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
    applicationService = TestBed.get(ApplicationService);
    fixture = TestBed.createComponent(ApplicationJobStatusComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should only show no information available message when FailingJobStatistic is null', () => {
    component.failingJobStatistic = null;
    component.ngOnChanges({});
    fixture.detectChanges();

    const errorMessageEl: HTMLElement = fixture.nativeElement.querySelector('.clickable-list li a');
    expect(errorMessageEl.textContent).toEqual(noInformationAvailable);
    const errorIconEl: HTMLElement = fixture.nativeElement.querySelector(allJobStatusIconCssCLasses);
    expect(errorIconEl.className).toEqual(questionCircleIcon);
    const jobStatusMessages: HTMLElement = fixture.nativeElement.querySelector(allJobStatusCssClasses);
    expect(jobStatusMessages).toBeFalsy();
  });

  it('should link to the correct job group', () => {
    component.selectedApplication = new Application({
      csiConfigurationId: null,
      dateOfLastResults: '',
      id: 67,
      name: 'Example'
    });
    component.ngOnChanges({});
    fixture.detectChanges();

    const showJobsButton: HTMLElement = fixture.nativeElement.querySelector('a.btn.btn-default');
    expect(showJobsButton.attributes.getNamedItem('href').value).toEqual('/job/index#/jobGroup=Example');
  });

  it('should show the correct error messages depending on the FailingJobStatistic', () => {
    component.failingJobStatistic = new FailingJobStatistic({
      minimumFailedJobSuccessRate: null,
      numberOfFailingJobs: 0
    });
    component.ngOnChanges({});
    fixture.detectChanges();

    let errorMessageEl: HTMLElement = fixture.nativeElement.querySelector(allJobStatusCssClasses);
    let errorIconEl: HTMLElement = fixture.nativeElement.querySelector(allJobStatusIconCssCLasses);
    expect(errorMessageEl.textContent).toEqual(noFailingJobs);
    expect(errorIconEl.className).toEqual(checkCircleIcon);
    expect(errorMessageEl.className).toEqual('job-status-good');

    component.failingJobStatistic = new FailingJobStatistic({
      minimumFailedJobSuccessRate: 0,
      numberOfFailingJobs: 0
    });
    component.ngOnChanges({});
    fixture.detectChanges();

    errorMessageEl = fixture.nativeElement.querySelector(allJobStatusCssClasses);
    errorIconEl = fixture.nativeElement.querySelector(allJobStatusIconCssCLasses);
    expect(errorMessageEl.textContent).toEqual(noFailingJobs);
    expect(errorIconEl.className).toEqual(checkCircleIcon);
    expect(errorMessageEl.className).toEqual('job-status-good');

    component.failingJobStatistic = new FailingJobStatistic({
      minimumFailedJobSuccessRate: 80,
      numberOfFailingJobs: 1
    });
    component.ngOnChanges({});
    fixture.detectChanges();

    errorMessageEl = fixture.nativeElement.querySelector(allJobStatusCssClasses);
    errorIconEl = fixture.nativeElement.querySelector(allJobStatusIconCssCLasses);
    expect(errorMessageEl.textContent).toEqual(oneFailingJob);
    expect(errorIconEl.className).toEqual(exclamationCircleIcon);
    expect(errorMessageEl.className).toEqual('job-status-warning');

    component.failingJobStatistic = new FailingJobStatistic({
      minimumFailedJobSuccessRate: 80,
      numberOfFailingJobs: 2
    });
    component.ngOnChanges({});
    fixture.detectChanges();

    errorMessageEl = fixture.nativeElement.querySelector(allJobStatusCssClasses);
    errorIconEl = fixture.nativeElement.querySelector(allJobStatusIconCssCLasses);
    expect(errorMessageEl.textContent).toEqual(multipleFailingJobs);
    expect(errorIconEl.className).toEqual(exclamationCircleIcon);
    expect(errorMessageEl.className).toEqual('job-status-warning');

    component.failingJobStatistic = new FailingJobStatistic({
      minimumFailedJobSuccessRate: 60,
      numberOfFailingJobs: 1
    });
    component.ngOnChanges({});
    fixture.detectChanges();

    errorMessageEl = fixture.nativeElement.querySelector(allJobStatusCssClasses);
    errorIconEl = fixture.nativeElement.querySelector(allJobStatusIconCssCLasses);
    expect(errorMessageEl.textContent).toEqual(oneFailingJob);
    expect(errorIconEl.className).toEqual(exclamationCircleIcon);
    expect(errorMessageEl.className).toEqual('job-status-error');

    component.failingJobStatistic = new FailingJobStatistic({
      minimumFailedJobSuccessRate: 60,
      numberOfFailingJobs: 2
    });
    component.ngOnChanges({});
    fixture.detectChanges();

    errorMessageEl = fixture.nativeElement.querySelector(allJobStatusCssClasses);
    errorIconEl = fixture.nativeElement.querySelector(allJobStatusIconCssCLasses);
    expect(errorMessageEl.textContent).toEqual(multipleFailingJobs);
    expect(errorIconEl.className).toEqual(exclamationCircleIcon);
    expect(errorMessageEl.className).toEqual('job-status-error');
  });

  it('should only show one job status message', () => {
    let errorMessagesEl = fixture.nativeElement.querySelectorAll('.clickable-list li a:not(.integrations)');
    expect(errorMessagesEl.length).toEqual(1);

    component.failingJobStatistic = new FailingJobStatistic({
      minimumFailedJobSuccessRate: 80,
      numberOfFailingJobs: 1
    });
    component.ngOnChanges({});
    fixture.detectChanges();

    errorMessagesEl = fixture.nativeElement.querySelectorAll('.clickable-list li a:not(.integrations)');
    expect(errorMessagesEl.length).toEqual(1);

    component.failingJobStatistic = new FailingJobStatistic({
      minimumFailedJobSuccessRate: 50,
      numberOfFailingJobs: 0
    });
    component.ngOnChanges({});
    fixture.detectChanges();

    errorMessagesEl = fixture.nativeElement.querySelectorAll('.clickable-list li a:not(.integrations)');
    expect(errorMessagesEl.length).toEqual(1);
  });

});
