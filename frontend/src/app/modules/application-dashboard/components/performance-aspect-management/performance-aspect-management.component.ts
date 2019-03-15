import {Component, Input, OnInit} from '@angular/core';
import {ResultSelectionService} from "../../../../services/result-selection.service";

@Component({
  selector: 'osm-performance-aspect-management',
  templateUrl: './performance-aspect-management.component.html',
  styleUrls: ['./performance-aspect-management.component.scss']
})
export class PerformanceAspectManagementComponent implements OnInit {
  @Input() pageId: number;

  constructor( private measurandsService: ResultSelectionService) { }

  ngOnInit() {
    this.measurandsService.updatePages([{id: this.pageId, name: "does-not-matter"}])
  }

}
