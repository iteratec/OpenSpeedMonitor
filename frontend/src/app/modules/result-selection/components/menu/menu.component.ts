import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";

@Component({
  selector: 'osm-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss']
})
export class MenuComponent implements OnInit {

  chartMenu: any[] = [
    {name: "time Series", baseUrl: "/eventResultDashboard/showAll", label: "frontend.de.iteratec.osm.results.timeSeries", icon: "fas fa-chart-line"},
    {name: "aggregation", baseUrl: "/aggregation/show", label: "frontend.de.iteratec.osm.results.aggregation", icon: "fas fa-chart-bar"},
    {name: "distribution", baseUrl: "/distributionChart/show", label: "frontend.de.iteratec.osm.results.distributionChart", icon: "fas fa-chart-area"},
    {name: "pageComparison", baseUrl: "/pageComparison/show", label: "frontend.de.iteratec.osm.results.pageComparison", icon: "fas fa-balance-scale"},
    {name: "detailAnalysis", baseUrl: "/detailAnalysis/show", label: "frontend.de.iteratec.osm.results.detailAnalysis", icon: "fas fa-chart-pie"},
    {name: "resultList", baseUrl: "/tabularResultPresentation/listResults", label: "frontend.de.iteratec.osm.results.resultList", icon: "fas fa-th-list"},
  ];

  constructor(private router: Router) { }

  ngOnInit() {
  }

}
