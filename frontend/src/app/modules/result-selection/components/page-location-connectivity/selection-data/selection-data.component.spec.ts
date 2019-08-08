import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {SelectionDataComponent} from './selection-data.component';
import {SharedMocksModule} from "../../../../../testing/shared-mocks.module";
import {of} from "rxjs";
import {By} from "@angular/platform-browser";
import {ResultSelectionCommandParameter} from "../../../models/result-selection-command.model";
import {ResultSelectionStore} from "../../../services/result-selection.store";
import {ResultSelectionService} from "../../../services/result-selection.service";

describe('SelectionDataComponent', () => {
  let component: SelectionDataComponent;
  let fixture: ComponentFixture<SelectionDataComponent>;
  let resultSelectionStore: ResultSelectionStore;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SelectionDataComponent ],
      imports: [ SharedMocksModule ],
      providers: [
        ResultSelectionStore,
        ResultSelectionService
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    resultSelectionStore = TestBed.get(ResultSelectionStore);
    fixture = TestBed.createComponent(SelectionDataComponent);
    component = fixture.componentInstance;

    resultSelectionStore.eventsAndPages$.next({
      isLoading: false, data: [
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
      ]
    });
    resultSelectionStore.locationsAndBrowsers$.next({
      isLoading: false, data: [
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
      ]
    });
    resultSelectionStore.connectivities$.next({
      isLoading: false, data: [
      {
        id: 1,
        name: "DSL 6.000",
      },
      {
        id: 2,
        name: "UMTS",
      }
      ]
    });
  });

  it('should create', () => {
    component.parentChildData$ = of({isLoading: false, data: []});
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should display the correct data with active child selection', () => {
    component.childType = ResultSelectionCommandParameter.MEASURED_EVENTS;
    component.parentType = ResultSelectionCommandParameter.PAGES;
    component.parentChildData$ = resultSelectionStore.eventsAndPages$;
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.options.length).toBe(2);
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.options[0].innerText).toBe("ADS");
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.options[1].innerText).toBe("HP_entry");
    expect(fixture.debugElement.query(By.css('#result-selection-child-selection')).componentInstance.items.length).toBe(3);
    expect(fixture.debugElement.query(By.css('#result-selection-child-selection')).componentInstance.items.map(item => item.id)).toEqual([101,100,102]);
  });

  it('should display the correct data with inactive child selection', () => {
    component.childType = ResultSelectionCommandParameter.LOCATIONS;
    component.parentType = ResultSelectionCommandParameter.BROWSERS;
    component.showChildSelection = false;
    component.parentChildData$ = resultSelectionStore.locationsAndBrowsers$;
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.options.length).toBe(2);
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.options[0].innerText).toBe("Chrome");
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.options[1].innerText).toBe("Firefox");
    expect(fixture.debugElement.query(By.css('#result-selection-child-selection'))).toBeFalsy();
  });

  it('should correctly assign the opacity of the selection field if the selection is not optional', () => {
    component.childType = ResultSelectionCommandParameter.LOCATIONS;
    component.parentType = ResultSelectionCommandParameter.BROWSERS;
    component.parentChildData$ = resultSelectionStore.locationsAndBrowsers$;
    component.parentRequired = true;
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.style.opacity).toBe('1');
    component.parentSelection = [1];
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.style.opacity).toBe('1');
  });

  it('should correctly assign the opacity of the selection field if the selection is optional', () => {
    component.childType = ResultSelectionCommandParameter.LOCATIONS;
    component.parentType = ResultSelectionCommandParameter.BROWSERS;
    component.parentChildData$ = resultSelectionStore.locationsAndBrowsers$;
    component.parentRequired = false;
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.style.opacity).toBe('0.5');
    component.parentSelection = [1];
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.style.opacity).toBe('1');
  });
});
