import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CsiGraphComponent } from './csi-graph.component';

describe('CsiGraphComponent', () => {
  let component: CsiGraphComponent;
  let fixture: ComponentFixture<CsiGraphComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CsiGraphComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CsiGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
