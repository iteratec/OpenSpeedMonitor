import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { SelectionDataComponent } from './selection-data.component';
import {SharedMocksModule} from "../../../../../testing/shared-mocks.module";
import {ResultSelectionService} from "../../../services/result-selection.service";
import {of} from "rxjs";
import {By} from "@angular/platform-browser";

describe('SelectionDataComponent', () => {
  let component: SelectionDataComponent;
  let fixture: ComponentFixture<SelectionDataComponent>;
  let resultSelectionService: ResultSelectionService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SelectionDataComponent ],
      imports: [ SharedMocksModule ],
      providers: [ ResultSelectionService]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    resultSelectionService = TestBed.get(ResultSelectionService);
    fixture = TestBed.createComponent(SelectionDataComponent);
    component = fixture.componentInstance;

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
  });

  it('should create', () => {
    component.parentChildData$ = of([]);
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should display the correct data with active child selection', () => {
    component.childType="measuredStep";
    component.parentType="page";
    component.parentChildData$ = resultSelectionService.eventsAndPages$;
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.options.length).toBe(2);
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.options[0].innerText).toBe("ADS");
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.options[1].innerText).toBe("HP_entry");
    expect(fixture.debugElement.query(By.css('#result-selection-child-selection')).componentInstance.items.length).toBe(3);
    expect(fixture.debugElement.query(By.css('#result-selection-child-selection')).componentInstance.items.map(item => item.id)).toEqual([101,100,102]);
  });

  it('should display the correct data with inactive child selection', () => {
    component.childType="location";
    component.parentType="browser";
    component.showChildSelection = false;
    component.parentChildData$ = resultSelectionService.locationsAndBrowsers$;
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.options.length).toBe(2);
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.options[0].innerText).toBe("Chrome");
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.options[1].innerText).toBe("Firefox");
    expect(fixture.debugElement.query(By.css('#result-selection-child-selection'))).toBeFalsy();
  });

  it('should correctly assign the opacity of the selection field if the selection is not optional', () => {
    component.childType="location";
    component.parentType="browser";
    component.parentChildData$ = resultSelectionService.locationsAndBrowsers$;
    component.parentSelectionOptional = false;
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.style.opacity).toBe('1');
    component.parentSelection = [1];
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.style.opacity).toBe('1');
  });

  it('should correctly assign the opacity of the selection field if the selection is optional', () => {
    component.childType="location";
    component.parentType="browser";
    component.parentChildData$ = resultSelectionService.locationsAndBrowsers$;
    component.parentSelectionOptional = true;
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.style.opacity).toBe('0.5');
    component.parentSelection = [1];
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#result-selection-parent-selection')).nativeElement.style.opacity).toBe('1');
  });
});
