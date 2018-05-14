# Frontend

The preceding proof of concept (which can be found at [/poc/newFrontendAngular/frontend](https://github.com/iteratec/OpenSpeedMonitor/tree/poc/newFrontendAngular/frontend)) has been done to integrate an Angular frontend into the application. With the release of Angular 6 the proof of concept has also been overhauled. Angular will be used as a multi-page application with self-contained modules, instead of a single page application. 

Additionally, the modules will be lazy loaded only when needed.

## Modules
Modules specify which components should be used when they are loaded. The components need to be imported and declared.
> Currently inside a module the used components have to be declared as `providers` for the injector to find them. This way seems to be deprecated and probably needs to be changed if possible. Injection tokens are a way to replace the deprecated method.

```typescript
import { InjectionToken, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SetupDashboardComponent } from './setup-dashboard.component';

@NgModule({
  imports: [
    CommonModule
  ],
  declarations: [SetupDashboardComponent],
  providers: [
    { provide: 'components', useValue: [SetupDashboardComponent], multi: true }
  ],
  entryComponents: [SetupDashboardComponent]
})
export class SetupDashboardModule { }

```
## Components
Components function as expected and they have not been modified from standard Angular 6.

## File structure

```json
frontend
├── dist (output folder)
├── e2e
├── node_modules
├── src
│   ├── app
│   │   ├── setup-dashboard
│   │   │   ├── setup-dashboard.component.css
│   │   │   ├── setup-dashboard.component.html
│   │   │   ├── setup-dashboard.component.spec.ts
│   │   │   ├── setup-dashboard.component.ts
│   │   │   ├── setup-dashboard.module.spec.ts
│   │   │   └── setup-dashboard.module.ts
│   │   ├── app.component.css
│   │   ├── app.component.html
│   │   ├── app.component.spec.ts
│   │   ├── app.component.ts
│   │   └── app.module.ts
│   ├── assets
│   ├── environments
│   │   ├── environment.prod.ts
│   │   └── environment.ts
│   ├── browserslist
│   ├── favicon.ico
│   ├── index.html
│   ├── karma.conf.js
│   ├── main.ts
│   ├── polyfills.ts
│   ├── styles.css
│   ├── test.ts
│   ├── tsconfig.app.json
│   ├── tsconfig.spec.json
│   └── tslint.json
├── angular.json
├── package.json
├── package-lock.json
├── README.md
├── tsconfig.json
└── tslint.json
```
Seen above is the current file structure for the frontend directory. In this case `src/app/setup-dashboard` contains the module which will be loaded later. This module must be declared as a `LazyModule` in `angular.json`.

## Configuration: angular.json
The modules to be loaded later must be declared in `angular.json`. This is done by declaring the path to the module in the `lazyModules` array. The `.ts` ending is to be omitted. For the current setup the `deployUrl` must also be set. This is so that the web server later finds the modules under the correct path.

```json
"projects": {
    "frontend": {
        "build": {
          "options": {
            "deployUrl": "/assets/frontend/",
              ...
            "lazyModules": [ "src/app/setup-dashboard/setup-dashboard.module" ]
              ...
```

## Embedding a module
To include a module into the page it needs to be declared inside a HTML-Tag. The necessary resources also need to be included into the page or loaded globally. Here our module `setup-dashboard` will be included into it's `gsp` page. `app-setup-dashboard` belongs to the associated selector of a component. With `data-module-path` the module to be used is declared. The path needs to be the same as the one in the `angular.json` with the addition of the class name after a `#`. In this case the class name is `SetupDashboardModule`.

By doing this the module will be loaded dynamically and there is no need to specifically declare the filename of the built module. The assets (declared with `asset:`) are digested by the grails asset pipeline and are hashed and compressed.
```html
<html xmlns="http://www.w3.org/1999/html">
<head>
    <title><g:message code="default.product.title"/></title>
</head>

<body>
<app-setup-dashboard data-module-path="src/app/setup-dashboard/setup-dashboard.module#SetupDashboardModule"></app-setup-dashboard>

<asset:stylesheet src="frontend/styles.css"/>
<asset:javascript src="frontend/runtime.js"/>
<asset:javascript src="frontend/polyfills.js"/>
<asset:javascript src="frontend/main.js"/>

</body>

</html>
```
## Building
For the building process the `build.gradle` in `grails-app` has been modified to include the built Angular assets.
```groovy
task installFrontendDependencies(type: NpmTask) {
    description = "Installs frontend package dependencies"
    workingDir = file("${project.projectDir}/frontend")
    args = ['install']
}

task compileFrontend(type: NpmTask, dependsOn: installFrontendDependencies) {
    description = "Compiles frontend"
    workingDir = file("${project.projectDir}/frontend")
    args = ['run', 'build:prod']
}

task syncFrontendJavascripts(type: Sync, dependsOn: compileFrontend) {
    description = "Syncs frontend javascript files"
    from 'frontend/dist/frontend'
    include '*.js'
    into 'grails-app/assets/javascripts/frontend'
    rename('(main|polyfills|runtime)\\..*\\.js', '$1.js')
}

task syncFrontendStylesheets(type: Sync, dependsOn: compileFrontend) {
    description = "Syncs frontend stylesheet files"
    from 'frontend/dist/frontend'
    include '*.css'
    into 'grails-app/assets/stylesheets/frontend'
    rename('(styles)\\..*\\.css', '$1.css')
}
```

***
### Information

[Blog 1: What's new in Angular CLI 6.0](https://blog.ninja-squad.com/2018/05/04/angular-cli-6.0/)

[Blog 2: Angular lazy loading](https://blog.novatec-gmbh.de/angular-2-in-a-multi-page-application/)

[Blog 3: Angular Elements with Angular 6](https://medium.com/@tomsu/building-web-components-with-angular-elements-746cd2a38d5b)

# Angular CLI specific

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 6.0.0.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
