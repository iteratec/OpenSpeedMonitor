import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ResultSelectionPageLocationConnectivityComponent} from './result-selection-page-location-connectivity.component';
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {ResultSelectionService} from "../../services/result-selection.service";
import {By} from "@angular/platform-browser";

describe('ResultSelectionPageLocationConnectivityComponent', () => {
  let component: ResultSelectionPageLocationConnectivityComponent;
  let fixture: ComponentFixture<ResultSelectionPageLocationConnectivityComponent>;
  let resultSelectionService: ResultSelectionService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ResultSelectionPageLocationConnectivityComponent],
      imports: [SharedMocksModule],
      providers: [ResultSelectionService]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    resultSelectionService = TestBed.get(ResultSelectionService);
    fixture = TestBed.createComponent(ResultSelectionPageLocationConnectivityComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show selectable pages and events', () => {
    fixture.debugElement.query(By.css('#pageAndEventTab')).nativeElement.click();
    resultSelectionService.eventsAndPages$.next([
      {
        id: 100,
        name: "Website1_HP_entry",
        parent: {
          id: 1,
          name: "HP_entry"
        }
      },
      {
        id: 101,
        name: "Website1_ADS",
        parent: {
          id: 2,
          name: "ADS"
        }
      },
      {
        id: 102,
        name: "Website2_HP_entry",
        parent: {
          id: 1,
          name: "HP_entry"
        }
      }
    ]);
    fixture.detectChanges();
    expect(fixture.debugElement.queryAll(By.css('#result-selection-page-selection option')).length).toBe(2);
    expect(fixture.debugElement.query(By.css('#result-selection-event-selection')).componentInstance.items.length).toBe(3);
  });

  it('should show selectable browser and locations', () => {
    fixture.debugElement.query(By.css('#browserAndLocationTab')).nativeElement.click();
    resultSelectionService.locationsAndBrowsers$.next([
      {
        id: 100,
        name: "prod-location-1",
        parent: {
          id: 1,
          name: "Chrome"
        }
      },
      {
        id: 101,
        name: "prod-location-2",
        parent: {
          id: 2,
          name: "Chrome"
        }
      },
      {
        id: 102,
        name: "prod-location-3",
        parent: {
          id: 1,
          name: "Firefox"
        }
      }
    ]);
    fixture.detectChanges();
    expect(fixture.debugElement.queryAll(By.css('#result-selection-browser-selection option')).length).toBe(2);
    expect(fixture.debugElement.query(By.css('#result-selection-location-selection')).componentInstance.items.length).toBe(3);
  });

  it('should show selectable connectivities', () => {
    fixture.debugElement.query(By.css('#connectivityTab')).nativeElement.click();
    resultSelectionService.connectivities$.next([
      {
        id: 1,
        name: "DSL 6.000",
      },
      {
        id: 2,
        name: "UMTS",
      }
    ]);
    fixture.detectChanges();
    expect(fixture.debugElement.queryAll(By.css('#result-selection-connectivity-selection option')).length).toBe(2);
  });

});
