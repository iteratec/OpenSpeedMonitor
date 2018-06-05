import {Component, Input, OnInit} from '@angular/core';
import {IPage} from "../../../common/model/page.model";

@Component({
  selector: 'app-page',
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.css']
})
export class PageComponent implements OnInit {
  @Input() page:IPage;
  constructor() { }

  ngOnInit() {
  }

}
