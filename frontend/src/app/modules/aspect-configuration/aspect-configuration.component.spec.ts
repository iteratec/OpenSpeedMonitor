import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { AspectConfigurationComponent } from './aspect-configuration.component';

describe('AspectConfigurationComponent', () => {
  let component: AspectConfigurationComponent;
  let fixture: ComponentFixture<AspectConfigurationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ AspectConfigurationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AspectConfigurationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
