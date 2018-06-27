import {Component, Input, OnInit} from '@angular/core';
import {DocumentationEntry} from "../documentation-entry.model";

@Component({
  selector: 'app-documentation-entry',
  templateUrl: './documentation-entry.component.html',
  styleUrls: ['./documentation-entry.component.css']
})
export class DocumentationEntryComponent implements OnInit {
  @Input() entry: DocumentationEntry;

  constructor() {
  }

  ngOnInit() {
  }
}
