import {Component, Input, OnInit} from '@angular/core';
import {PageDto} from "../../model/page.model";

@Component({
  selector: 'osm-page',
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.scss']
})
export class PageComponent implements OnInit {
  @Input() page: PageDto;
  constructor() { }

  ngOnInit() {
  }

}
