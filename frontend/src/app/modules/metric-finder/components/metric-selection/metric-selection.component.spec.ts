import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MetricSelectionComponent } from './metric-selection.component';
import {MetricFinderService} from '../../services/metric-finder.service';
import {MetricFinderServiceMock} from '../../services/metric-finder.service.mock';

describe('MetricSelectionComponent', () => {
  let component: MetricSelectionComponent;
  let fixture: ComponentFixture<MetricSelectionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MetricSelectionComponent ],
      providers: [{
        provide: MetricFinderService,
        useClass: MetricFinderServiceMock
      }]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MetricSelectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have a correct intersect method', () => {
    const lists = [
      ['a', 'b', 'c', 'd', 'e',           'x'     ],
      [     'b', 'c', 'd', 'e', 'f', 'g'          ],
      ['a',      'c', 'd', 'e', 'f', 'g', 'x'     ],
      [          'c', 'd'                         ],
      [          'c', 'd',                     'y']
    ];
    const expected = ['c', 'd'];
    // @ts-ignore
    expect(component.intersect(lists)).toEqual(expected);
  });

});
