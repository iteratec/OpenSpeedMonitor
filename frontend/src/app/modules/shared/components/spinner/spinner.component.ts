import { Component, OnInit, ElementRef, OnDestroy } from '@angular/core';
import { Spinner } from 'spin.js/spin.js';
import { SpinnerOptions } from 'spin.js//SpinnerOptions';
import { Subscription } from 'rxjs';
import {SpinnerService} from "../../services/spinner.service";

@Component({
  selector: 'osm-spinner',
  templateUrl: './spinner.component.html',
  styleUrls: ['./spinner.component.scss']
})
export class SpinnerComponent implements OnInit, OnDestroy {

  private spinner: any;
  private show: boolean = false;
  private element: any = null;
  private subscription: Subscription = null;

  constructor(private spinnerElement: ElementRef, private spinnerService: SpinnerService) {
    this.element = spinnerElement.nativeElement;
  }

  ngOnInit() {
    this.initSpinner();
    this.createServiceSubscription();
  }

  private createServiceSubscription() {
    this.subscription = this.spinnerService.spinnerObservable.subscribe(show => {
      if (show) {
        this.startSpinner();
      } else {
        this.stopSpinner();
      }
    });
  }

  private initSpinner() {
    let options: SpinnerOptions  = {
      lines: 13             // The number of lines to draw
      , length: 10            // The length of each line
      , width: 3              // The line thickness
      , radius: 10            // The radius of the inner circle
      , scale: 1              // Scales overall size of the spinner
      , corners: 1.0          // Corner roundness (0..1)
      , color: '#333333'      // #rgb or #rrggbb or array of colors
      , rotate: 0             // The rotation offset
      , direction: 1          // 1: clockwise, -1: counterclockwise
      , speed: 1              // Rounds per second
      , zIndex: 2e9           // The z-index (defaults to 2000000000)
      , className: 'spinner'  // The CSS class to assign to the spinner
      , top: '50%'            // Top position relative to parent
      , left: '50%'           // Left position relative to parent
      , shadow: false         // Whether to render a shadow
      , position: 'absolute'  // Element positioning
    };
    this.spinner = new Spinner(options);
    this.startSpinner();
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
  }

  startSpinner() {
    this.show = true;
    this.spinner.spin(this.element.firstChild);
    this.element.parentElement.classList.add( "parentSpinner");

  }

  stopSpinner() {
    this.show = false;
    this.spinner.stop();
    this.element.parentElement.classList.remove("parentSpinner");
  }
}
