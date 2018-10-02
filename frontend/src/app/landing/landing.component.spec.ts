import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {LandingComponent} from './landing.component';
import {SharedMocksModule} from "../testing/shared-mocks.module";
import {LandingService} from "./services/landing.service";

describe('LandingComponent', () => {
  let component: LandingComponent;
  let fixture: ComponentFixture<LandingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [LandingComponent],
      providers: [
        LandingService
      ],
      imports: [
        SharedMocksModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LandingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
