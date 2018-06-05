import {Component, Input} from '@angular/core';
import {IScript} from "../../model/script.model";

@Component({
  selector: 'app-script',
  templateUrl: './script.component.html',
  styleUrls: ['./script.component.css']
})
export class ScriptComponent {

  @Input() script: IScript;

  constructor() { }

}
