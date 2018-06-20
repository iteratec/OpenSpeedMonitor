import {Component, Input} from '@angular/core';
import {IScript} from "../../model/script.model";

@Component({
  selector: 'osm-script',
  templateUrl: './script.component.html',
  styleUrls: ['./script.component.css']
})
export class ScriptComponent {

  @Input() script: IScript;

  constructor() { }

}
