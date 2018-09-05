import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {CsiInfoComponent} from './csi-info.component';
import {ApplicationDashboardService} from "../../services/application-dashboard.service";
import {SharedMocksModule} from "../../../testing/shared-mocks.module";

describe('CsiInfoComponent', () => {
  let component: CsiInfoComponent;
  let fixture: ComponentFixture<CsiInfoComponent>;
  let applicationDashboardService: ApplicationDashboardService;

  let caseOneText = 'This application has not been measured yet.';
  let caseTwoText = 'The calculation of the customer satisfaction index (CSI) is not configured for this application.';
  let caseThreeText = 'The configuration of the customer satisfaction index (CSI) does not produce a CSI value for this application or there are no new measurements since the CSI configuration has been updated.';
  let caseFourText = 'The measurements are not working properly.';

  let infoIconClass = 'icon-info fas fa-info-circle';
  let warningIconClass = 'icon-warning fas fa-exclamation-triangle';

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
    component.selectedApplication = {
      csiConfigurationId: null,
      dateOfLastResults: '',
      id: 67,
      name: 'Example'
    };
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should be able to call the createCsiConfiguration method from the ApplicationDashboardService', () => {
    applicationDashboardService = TestBed.get(ApplicationDashboardService);

    spyOn(applicationDashboardService, "createCsiConfiguration");
    component.createCsiConfiguration();
    expect(applicationDashboardService.createCsiConfiguration).toHaveBeenCalledWith(component.selectedApplication);
  });

  it('should show message that application has not been measured yet (case 1)', () => {
    component.csiData = {
      csiDtoList: [],
      hasCsiConfiguration: true,
      hasInvalidJobResults: false,
      hasJobResults: false
    };

    component.ngOnChanges({});

    expect(component.errorCase).toBeUndefined();
    expect(component.iconClass).toEqual(infoIconClass);
    expect(component.infoText).toEqual(caseOneText);

    fixture.detectChanges();

    const infoTextEl: HTMLElement = fixture.nativeElement.querySelector('.info-text');
    expect(infoTextEl.textContent).toEqual(caseOneText);
    const infoIconEl: HTMLElement = fixture.nativeElement.querySelector('.icon-info');
    expect(infoIconEl.className).toEqual(infoIconClass);
    const infoButtonEl: HTMLElement = fixture.nativeElement.querySelector('.info-button');
    expect(infoButtonEl).toBeFalsy();
  });

  it('should show message that csi is not configured and an option to create one (case 2)', () => {
    component.csiData = {
      csiDtoList: [],
      hasCsiConfiguration: false,
      hasInvalidJobResults: false,
      hasJobResults: false
    };

    component.ngOnChanges({});

    expect(component.errorCase).toBe(2);
    expect(component.iconClass).toEqual(infoIconClass);
    expect(component.infoText).toEqual(caseTwoText);

    fixture.detectChanges();

    const infoTextEl: HTMLElement = fixture.nativeElement.querySelector('.info-text');
    expect(infoTextEl.textContent).toEqual(caseTwoText);
    const infoIconEl: HTMLElement = fixture.nativeElement.querySelector('.icon-info');
    expect(infoIconEl.className).toEqual(infoIconClass);
    const infoButtonEl: HTMLElement = fixture.nativeElement.querySelector('.info-button');
    expect(infoButtonEl.textContent).toEqual('Create CSI Configuration');
  });

  it('should show message that no csi results exist and an option to go to related configuration (case 3)', () => {
    component.csiData = {
      csiDtoList: [],
      hasCsiConfiguration: true,
      hasInvalidJobResults: false,
      hasJobResults: true
    };
    component.selectedApplication.csiConfigurationId = 8;

    component.ngOnChanges({});

    expect(component.errorCase).toBe(3);
    expect(component.iconClass).toEqual(warningIconClass);
    expect(component.infoText).toEqual(caseThreeText);

    fixture.detectChanges();

    const infoTextEl: HTMLElement = fixture.nativeElement.querySelector('.info-text');
    expect(infoTextEl.textContent).toEqual(caseThreeText);
    const infoIconEl: HTMLElement = fixture.nativeElement.querySelector('.icon-warning');
    expect(infoIconEl.className).toEqual(warningIconClass);
    const infoButtonEl: HTMLElement = fixture.nativeElement.querySelector('.info-button');
    expect(infoButtonEl.textContent).toEqual('Configure CSI Configuration');
    expect(infoButtonEl.attributes.getNamedItem('href').value).toEqual('/csiConfiguration/configurations/8');
  });

  it('should show message that measurements are faulty and an option to view filtered job overview (case 4)', () => {
    component.csiData = {
      csiDtoList: [],
      hasCsiConfiguration: true,
      hasInvalidJobResults: true,
      hasJobResults: true
    };

    component.ngOnChanges({});

    expect(component.errorCase).toBe(4);
    expect(component.iconClass).toEqual(warningIconClass);
    expect(component.infoText).toEqual(caseFourText);

    fixture.detectChanges();

    const infoTextEl: HTMLElement = fixture.nativeElement.querySelector('.info-text');
    expect(infoTextEl.textContent).toEqual(caseFourText);
    const infoIconEl: HTMLElement = fixture.nativeElement.querySelector('.icon-warning');
    expect(infoIconEl.className).toEqual(warningIconClass);
    const infoButtonEl: HTMLElement = fixture.nativeElement.querySelector('.info-button');
    expect(infoButtonEl.textContent).toEqual('Check Measurements');
    expect(infoButtonEl.attributes.getNamedItem('href').value).toEqual('/job/#/jobGroup=Example');
  });

});
