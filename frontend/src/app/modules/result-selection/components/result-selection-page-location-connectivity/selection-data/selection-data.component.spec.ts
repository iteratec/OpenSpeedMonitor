import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectionDataComponent } from './selection-data.component';

describe('SelectionDataComponent', () => {
  let component: SelectionDataComponent;
  let fixture: ComponentFixture<SelectionDataComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SelectionDataComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectionDataComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
