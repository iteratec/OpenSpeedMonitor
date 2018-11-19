import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { GraphiteIntegrationComponent } from './graphite-integration.component';
import {SharedMocksModule} from "../../../../../testing/shared-mocks.module";

describe('GraphiteIntegrationComponent', () => {
  let component: GraphiteIntegrationComponent;
  let fixture: ComponentFixture<GraphiteIntegrationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ GraphiteIntegrationComponent ],
      imports: [ SharedMocksModule ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(GraphiteIntegrationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
