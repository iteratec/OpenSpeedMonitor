import {Component, OnInit} from '@angular/core';
import {JobGroupRestService} from "./service/rest/job-group-rest.service";
import {JobGroup, JobGroupFromJson} from "./service/rest/job-group.model";
import {TranslateService} from "@ngx-translate/core";
import {OsmLangService} from "../translation/service/osm-lang.service";

@Component({
  selector: 'app-setup-dashboard',
  templateUrl: './setup-dashboard.component.html',
  styleUrls: ['./setup-dashboard.component.css']
})
export class SetupDashboardComponent implements OnInit {
  activeJobGroups: JobGroup[];

  constructor(private jobGroupRestService: JobGroupRestService, private translate: TranslateService, private osmLangService: OsmLangService) {
    let supportedLangs: string[] = ['en', 'de'];
    translate.addLangs(supportedLangs);
    translate.setDefaultLang('en');

    translate.use(supportedLangs.includes(this.osmLangService.getOsmLang()) ? this.osmLangService.getOsmLang() : translate.getDefaultLang());
  }

  ngOnInit() {
    this.getActiveJobs();
  }

  getActiveJobs() {
    this.jobGroupRestService.getActiveJobGroups().subscribe((activeJobs: any[]) => {
      this.activeJobGroups = activeJobs.map(jobJson => new JobGroupFromJson(jobJson));
    })
  }
}
