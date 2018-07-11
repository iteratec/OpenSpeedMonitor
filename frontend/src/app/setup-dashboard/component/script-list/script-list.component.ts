import {Component, Input} from '@angular/core';
import {Observable} from "rxjs/internal/Observable";
import {ScriptDto} from "../../model/script.model";
import {ScriptService} from "../../service/rest/script.service";
import {filter, map} from "rxjs/operators";
import {JobGroupDTO} from "../../model/job-group.model";

@Component({
  selector: 'osm-script-list',
  templateUrl: './script-list.component.html',
  styleUrls: ['./script-list.component.scss']
})
export class ScriptListComponent {
  scriptList$: Observable<ScriptDto[]>;

  @Input() jobGroup: JobGroupDTO;

  constructor(private scriptService: ScriptService) {
    this.scriptList$ = this.scriptService.scripts$.pipe(
      filter(() => !!this.jobGroup),
      map((scripts: ScriptDto[]) => this.filterScriptsByJobGroup(this.jobGroup, scripts))
    )
  }

  private filterScriptsByJobGroup(jobGroup: JobGroupDTO, scripts: ScriptDto[]): ScriptDto[] {
    return scripts.filter(script => script.jobGroupId == jobGroup.id)
  }

}
