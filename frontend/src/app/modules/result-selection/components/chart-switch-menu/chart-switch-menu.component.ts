import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";
import {ChartSwitchMenuEntry} from "../../models/chart-switch-menu-entry.model";

@Component({
  selector: 'osm-menu',
  templateUrl: './chart-switch-menu.component.html',
  styleUrls: ['./chart-switch-menu.component.scss']
})
export class ChartSwitchMenuComponent implements OnInit {

  chartSwitchMenu: ChartSwitchMenuEntry[] = [
    {baseUrl: "/eventResultDashboard/showAll", label: "frontend.de.iteratec.osm.results.timeSeries", icon: "fas fa-chart-line"},
    {baseUrl: "/aggregationDev/show", label: "frontend.de.iteratec.osm.results.aggregation", icon: "fas fa-chart-bar"},
    {baseUrl: "/distributionChart/show", label: "frontend.de.iteratec.osm.results.distributionChart", icon: "fas fa-chart-area"},
    {baseUrl: "/pageComparison/show", label: "frontend.de.iteratec.osm.results.pageComparison", icon: "fas fa-balance-scale"},
    {baseUrl: "/detailAnalysis/show", label: "frontend.de.iteratec.osm.results.detailAnalysis", icon: "fas fa-chart-pie"},
    {baseUrl: "/tabularResultPresentation/listResults", label: "frontend.de.iteratec.osm.results.resultList", icon: "fas fa-th-list"}
  ];

  constructor(private router: Router) { }

  ngOnInit() {
  }

}
