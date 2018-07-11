import {Component, Input} from '@angular/core';
import {ScriptDto} from "../../model/script.model";

@Component({
  selector: 'osm-script',
  templateUrl: './script.component.html',
  styleUrls: ['./script.component.scss']
})
export class ScriptComponent {

  @Input() script: ScriptDto;

  constructor() { }

}
