import {Component, Input, OnInit} from '@angular/core';
import {Page} from "../../model/page.model";

@Component({
  selector: 'app-page',
  templateUrl: './page.component.html',
  styleUrls: ['./page.component.css']
})
export class PageComponent implements OnInit {
  @Input() page:Page;
  constructor() { }

  ngOnInit() {
  }

}
