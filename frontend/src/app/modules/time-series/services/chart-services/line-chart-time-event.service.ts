import {Injectable} from '@angular/core';
import {EventDTO, TimeEvent} from '../../models/event.model';
import {
  BaseType as D3BaseType,
  ContainerElement as D3ContainerElement,
  select as d3Select,
  Selection as D3Selection
} from 'd3-selection';
import {ScaleTime as D3ScaleTime} from 'd3-scale';
import {parseDate} from '../../../../utils/date.util';

@Injectable({
  providedIn: 'root'
})
export class LineChartTimeEventService {

  constructor() {
  }

  addEventMarkerGroupToChart(chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>) {
    chart.append('g').attr('id', 'event-marker-group');
  }

  addEventMarkerTooltipBoxToSvgParent() {
    d3Select('#time-series-chart').select(function () {
      return (<SVGElement>this).parentNode;
    }).append('div')
      .attr('id', 'event-marker-tooltip')
      .style('opacity', '0.9');
  }

  addEventMarkerToChart(chart: D3Selection<D3BaseType, {}, D3ContainerElement, {}>,
                        xScale: D3ScaleTime<number, number>,
                        events: EventDTO[],
                        width: number,
                        height: number,
                        margin: { [key: string]: number }): void {
    const eventMarkerGroup = d3Select('#event-marker-group')
      .selectAll('g')
      .data(events, (d: EventDTO) => {
        // fixme This key is not unique. EventDTO has no unique date.
        return d.eventDate.toString();
      })
      .join(
        enter => {
          const eventMarker = enter
            .append('g')
            .attr('class', 'event-marker');
          eventMarker
            .append('line')
            .attr('class', 'event-marker-line')
            .style('opacity', '0')
            .attr('y1', 0)
            .attr('y2', height);
          eventMarker
            .append('circle')
            .attr('class', 'event-marker-dot')
            .style('cursor', 'pointer')
            .on('mouseover', (event: EventDTO, index: number, nodes: []) => this.showEventMarkerTooltip(event, index, nodes, width, margin))
            .on('mouseout', () => d3Select('#event-marker-tooltip').style('opacity', 0))
            .on('click', (_, index: number, nodes: []) => this.changeLineVisibility(index, nodes))
            .attr('cy', height)
            .attr('r', 8);
          return eventMarker;
        }
      );

    eventMarkerGroup.selectAll('.event-marker-line')
      .attr('x1', (event: EventDTO) => xScale(event.eventDate))
      .attr('x2', (event: EventDTO) => xScale(event.eventDate));
    eventMarkerGroup.selectAll('.event-marker-dot')
      .attr('cx', (event: EventDTO) => xScale(parseDate(event.eventDate)));
  }

  private showEventMarkerTooltip(event: EventDTO,
                                 index: number, nodes: [],
                                 width: number,
                                 margin: { [key: string]: number }): D3Selection<SVGCircleElement, EventDTO, D3BaseType, unknown> {
    const eventMarkerTooltipBox = d3Select('#event-marker-tooltip');
    eventMarkerTooltipBox.style('opacity', '0.9');
    eventMarkerTooltipBox.html(this.createEventMarkerTooltipContent(event).outerHTML);

    const circle = d3Select(nodes[index]);
    const top = parseFloat(circle.attr('cy')) + margin.top;

    const tooltipWidth: number = (<HTMLDivElement>eventMarkerTooltipBox.node()).getBoundingClientRect().width;
    const xPos = parseFloat(circle.attr('cx'));
    const left = (tooltipWidth + xPos > width) ? xPos - tooltipWidth + margin.right + 10 : xPos + margin.left + 50;
    eventMarkerTooltipBox.style('top', top + 'px');
    eventMarkerTooltipBox.style('left', left + 'px');

    return nodes[index];
  }

  private changeLineVisibility(index: number, nodes) {
    const eventMarkerLineSelection = d3Select(nodes[index].parentNode).select('.event-marker-line');
    if (eventMarkerLineSelection.style('opacity') === '1') {
      eventMarkerLineSelection.style('opacity', '0');
    } else {
      eventMarkerLineSelection.style('opacity', '1');
    }
  }

  private createEventMarkerTooltipContent(event: EventDTO): HTMLDivElement {
    const container: HTMLDivElement = document.createElement('div');
    container.className = 'gridContainer';

    const dateItem = document.createElement('div');
    dateItem.append(event.eventDate.toLocaleString());
    container.append(dateItem);

    const shortNameItem = document.createElement('div');
    const shortNameParagraph = document.createElement('p');
    shortNameParagraph.style.fontWeight = 'bold';
    shortNameParagraph.append(event.shortName + ':');
    shortNameItem.append(shortNameParagraph);
    container.append(shortNameItem);

    const descriptionItem = document.createElement('div');
    const descriptionParagraph = document.createElement('p');
    descriptionParagraph.append(event.description);
    descriptionItem.append(descriptionParagraph);
    container.append(descriptionItem);

    return container;
  }
}
