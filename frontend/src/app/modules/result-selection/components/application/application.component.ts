import {Component, ViewEncapsulation,} from '@angular/core';
import {SelectableApplication} from 'src/app/models/application.model';
import {ResultSelectionStore, UiComponent} from "../../services/result-selection.store";

@Component({
    selector: 'osm-result-selection-application',
    templateUrl: './application.component.html',
    styleUrls: ['./application.component.scss'],
    encapsulation: ViewEncapsulation.None
  })

export class ApplicationComponent {
  applications: SelectableApplication[];
  filteredApplications: SelectableApplication[];
  selectedApplications: number[] = [];
  selectableTags: string[];
  selectedTag: string = '';

  isEmpty = true;
  isSelected = false;

  constructor(private resultSelectionStore: ResultSelectionStore) {
    this.resultSelectionStore.registerComponent(UiComponent.APPLICATION);
    this.resultSelectionStore.applications$.subscribe(applications => {
      if(applications) {
        this.updateApplicationsAndTags(applications);
      }
    });
  }

  filterByTag(tag: string): void {
    if(!this.isSelected) {
      this.isSelected = true;
      this.selectedTag = tag;
      this.setFilteredApplications(this.selectedTag);
    } else if(tag !== this.selectedTag) {
      this.selectedTag = tag;
      this.setFilteredApplications(this.selectedTag);
    } else {
      this.isSelected = false;
      this.filteredApplications = this.applications;
    }
  }

  updateApplicationsAndTags(applications: SelectableApplication[]): void {
    if(applications != null && applications.length > 0) {
      this.isEmpty = false;
      applications.sort((a, b) => {
        return a.name.localeCompare(b.name);
      });
      this.applications = applications;
      this.updateTags(this.applications);

      if (this.isSelected && (this.selectableTags.indexOf(this.selectedTag) > -1)) {
        this.setFilteredApplications(this.selectedTag);
      } else {
        this.removeSelection(applications);
      }
    } else {
      this.isEmpty = true;
      this.updateTags(applications);
    }
  }

  private updateTags(applications: SelectableApplication[]) {
    if(applications){
      this.selectableTags = applications.map(value => value.tags).reduce((a, b) =>
         a.concat(b), []).filter((v, i, a) => 
         a.indexOf(v) === i);
    } else {
      this.selectableTags = [];
    }
  }

  private setFilteredApplications(tag: string): void {
    let numberOfPreviouslySelectedApplications = this.selectedApplications.length;
    let numberOfSelectedApplications = 0;

    if(this.applications) {
      let filteredApplications = [];
      this.applications.forEach(element => {
        if(element.tags.indexOf(tag) > -1) {
          filteredApplications.push(element);
        }
      });

      this.selectedApplications = this.selectedApplications.filter(item =>
        filteredApplications.map(item => item.id).includes(item)
      );
      this.filteredApplications = filteredApplications;
    }

    if(this.selectedApplications.length !== numberOfPreviouslySelectedApplications) {
      this.resultSelectionStore.setSelectedApplications(this.selectedApplications);
    }
  }

  private removeSelection(applications: SelectableApplication[]) {
    this.isSelected = false;
    this.filteredApplications = applications;
    let numberOfPreviouslySelectedApplications = this.selectedApplications.length;
    this.selectedApplications = this.selectedApplications.filter(item =>
      applications.map(item => item.id).includes(item)
    );
    if(this.selectedApplications.length !== numberOfPreviouslySelectedApplications) {
      this.resultSelectionStore.setSelectedApplications(this.selectedApplications);
    }
  }

  onSelectionChange() {
    if(this.selectedApplications) {
      this.resultSelectionStore.setSelectedApplications(this.selectedApplications);
    }
  }
}
