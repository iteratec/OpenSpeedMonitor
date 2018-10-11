import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ContinueSetupComponent} from './continue-setup.component';
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {SharedModule} from "../../../shared/shared.module";

describe('ContinueSetupComponent', () => {
  let component: ContinueSetupComponent;
  let fixture: ComponentFixture<ContinueSetupComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ContinueSetupComponent],
      imports: [
        SharedMocksModule,
        SharedModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ContinueSetupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
