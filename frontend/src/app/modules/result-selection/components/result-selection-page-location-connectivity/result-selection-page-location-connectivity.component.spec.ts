import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {
  ActiveTab,
  ResultSelectionPageLocationConnectivityComponent
} from './result-selection-page-location-connectivity.component';
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {ResultSelectionService} from "../../services/result-selection.service";
import {By} from "@angular/platform-browser";
import {SelectionDataComponent} from "./selection-data/selection-data.component";

describe('ResultSelectionPageLocationConnectivityComponent', () => {
  let component: ResultSelectionPageLocationConnectivityComponent;
  let fixture: ComponentFixture<ResultSelectionPageLocationConnectivityComponent>;
  let resultSelectionService: ResultSelectionService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ResultSelectionPageLocationConnectivityComponent, SelectionDataComponent],
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

  it('should have correct selection data components', () => {
    expect(fixture.debugElement.queryAll(By.directive(SelectionDataComponent)).length).toBe(3);
    expect(fixture.debugElement.queryAll(By.directive(SelectionDataComponent)).map(debugElement => debugElement.attributes.parentType)).toEqual(["page", "browser", "connectivity"]);
  });

  it('should correctly switch between tabs', () => {
    expect(fixture.debugElement.query(By.css('#pageAndEventTab')).classes.active).toBe(true);
    expect(fixture.debugElement.query(By.css('#browserAndLocationTab')).classes.active).toBe(false);
    expect(fixture.debugElement.query(By.css('#connectivityTab')).classes.active).toBe(false);
    expect(fixture.debugElement.queryAll(By.css('osm-selection-data[hidden]')).length).toBe(2);
    expect(fixture.debugElement.queryAll(By.css('osm-selection-data:not([hidden])')).length).toBe(1);
    expect(fixture.debugElement.query(By.css('osm-selection-data:not([hidden])')).componentInstance.parentType).toBe("page");
    component.showPageSelection = false;
    component.activeTab = ActiveTab.BrowserAndLocation;
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#pageAndEventTab'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#browserAndLocationTab')).classes.active).toBe(true);
    expect(fixture.debugElement.query(By.css('#connectivityTab')).classes.active).toBe(false);
    expect(fixture.debugElement.queryAll(By.css('osm-selection-data[hidden]')).length).toBe(1);
    expect(fixture.debugElement.queryAll(By.css('osm-selection-data:not([hidden])')).length).toBe(1);
    expect(fixture.debugElement.query(By.css('osm-selection-data:not([hidden])')).componentInstance.parentType).toBe("browser");
    component.activeTab = ActiveTab.Connectivity;
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('#pageAndEventTab'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('#browserAndLocationTab')).classes.active).toBe(false);
    expect(fixture.debugElement.query(By.css('#connectivityTab')).classes.active).toBe(true);
    expect(fixture.debugElement.queryAll(By.css('osm-selection-data[hidden]')).length).toBe(1);
    expect(fixture.debugElement.queryAll(By.css('osm-selection-data:not([hidden])')).length).toBe(1);
    expect(fixture.debugElement.query(By.css('osm-selection-data:not([hidden])')).componentInstance.parentType).toBe("connectivity");
  });
});
