import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {ChartSwitchMenuEntry} from "../../../result-selection/models/chart-switch-menu-entry.model";

@Component({
  selector: 'osm-menu',
  templateUrl: './chart-switch-menu.component.html',
  styleUrls: ['./chart-switch-menu.component.scss']
})
export class ChartSwitchMenuComponent implements OnInit {

  chartSwitchMenu: ChartSwitchMenuEntry[] = [
    {baseUrl: "/eventResultDashboardDev/showAll", label: "frontend.de.iteratec.osm.results.timeSeries", icon: "fas fa-chart-line"},
    {baseUrl: "/aggregationDev/show", label: "frontend.de.iteratec.osm.results.aggregation", icon: "fas fa-chart-bar"},
    {baseUrl: "/distributionDev/show", label: "frontend.de.iteratec.osm.results.distribution", icon: "fas fa-chart-area"},
    {baseUrl: "/pageComparison/show", label: "frontend.de.iteratec.osm.results.pageComparison", icon: "fas fa-balance-scale"},
    {baseUrl: "/detailAnalysis/show", label: "frontend.de.iteratec.osm.results.detailAnalysis", icon: "fas fa-chart-pie"},
    {baseUrl: "/tabularResultPresentation/listResults", label: "frontend.de.iteratec.osm.results.resultList", icon: "fas fa-th-list"}
  ];

  queryString: string = '';

  constructor(private router: Router, private route: ActivatedRoute) {
    route.queryParams.subscribe(() => {
      this.queryString = window.location.search;
    });
  }

  ngOnInit() {
  }

}
