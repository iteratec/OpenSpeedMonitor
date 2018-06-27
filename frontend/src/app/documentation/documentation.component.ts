import {Component, OnInit} from '@angular/core';
import {DocumentationEntry} from "./documentation-entry.model";

@Component({
  selector: 'app-documentation',
  templateUrl: './documentation.component.html',
  styleUrls: ['./documentation.component.css']
})
export class DocumentationComponent implements OnInit {

  entries: DocumentationEntry[] = [
    {
      title: 'OpenSpeedMonitor', text: this.loremIpsum(),
      links: [
        {text: 'Homepage', url: 'http://openspeedmonitor.de/'},
        {url: 'https://github.com/iteratec/OpenSpeedMonitor', text: 'Github'},
        {url: 'https://github.com/iteratec/OpenSpeedMonitor/releases', text: 'Release notes'}
      ],
      imgUrl: '/assets/OpenSpeedMonitor_dark.svg'
    },
    {
      title: 'Rest API', text: this.loremIpsum(),
      links: [
        {text: 'API', url: '/rest/man'}
      ],
      imgUrl: 'https://swagger.io/swagger/media/assets/images/swagger_logo.svg'
    },
    {
      title: 'Scripts', text: this.loremIpsum(),
      links: [
        {
          text: 'Documentation', url: 'https://sites.google.com/a/webpagetest.org/docs/using-webpagetest/scripting'
        },
      ],
      imgUrl: 'https://images.pexels.com/photos/270366/pexels-photo-270366.jpeg?auto=compress'
    },
    {
      title: 'WebPagetest', text: this.loremIpsum(),
      links: [
        {text: 'Documentation', url: 'https://sites.google.com/a/webpagetest.org/docs/using-webpagetest/'},
        {text: 'Metrics', url: 'https://sites.google.com/a/webpagetest.org/docs/using-webpagetest/metrics'},
      ],
      imgUrl: 'https://developers.google.com/web/fundamentals/performance/speed-tools/images/tool-webpagetest.svg'
    },
    {
      title: 'DetailAnalysis', text: this.loremIpsum(),
      links: [
        {text: 'Github', url: 'https://github.com/iteratec/OsmDetailAnalysis'},
      ],
      imgUrl: '/assets/OpenSpeedMonitor_dark.svg'
    }
  ];

  loremIpsum(): string {
    return `Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aenean quis scelerisque diam. 
    Cras blandit purus tortor, at sagittis erat lobortis quis. Sed vel nisl id elit tincidunt tempor. 
    Nam diam justo, aliquet sit amet tortor vehicula, laoreet sodales magna. Integer congue quis leo vel tristique.`
  }

  constructor() {
  }

  ngOnInit() {
  }
}
