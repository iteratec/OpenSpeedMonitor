import {BrowserModule} from '@angular/platform-browser';
import {ApplicationRef, Injector, NgModule, NgModuleFactory, SystemJsNgModuleLoader, Type} from '@angular/core';
import {AppRoutingModule} from "./app-routing.module";
import {APP_BASE_HREF} from '@angular/common';
import {NotFoundComponent} from "./not-found.component";


@NgModule({
  declarations: [NotFoundComponent],
  imports: [
    BrowserModule, AppRoutingModule
  ],
  providers: [SystemJsNgModuleLoader,
    {provide: APP_BASE_HREF, useValue: '/',}
  ],
})
export class AppModule {
  constructor(private injector: Injector, private moduleLoader: SystemJsNgModuleLoader) {
  }

  ngDoBootstrap(appRef: ApplicationRef) {
    const widgets = document.querySelectorAll('[data-module-path]');
    for (const i in widgets) {
      if (!widgets.hasOwnProperty(i)) {
        continue;
      }
      const modulePath = (widgets[i] as HTMLElement).getAttribute('data-module-path');
      if (!modulePath) {
        continue;
      }
      this.moduleLoader.load(modulePath).then((moduleFactory: NgModuleFactory<any>) => {
        this.bootstrapComponentsFromModule(appRef, moduleFactory);
      }, (error) => {
        console.error(error);
      });
    }
  }

  private bootstrapComponentsFromModule(appRef: ApplicationRef, moduleFactory: NgModuleFactory<any>) {
    const ngModuleRef = moduleFactory.create(this.injector);
    ngModuleRef.injector.get('components').forEach((components: Type<{}>[]) => {
      components.forEach((component: Type<{}>) => {
        const compFactory = ngModuleRef.componentFactoryResolver.resolveComponentFactory(component);
        if (document.querySelector(compFactory.selector)) {
          appRef.bootstrap(compFactory);
        }
      });
    });
  }
}
