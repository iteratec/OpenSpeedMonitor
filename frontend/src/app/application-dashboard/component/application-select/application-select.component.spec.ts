import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplicationSelectComponent } from './application-select.component';

describe('ApplicationSelectComponent', () => {
  let component: ApplicationSelectComponent;
  let fixture: ComponentFixture<ApplicationSelectComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ApplicationSelectComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ApplicationSelectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
