import {
  Component,
  ElementRef,
  Input,
  OnInit,
  ViewChild
} from '@angular/core';
import {GrailsBridgeService} from "../../../../services/grails-bridge.service";
import {AggregationChartDataService} from "../../services/aggregation-chart-data.service";

@Component({
  selector: 'osm-aggregation-chart',
  templateUrl: './aggregation-chart.component.html',
  styleUrls: ['./aggregation-chart.component.scss']
})
export class AggregationChartComponent implements OnInit {

  @ViewChild('svg') svgElement: ElementRef;
  @Input() barchartAverageData;
  @Input() barchartMedianData;
  
  
  dat = {
    filterRules: {},
    hasComparativeData: false,
    i18nMap: {
      measurand: "Measurand",
      jobGroup: "Job Group",
      page: "Page",
      comparativeImprovement: "Improvement",
      comparativeDeterioration: "Deterioration"
    },
    series: [
      {
        aggregationValue: "avg",
        browser: null,
        deviceType: null,
        jobGroup: "LokalTest_pal",
        measurand: "DOC_COMPLETE_TIME",
        measurandGroup: "LOAD_TIMES",
        measurandLabel: "Document Complete",
        operatingSystem: null,
        page: "finn",
        unit: "ms",
        value: 1314.2,
        valueComparative: null
      }
    ]
  };

  constructor(private grailsBridgeService: GrailsBridgeService, private aggregationChartDataService: AggregationChartDataService) {
    window.addEventListener("aggregationChartLoaded", () => {
      this.grailsBridgeService.globalOsmNamespace.ChartModules.Aggregation('#aggregation-chart');
    });
  }

  ngOnInit(): void {
    const assetPathAggregationChart: string = this.grailsBridgeService.globalOsmNamespace.assetPaths.aggregationChart;
    this.grailsBridgeService.globalOsmNamespace.postLoader.loadJavascript(assetPathAggregationChart, 'aggregationChart');
  }
}
