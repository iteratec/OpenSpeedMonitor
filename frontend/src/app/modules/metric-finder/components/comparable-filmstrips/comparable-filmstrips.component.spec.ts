import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ComparableFilmstripsComponent } from './comparable-filmstrips.component';

describe('ComparableFilmstripsComponent', () => {
  let component: ComparableFilmstripsComponent;
  let fixture: ComponentFixture<ComparableFilmstripsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ComparableFilmstripsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ComparableFilmstripsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
