import {Component, Input} from '@angular/core';
import {Observable} from "rxjs/internal/Observable";
import {IScript} from "../../model/script.model";
import {ScriptService} from "../../service/rest/script.service";
import {filter, map} from "rxjs/operators";
import {JobGroup} from "../../model/job-group.model";

@Component({
  selector: 'app-script-list',
  templateUrl: './script-list.component.html',
  styleUrls: ['./script-list.component.css']
})
export class ScriptListComponent {
  scriptList$: Observable<IScript[]>;

  @Input() jobGroup: JobGroup;

  constructor(private scriptService: ScriptService) {
    this.scriptList$ = this.scriptService.scripts$.pipe(
      filter(() => !!this.jobGroup),
      map((scripts: IScript[]) => this.filterScriptsByJobGroup(this.jobGroup, scripts))
    )
  }

  private filterScriptsByJobGroup(jobGroup: JobGroup, scripts: IScript[]) : IScript[] {
    return scripts.filter(script => script.jobGroupId == jobGroup.id)
  }

}
