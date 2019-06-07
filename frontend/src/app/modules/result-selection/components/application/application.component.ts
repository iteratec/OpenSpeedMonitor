import {Component, ViewEncapsulation,} from '@angular/core';
import {SelectableApplication} from 'src/app/models/application.model';
import {ResultSelectionStore, UiComponent} from "../../services/result-selection.store";
import {ResultSelectionCommandParameter} from "../../models/result-selection-command.model";

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

  constructor(private resultSelectionStore: ResultSelectionStore) {
    this.resultSelectionStore.registerComponent(UiComponent.APPLICATION);
    this.resultSelectionStore.applications$.subscribe(applications => {
      this.updateApplicationsAndTags(applications.data);
    });
  }

  updateApplicationsAndTags(applications: SelectableApplication[]): void {
    if(applications != null && applications.length > 0) {
      applications.sort((a, b) => {
        return a.name.localeCompare(b.name);
      });
      this.applications = applications;
      this.updateTags(applications);

      if (this.isTagSelected && (this.selectableTags.indexOf(this.selectedTag) > -1)) {
        this.filterApplicationsByTag(this.selectedTag);
      } else {
        this.removeSelection(applications);
      }
    } else {
      this.updateTags(applications);
    }
  }

  selectTag(tag: string): void {
    this.filterApplicationsByTag(tag);
    this.selectedTag = tag;
  }

  isTagSelected(): boolean {
    return this.applications.length != this.filteredApplications.length;
  }

  private updateTags(applications: SelectableApplication[]) {
    if(applications) {
      this.selectableTags = applications.map(value => value.tags).reduce((a, b) =>
         a.concat(b), []).filter((v, i, a) => 
         a.indexOf(v) === i);
    } else {
      this.selectableTags = [];
    }
  }

  private filterApplicationsByTag(tag: string): void {
    if (tag !== this.selectedTag) {
      let numberOfPreviouslySelectedApplications = this.selectedApplications.length;
      let numberOfSelectedApplications = 0;
      if (this.applications) {
        this.filteredApplications = this.applications.filter((app: SelectableApplication) => app.tags.indexOf(tag) > -1);
        this.selectedApplications = this.selectedApplications.filter((selectedAppId: number) =>
          this.filteredApplications.map(item => item.id).includes(selectedAppId)
        );
      }
      if (this.selectedApplications.length !== numberOfPreviouslySelectedApplications) {
        this.resultSelectionStore.setResultSelectionCommandIds(this.selectedApplications, ResultSelectionCommandParameter.APPLICATIONS);
      }
    } else {
      this.filteredApplications = this.applications;
    }
  }

  private removeSelection(applications: SelectableApplication[]) {
    this.filteredApplications = applications;
    let numberOfPreviouslySelectedApplications = this.selectedApplications.length;
    this.selectedApplications = this.selectedApplications.filter(item =>
      applications.map(item => item.id).includes(item)
    );
    if(this.selectedApplications.length !== numberOfPreviouslySelectedApplications) {
      this.resultSelectionStore.setResultSelectionCommandIds(this.selectedApplications, ResultSelectionCommandParameter.APPLICATIONS);
    }
  }

  onSelectionChange() {
    if(this.selectedApplications) {
      this.resultSelectionStore.setResultSelectionCommandIds(this.selectedApplications, ResultSelectionCommandParameter.APPLICATIONS);
    }
  }
}
