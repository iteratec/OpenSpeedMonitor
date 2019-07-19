import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ChartSwitchMenuComponent} from './chart-switch-menu.component';
import {RouterTestingModule} from "@angular/router/testing";
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";

describe('ChartSwitchMenuComponent', () => {
  let component: ChartSwitchMenuComponent;
  let fixture: ComponentFixture<ChartSwitchMenuComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ChartSwitchMenuComponent],
      imports: [SharedMocksModule],
      providers: [RouterTestingModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ChartSwitchMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
