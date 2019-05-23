import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {PageLocationConnectivityComponent} from './page-location-connectivity.component';
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {ResultSelectionService} from "../../services/result-selection.service";
import {By} from "@angular/platform-browser";
import {ResultSelectionStore} from "../../services/result-selection.store";

describe('PageLocationConnectivityComponent', () => {
  let component: PageLocationConnectivityComponent;
  let fixture: ComponentFixture<PageLocationConnectivityComponent>;
  let resultSelectionService: ResultSelectionService;
  let resultSelectionStore: ResultSelectionStore;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [PageLocationConnectivityComponent],
      imports: [SharedMocksModule],
      providers: [
        ResultSelectionService,
        ResultSelectionStore
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    resultSelectionStore = TestBed.get(ResultSelectionStore);
    fixture = TestBed.createComponent(PageLocationConnectivityComponent);
    component = fixture.componentInstance;

    resultSelectionStore.eventsAndPages$.next([
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
    resultSelectionStore.locationsAndBrowsers$.next([
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
          id: 1,
          name: "Chrome"
        }
      },
      {
        id: 102,
        name: "prod-location-3",
        parent: {
          id: 2,
          name: "Firefox"
        }
      }
    ]);
    resultSelectionStore.connectivities$.next([
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
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show selectable pages and events', () => {
    fixture.debugElement.query(By.css('#pageAndEventTab')).nativeElement.click();
    fixture.detectChanges();
    expect(fixture.debugElement.queryAll(By.css('#result-selection-page-selection option')).length).toBe(2);
    expect(fixture.debugElement.query(By.css('#result-selection-event-selection')).componentInstance.items.length).toBe(3);
  });

  it('should show selectable browser and locations', () => {
    fixture.debugElement.query(By.css('#browserAndLocationTab')).nativeElement.click();
    fixture.detectChanges();
    expect(fixture.debugElement.queryAll(By.css('#result-selection-browser-selection option')).length).toBe(2);
    expect(fixture.debugElement.query(By.css('#result-selection-location-selection')).componentInstance.items.length).toBe(3);
  });

  it('should show selectable connectivities', () => {
    fixture.debugElement.query(By.css('#connectivityTab')).nativeElement.click();
    fixture.detectChanges();
    expect(fixture.debugElement.queryAll(By.css('#result-selection-connectivity-selection option')).length).toBe(2);
  });

  it('should correctly show available items according to selections', () => {
    fixture.debugElement.query(By.css('#pageAndEventTab')).nativeElement.click();
    fixture.detectChanges();
    let selectElement = fixture.debugElement.query(By.css('#result-selection-page-selection')).nativeElement;
    selectElement.value = selectElement.options[0].value;
    selectElement.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(component.selectedPages).toEqual([2]);
    expect(fixture.debugElement.query(By.css('#result-selection-event-selection')).componentInstance.items.length).toBe(1);
    expect(fixture.debugElement.query(By.css('#result-selection-event-selection')).componentInstance.items[0].id).toBe(101);

    fixture.debugElement.query(By.css('#browserAndLocationTab')).nativeElement.click();
    fixture.detectChanges();
    selectElement = fixture.debugElement.query(By.css('#result-selection-browser-selection')).nativeElement;
    selectElement.value = selectElement.options[0].value;
    selectElement.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(component.selectedBrowsers).toEqual([1]);
    expect(fixture.debugElement.query(By.css('#result-selection-location-selection')).componentInstance.items.length).toBe(2);
    expect(fixture.debugElement.query(By.css('#result-selection-location-selection')).componentInstance.items.map(item => item.id)).toEqual([100, 101]);

    fixture.debugElement.query(By.css('#connectivityTab')).nativeElement.click();
    fixture.detectChanges();
    selectElement = fixture.debugElement.query(By.css('#result-selection-connectivity-selection')).nativeElement;
    selectElement.value = selectElement.options[0].value;
    selectElement.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(component.selectedConnectivities).toEqual([1]);
  });

});
