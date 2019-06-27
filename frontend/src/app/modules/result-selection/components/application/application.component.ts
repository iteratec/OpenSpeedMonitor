import {Component, Input, ViewEncapsulation,} from '@angular/core';
import {SelectableApplication} from 'src/app/models/application.model';
import {ResultSelectionStore} from "../../services/result-selection.store";
import {ResultSelectionCommandParameter} from "../../models/result-selection-command.model";
import {UiComponent} from "../../../../enums/ui-component.enum";
import {Observable} from "rxjs";

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

  @Input() resetResultSelectionEvent: Observable<void>;

  constructor(private resultSelectionStore: ResultSelectionStore) {
    this.resultSelectionStore.applications$.subscribe(applications => {
      this.updateApplications(applications.data);
      this.updateTags(applications.data);
      this.filterApplicationsByTag(this.selectedTag);
    });
  }

  ngOnInit() {
    this.resultSelectionStore.registerComponent(UiComponent.APPLICATION);
    this.resetResultSelectionEvent.subscribe(() => this.resetResultSelection())
  }

  onSelectionChange() {
    if (this.selectedApplications) {
      this.resultSelectionStore.setResultSelectionCommandIds(this.selectedApplications, ResultSelectionCommandParameter.APPLICATIONS);
    }
  }

  selectTag(tag: string): void {
    if (tag == this.selectedTag && this.isTagSelected()) {
      this.selectedTag = '';
    } else {
      this.selectedTag = tag;
    }
    this.filterApplicationsByTag(tag);
  }

  isTagSelected(): boolean {
    return this.selectedTag != '';
  }

  updateApplications(applications: SelectableApplication[]): void {
    if (applications != null && applications.length > 0) {
      this.applications = ApplicationComponent.sortByName(applications);
    } else {
      this.applications = [];
    }
  }

  updateTags(applications: SelectableApplication[]) {
    if (applications) {
      this.selectableTags = applications.map(value => value.tags).reduce((a, b) =>
         a.concat(b), []).filter((v, i, a) => 
         a.indexOf(v) === i);
    } else {
      this.selectableTags = [];
    }
    if (this.selectableTags.indexOf(this.selectedTag) == -1) {
      this.selectedTag = '';
    }
  }

  filterApplicationsByTag(tag: string): void {
    let numberOfPreviouslySelectedApplications = this.selectedApplications.length;
    if (this.isTagSelected()) {
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
      this.selectedApplications = this.selectedApplications.filter(item =>
        this.applications.map(item => item.id).includes(item)
      );
      if (this.selectedApplications.length !== numberOfPreviouslySelectedApplications) {
        this.resultSelectionStore.setResultSelectionCommandIds(this.selectedApplications, ResultSelectionCommandParameter.APPLICATIONS);
      }
    }
  }

  private resetResultSelection() {
    this.selectedApplications = [];
    this.selectedTag = '';
    this.resultSelectionStore.setResultSelectionCommandIds(this.selectedApplications, ResultSelectionCommandParameter.APPLICATIONS);
  }

  private static sortByName(applications: SelectableApplication[]): SelectableApplication[] {
    return applications.sort((a, b) => {
      return a.name.localeCompare(b.name);
    });
  }
}
