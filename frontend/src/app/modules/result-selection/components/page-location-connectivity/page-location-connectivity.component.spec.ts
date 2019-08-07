import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ActiveTab, PageLocationConnectivityComponent} from './page-location-connectivity.component';
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {ResultSelectionService} from "../../services/result-selection.service";
import {By} from "@angular/platform-browser";
import {ResultSelectionStore} from "../../services/result-selection.store";
import {SelectionDataComponent} from "./selection-data/selection-data.component";
import {ResultSelectionCommandParameter} from "../../models/result-selection-command.model";

describe('PageLocationConnectivityComponent', () => {
  let component: PageLocationConnectivityComponent;
  let fixture: ComponentFixture<PageLocationConnectivityComponent>;
  let resultSelectionService: ResultSelectionService;
  let resultSelectionStore: ResultSelectionStore;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [PageLocationConnectivityComponent, SelectionDataComponent],
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
    resultSelectionStore.eventsAndPages$.next({isLoading: false, data: []});
    resultSelectionStore.locationsAndBrowsers$.next({isLoading: false, data: []});
    component.showPageSelection = true;
    component.showMeasuredStepSelection = true;
    component.showBrowserSelection = true;
    component.showLocationSelection = true;
    component.showConnectivitySelection = true;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have correct selection data components', () => {
    expect(fixture.debugElement.queryAll(By.directive(SelectionDataComponent)).length).toBe(3);
    expect(fixture.debugElement.queryAll(By.directive(SelectionDataComponent)).map(debugElement =>
      debugElement.attributes['ng-reflect-parent-type']
    ))
      .toEqual([ResultSelectionCommandParameter.PAGES, ResultSelectionCommandParameter.BROWSERS, ResultSelectionCommandParameter.CONNECTIVITIES]);
  });

  it('should correctly switch between tabs', () => {
    expect(fixture.debugElement.query(By.css('#pageAndEventTab')).classes.active).toBe(true);
    expect(fixture.debugElement.query(By.css('#browserAndLocationTab')).classes.active).toBe(false);
    expect(fixture.debugElement.query(By.css('#connectivityTab')).classes.active).toBe(false);
    expect(fixture.debugElement.queryAll(By.css('osm-selection-data[hidden]')).length).toBe(2);
    expect(fixture.debugElement.queryAll(By.css('osm-selection-data:not([hidden])')).length).toBe(1);
    expect(fixture.debugElement.query(By.css('osm-selection-data:not([hidden])')).componentInstance.parentType).toBe(ResultSelectionCommandParameter.PAGES);
    component.showPageSelection = false;
    component.activeTab = ActiveTab.BrowserAndLocation;
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#pageAndEventTab'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#browserAndLocationTab')).classes.active).toBe(true);
    expect(fixture.debugElement.query(By.css('#connectivityTab')).classes.active).toBe(false);
    expect(fixture.debugElement.queryAll(By.css('osm-selection-data[hidden]')).length).toBe(1);
    expect(fixture.debugElement.queryAll(By.css('osm-selection-data:not([hidden])')).length).toBe(1);
    expect(fixture.debugElement.query(By.css('osm-selection-data:not([hidden])')).componentInstance.parentType).toBe(ResultSelectionCommandParameter.BROWSERS);
    component.activeTab = ActiveTab.Connectivity;
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#pageAndEventTab'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#browserAndLocationTab')).classes.active).toBe(false);
    expect(fixture.debugElement.query(By.css('#connectivityTab')).classes.active).toBe(true);
    expect(fixture.debugElement.queryAll(By.css('osm-selection-data[hidden]')).length).toBe(1);
    expect(fixture.debugElement.queryAll(By.css('osm-selection-data:not([hidden])')).length).toBe(1);
    expect(fixture.debugElement.query(By.css('osm-selection-data:not([hidden])')).componentInstance.parentType).toBe(ResultSelectionCommandParameter.CONNECTIVITIES);
  });
});
