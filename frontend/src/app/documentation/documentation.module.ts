import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {HttpClientModule} from "@angular/common/http";
import {DocumentationComponent} from "./documentation.component";
import {DocumentationEntryComponent} from "./category/documentation-entry.component";

@NgModule({
  imports: [
    CommonModule, HttpClientModule
  ],
  declarations: [DocumentationComponent, DocumentationEntryComponent],
  providers: [
    {provide: 'components', useValue: [DocumentationComponent], multi: true}
  ],
  entryComponents: [DocumentationComponent]
})
export class DocumentationModule {
}
