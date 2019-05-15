import {Component, OnInit} from '@angular/core';
import {ApplicationService} from "../../services/application.service";
import {PageMetricsDto} from "../application-dashboard/models/page-metrics.model";
import {ActivatedRoute} from "@angular/router";
import {Page} from "../../models/page.model";

@Component({
  selector: 'osm-aspect-configuration',
  templateUrl: './aspect-configuration.component.html',
  styleUrls: ['./aspect-configuration.component.scss']
})
export class AspectConfigurationComponent implements OnInit {

  page: Page;

  constructor(private route: ActivatedRoute, public applicationService: ApplicationService) { }

  ngOnInit() {
    const pageId = Number(this.route.snapshot.paramMap.get("pageId"));
    const pages = this.applicationService.metrics$.getValue().filter((pageMetricsDto: PageMetricsDto) => pageMetricsDto.pageId == pageId)
    if (pages.length == 1 ){
      const pageMetricDto: PageMetricsDto = pages[0];
      this.page = {id: pageMetricDto.pageId, name: pageMetricDto.pageName}
    }
  }
}
