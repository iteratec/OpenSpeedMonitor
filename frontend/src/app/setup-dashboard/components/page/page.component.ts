import {Component, Input, OnInit} from '@angular/core';
import {IPage} from "../../../shared/models/page.model";

@Component({
  selector: 'osm-page',
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.css']
})
export class PageComponent implements OnInit {
  @Input() page:IPage;
  constructor() { }

  ngOnInit() {
  }

}
