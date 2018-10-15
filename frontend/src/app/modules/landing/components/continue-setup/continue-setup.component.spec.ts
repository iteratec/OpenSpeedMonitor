import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {ContinueSetupComponent} from './continue-setup.component';
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {SharedModule} from "../../../shared/shared.module";
import {By} from "@angular/platform-browser";

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

  it('should have a button to resume the setup', () => {
    const button = fixture.debugElement.query(By.css("#continue-setup"));
    expect(button.nativeElement).toBeTruthy();
    expect(button.nativeElement.href).toMatch(".*/infrastructureSetup")
  });
});
