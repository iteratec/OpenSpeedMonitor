import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplicationCsiComponent } from './application-csi.component';

describe('ApplicationCsiComponent', () => {
  let component: ApplicationCsiComponent;
  let fixture: ComponentFixture<ApplicationCsiComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ApplicationCsiComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationCsiComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
