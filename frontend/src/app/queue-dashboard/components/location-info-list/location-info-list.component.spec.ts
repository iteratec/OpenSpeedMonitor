import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {LocationInfoListComponent} from "./location-info-list.component";
import {SharedMocksModule} from "../../../testing/shared-mocks.module";
import {formatDate} from "@angular/common";

import { registerLocaleData } from '@angular/common';
import localeDe from '@angular/common/locales/de';
registerLocaleData(localeDe, 'de');

describe("LocationInfoListComponent", () => {
  let component: LocationInfoListComponent;
  let fixture: ComponentFixture<LocationInfoListComponent>;
  let mockserver;
  let mockinformation = [];

  beforeEach( async( () => {
    TestBed.configureTestingModule({
      declarations: [
        LocationInfoListComponent
      ],
      imports: [
        SharedMocksModule
      ]
    }).compileComponents()
  }));

  beforeEach( () => {
    fixture = TestBed.createComponent(LocationInfoListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    mockserver = {
      label:"prod.server02.wpt.iteratec.de",
      baseUrl:"http://prod.server02.wpt.iteratec.de/",
      id:11
      };

    mockinformation = [{agents: 1,
      errorsLastHour: 0,
      eventResultLastHour: 0,
      eventsNextHour: 2,
      executingJobs: [],
      id: "Dulles_GalaxyS5:undefined",
      jobResultsLastHour: 0,
      jobs: 2,
      jobsNextHour: 1,
      label: "Dulles_GalaxyS5",
      lastHealthCheckDate: "2018-10-15 15:00:16.0",
      pendingJobs: 2,
      runningJobs: 0}];

    component.wptServerID = mockserver.id
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it("should display incoming information", () => {
    const datarows : HTMLCollection = fixture.nativeElement.querySelectorAll(".queueRow");
    expect(datarows.length).toEqual(0);

    component.serverInfo = {[mockserver.id] : mockinformation};
    fixture.detectChanges();

    const datarows2 : HTMLCollection = fixture.nativeElement.querySelectorAll(".queueRow");
    expect(datarows2.length).toBeGreaterThan(0);
  });

  it("should insert jobRows when pending or running jobs > 0", () =>{
    component.serverInfo = {[mockserver.id] : mockinformation};
    fixture.detectChanges();

    const jobRows : HTMLCollection = fixture.nativeElement.querySelectorAll(".jobRow");
    expect(jobRows.length).toBeGreaterThan(0);
  });

  it("should parse date correctly", () => {
    const date = component.parseDate("2018-10-16 11:00:14.0");
    const formatted = formatDate(date, "short", "de");
    expect(formatted).toEqual('16.10.18, 11:00');
  });

});
