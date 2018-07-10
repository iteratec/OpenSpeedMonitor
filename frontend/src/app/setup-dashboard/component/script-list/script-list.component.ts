import {Component, Input} from '@angular/core';
import {Observable} from "rxjs/internal/Observable";
import {IScript} from "../../model/script.model";
import {ScriptService} from "../../service/rest/script.service";
import {filter, map} from "rxjs/operators";
import {JobGroupDTO} from "../../../shared/model/job-group.model";

@Component({
  selector: 'osm-script-list',
  templateUrl: './script-list.component.html',
  styleUrls: ['./script-list.component.scss']
})
export class ScriptListComponent {
  scriptList$: Observable<IScript[]>;

  @Input() jobGroup: JobGroupDTO;

  constructor(private scriptService: ScriptService) {
    this.scriptList$ = this.scriptService.scripts$.pipe(
      filter(() => !!this.jobGroup),
      map((scripts: IScript[]) => this.filterScriptsByJobGroup(this.jobGroup, scripts))
    )
  }

  private filterScriptsByJobGroup(jobGroup: JobGroupDTO, scripts: IScript[]): IScript[] {
    return scripts.filter(script => script.jobGroupId == jobGroup.id)
  }

}
