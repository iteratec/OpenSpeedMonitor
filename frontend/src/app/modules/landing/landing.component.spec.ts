import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {LandingComponent} from './landing.component';
import {SharedMocksModule} from "../../testing/shared-mocks.module";
import {ApplicationService} from "../../services/application.service";
import {SharedModule} from "../shared/shared.module";
import {By} from "@angular/platform-browser";
import {Application} from "../../models/application.model";
import {CsiValueSmallComponent} from "../shared/components/csi-value/csi-value-small/csi-value-small.component";
import {ApplicationCsi} from "../../models/csi-list.model";

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
    expect(links[0].nativeElement.href).toMatch(".*/applicationDashboard/1");
    expect(links[0].query(By.css(".title")).nativeElement.textContent.trim()).toEqual("TestOne");
    expect(links[0].query(By.directive(CsiValueSmallComponent)).componentInstance.showLoading).toBeTruthy();
    expect(links[1].nativeElement.href).toMatch(".*/applicationDashboard/2");
    expect(links[1].query(By.css(".title")).nativeElement.textContent.trim()).toEqual("TestTwo");
    expect(links[1].query(By.directive(CsiValueSmallComponent)).componentInstance.showLoading).toBeTruthy();
  });

  it('should show a list of applications if existing, with CSI values', () => {
    applicationService.applications$.next({
      isLoading: false,
      data: [
        new Application({id: 1, name: "TestOne"}),
        new Application({id: 2, name: "TestTwo"})
      ]
    });
    applicationService.applicationCsiById$.next({
      isLoading: false,
      1: new ApplicationCsi({csiValues: [{csiDocComplete: 50}, {csiDocComplete: 60}]}),
      2: new ApplicationCsi({csiValues: [{csiDocComplete: 60}, {csiDocComplete: 70}]})
    });
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css("main")).classes.center).toBeFalsy();
    expect(fixture.debugElement.query(By.css(".clickable-list"))).toBeTruthy();
    const links = fixture.debugElement.queryAll(By.css(".clickable-list a"));
    expect(links.length).toBe(2);
    expect(links[0].nativeElement.href).toMatch(".*/applicationDashboard/1");
    expect(links[0].query(By.css(".title")).nativeElement.textContent.trim()).toEqual("TestOne");
    expect(links[0].query(By.directive(CsiValueSmallComponent)).componentInstance.showLoading).toBeFalsy();
    expect(links[0].query(By.directive(CsiValueSmallComponent)).componentInstance.csiValue).toEqual(60);
    expect(links[1].nativeElement.href).toMatch(".*/applicationDashboard/2");
    expect(links[1].query(By.css(".title")).nativeElement.textContent.trim()).toEqual("TestTwo");
    expect(links[1].query(By.directive(CsiValueSmallComponent)).componentInstance.showLoading).toBeFalsy();
    expect(links[1].query(By.directive(CsiValueSmallComponent)).componentInstance.csiValue).toEqual(70);
  })
});
