import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {EmptyStateComponent} from './empty-state.component';
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";

describe('EmptyStateComponent', () => {
  let component: EmptyStateComponent;
  let fixture: ComponentFixture<EmptyStateComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [EmptyStateComponent],
      imports: [
        SharedMocksModule
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EmptyStateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
