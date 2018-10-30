import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {EmptyStateComponent} from './empty-state.component';
import {SharedMocksModule} from "../../../../testing/shared-mocks.module";
import {By} from "@angular/platform-browser";

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

  it('should have a caption with translated message key', () => {
    component.messageKey = "testKey";
    component.image = "youdontsay.jpeg";
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css(".empty-state figcaption")).nativeElement.innerHTML).toEqual("testKey");
    expect(fixture.debugElement.query(By.css(".empty-state img")).nativeElement.src).toMatch(".*/assets/frontend/youdontsay.jpeg");
  });
});
