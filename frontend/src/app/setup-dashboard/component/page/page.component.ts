import {Component, Input, OnInit} from '@angular/core';
import {PageDto} from "../../../shared/model/page.model";

@Component({
  selector: 'osm-page',
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.css']
})
export class PageComponent implements OnInit {
  @Input() page: PageDto;
  constructor() { }

  ngOnInit() {
  }

}
