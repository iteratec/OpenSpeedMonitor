import {BrowserModule} from '@angular/platform-browser';
import {ApplicationRef, Injector, NgModule, NgModuleFactory, SystemJsNgModuleLoader, Type} from '@angular/core';
import {APP_BASE_HREF} from '@angular/common';
import {RouterModule} from '@angular/router';
import {AppRoutingModule} from "./app-routing.module";


@NgModule({
  declarations: [],
  imports: [
    BrowserModule, AppRoutingModule
  ],
  exports: [RouterModule],
  providers: [SystemJsNgModuleLoader,
    {provide: APP_BASE_HREF, useValue: '/'}
  ]
})
export class AppModule {
  constructor(private injector: Injector, private moduleLoader: SystemJsNgModuleLoader) {
  }

  ngDoBootstrap(appRef: ApplicationRef) {
    const widgets = document.querySelectorAll('[data-module-path]');
    for (const i in widgets) {
      if (widgets.hasOwnProperty(i)) {
        const modulePath = (widgets[i] as HTMLElement).getAttribute('data-module-path');
        if (modulePath) {
          this.moduleLoader.load(modulePath)
            .then((moduleFactory: NgModuleFactory<any>) => {
              const ngModuleRef = moduleFactory.create(this.injector);
              ngModuleRef.injector.get('components').forEach((components: Type<{}>[]) => {
                components.forEach((component: Type<{}>) => {
                  const compFactory = ngModuleRef.componentFactoryResolver.resolveComponentFactory(component);
                  if (document.querySelector(compFactory.selector)) {
                    appRef.bootstrap(compFactory);
                  }
                });
              });
            }, (error) => {
              console.error(error);
            });
        }
      }
    }
  }
}
