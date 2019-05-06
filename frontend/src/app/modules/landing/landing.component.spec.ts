import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {LandingComponent} from './landing.component';
import {SharedMocksModule} from "../../testing/shared-mocks.module";
import {ApplicationService} from "../../services/application.service";
import {SharedModule} from "../shared/shared.module";
import {By} from "@angular/platform-browser";
import {Application} from "../../models/application.model";
import {CsiValueSmallComponent} from "../shared/components/csi-value/csi-value-small/csi-value-small.component";
import {ApplicationCsi} from "../../models/application-csi.model";

describe('LandingComponent', () => {
  let component: LandingComponent;
  let fixture: ComponentFixture<LandingComponent>;
  let applicationService: ApplicationService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [LandingComponent],
      providers: [
        ApplicationService,
      ],
      imports: [
        SharedMocksModule,
        SharedModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    applicationService = TestBed.get(ApplicationService);
    fixture = TestBed.createComponent(LandingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should not have a main component when no data is loaded', () => {
    expect(fixture.debugElement.query(By.css("main"))).toBeFalsy();
  });

  it('should have a button to create a measurement', () => {
    applicationService.applications$.next({
      isLoading: false,
      data: []
    });
    fixture.detectChanges();
    const button = fixture.debugElement.query(By.css("main #create-measurement"));
    expect(button.nativeElement).toBeTruthy();
    expect(button.nativeElement.href).toMatch(".*/measurementSetup/create")
  });

  it('should show the centered empty state without applications', () => {
    applicationService.applications$.next({
      isLoading: false,
      data: []
    });
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css("main")).classes.center).toBeTruthy();
    expect(fixture.debugElement.query(By.css("osm-empty-state"))).toBeTruthy();
    expect(fixture.debugElement.query(By.css(".clickable-list"))).toBeFalsy();
  });

  it('should show a list of applications if existing, with loading CSI', () => {
    applicationService.applications$.next({
      isLoading: false,
      data: [
        new Application({id: 1, name: "TestOne"}),
        new Application({id: 2, name: "TestTwo"})
      ]
    });
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css("main")).classes.center).toBeFalsy();
    expect(fixture.debugElement.query(By.css(".clickable-list"))).toBeTruthy();
    const links = fixture.debugElement.queryAll(By.css(".clickable-list a"));
    expect(links.length).toBe(2);

    const linkHrefs = [".*/applicationDashboard/1", ".*/applicationDashboard/2"];
    const linkTitles = ["TestOne", "TestTwo"];
    expect(links.some(element => element.nativeElement.href.match(linkHrefs[0])));
    expect(links.some(element => element.query(By.css(".title")).nativeElement.textContent.trim().match(linkTitles[0])));
    expect(links.some(element => element.nativeElement.href.match(linkHrefs[1])));
    expect(links.some(element => element.query(By.css(".title")).nativeElement.textContent.trim().match(linkTitles[1])));
    expect(links[0].query(By.directive(CsiValueSmallComponent)).componentInstance.showLoading).toBeTruthy();
    expect(links[1].query(By.directive(CsiValueSmallComponent)).componentInstance.showLoading).toBeTruthy();
  });

  it('should show a list of applications if existing, with CSI values (in descending order)', () => {
    applicationService.applications$.next({
      isLoading: false,
      data: [
        new Application({id: 1, name: "TestOne"}),
        new Application({id: 2, name: "TestTwo"}),
        new Application({id: 3, name: "TestThree"}),
        new Application({id: 4, name: "TestFour"})
      ]
    });
    applicationService.applicationCsiById$.next({
      isLoading: false,
      1: new ApplicationCsi({csiValues: [{csiDocComplete: 50}, {csiDocComplete: 60}]}),
      2: new ApplicationCsi({csiValues: [{csiDocComplete: 60}, {csiDocComplete: 70}]}),
      3: new ApplicationCsi({csiValues: [{csiDocComplete: 70}, {csiDocComplete: 80}]}),
      4: new ApplicationCsi({csiValues: []})
    });
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css("main")).classes.center).toBeFalsy();
    expect(fixture.debugElement.query(By.css(".clickable-list"))).toBeTruthy();
    const links = fixture.debugElement.queryAll(By.css(".clickable-list a"));
    expect(links.length).toBe(4);
    expect(links[0].nativeElement.href).toMatch(".*/applicationDashboard/3");
    expect(links[0].query(By.css(".title")).nativeElement.textContent.trim()).toEqual("TestThree");
    expect(links[0].query(By.directive(CsiValueSmallComponent)).componentInstance.showLoading).toBeFalsy();
    expect(links[0].query(By.directive(CsiValueSmallComponent)).componentInstance.csiValue).toEqual(80);
    expect(links[1].nativeElement.href).toMatch(".*/applicationDashboard/2");
    expect(links[1].query(By.css(".title")).nativeElement.textContent.trim()).toEqual("TestTwo");
    expect(links[1].query(By.directive(CsiValueSmallComponent)).componentInstance.showLoading).toBeFalsy();
    expect(links[1].query(By.directive(CsiValueSmallComponent)).componentInstance.csiValue).toEqual(70);
    expect(links[2].nativeElement.href).toMatch(".*/applicationDashboard/1");
    expect(links[2].query(By.css(".title")).nativeElement.textContent.trim()).toEqual("TestOne");
    expect(links[2].query(By.directive(CsiValueSmallComponent)).componentInstance.showLoading).toBeFalsy();
    expect(links[2].query(By.directive(CsiValueSmallComponent)).componentInstance.csiValue).toEqual(60);
    expect(links[3].nativeElement.href).toMatch(".*/applicationDashboard/4");
    expect(links[3].query(By.css(".title")).nativeElement.textContent.trim()).toEqual("TestFour");
    expect(links[3].query(By.directive(CsiValueSmallComponent)).componentInstance.showLoading).toBeFalsy();
    expect(links[3].query(By.directive(CsiValueSmallComponent)).componentInstance.csiValue).toEqual(undefined);
  });

  it('should show a button to view all jobs', () => {
    applicationService.applications$.next({
      isLoading: false,
      data: []
    });
    fixture.detectChanges();
    const button = fixture.debugElement.query(By.css("a#show-jobs"));
    expect(button.nativeElement).toBeTruthy();
    expect(button.nativeElement.href).toMatch(".*/job/index");
  });

  it('should show empty state', () => {
    applicationService.applications$.next({
      isLoading: false,
      data: []
    });
    applicationService.failingJobs$.next([]);
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css(".healthy"))).toBeTruthy();
  });

  it('should show failed measurements', () => {
    applicationService.applications$.next({
      isLoading: false,
      data: []
    });
    applicationService.failingJobs$.next({
      'some_app': [{
        job_id: 771,
        percentageFailLast5: 100,
        location: "otto-prod-netlab",
        application: "develop_Desktop",
        script: "OTTO_ADS und HP"
      }]
    });
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css(".error"))).toBeTruthy();
  });
});
