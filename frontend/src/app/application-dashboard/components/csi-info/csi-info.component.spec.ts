import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CsiInfoComponent } from './csi-info.component';

describe('CsiInfoComponent', () => {
  let component: CsiInfoComponent;
  let fixture: ComponentFixture<CsiInfoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CsiInfoComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CsiInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
