# How to contribute to OpenSpeedMonitor

*Thank you for taking the time to contribute* :+1:

There are two major ways in which you are able to contribute this project:

* [Report bugs or suggest improvements](#github-issues)

* [Enhance the codebase](#submit-code)


## GitHub Issues
Submit GitHub Issues to inform us about bugs or desireable improvements. To avoid duplicates it is very much appreciated if you check the existing issues first. Please include as many details as possible in your issue since the OpenSpeedMonitor has an extensive codebase and feature set. The usage of a clear and descriptive title is also encouraged.


## Submit Code

In order to enhance our codebase we strongly recommend the following procedure:
1. Fork this repository, then clone your fork
2. Create your branch from *develop* (see [branching guidelines](#branching-guidelines))
3. Add or change code within your branch (see [coding guidlines](#coding-guidelines))
4. Add Test for your changes and check if all tests still work.
5. Create a Pull Request  for your branch


### Branching Guidelines
Your branch should be named in a way to describe the issue it is addressing shortly. Depending on your issue please also choose one of the following prefixes :
* *bugfix/[yourBranchNameHere]* 
* *feature/[yourBranchNameHere]*
* *refactoring/[yourBranchNameHere]*

### Coding Guidelines

Generally try to avoid extensive comments instead use descriptive names.

At the moment we are migrating from a classical web application with serverside rendered pages to a web application with Angular frontend. New features are  implemented with Angular in a multipage setup with a slim REST Backend. Minor fixes within server pages do not need to use angular.

#### Adding Angular Components

Create a module for your component with the help of the Angular CLI within the */frontend* folder or choose an existing module. Create your components within your module also by using the Angular CLI. The only task that cannot be performed by the Angular CLI is testing. Please use the Gradle task "angularTest" instead.

In order to serve your components they need to be added to a view. Every Angular module therefore needs an entry component which is embedded in the view's *.gsp file. The views are found in the */grails-app/views* folder. Controllers with corresponding names implement the REST Endpoints for views. If you add a new view please change the URL Mappings of the Grails Application for its controller to */[yourViewAndControllerNameHere]/rest* manually. You can also access URL parameters within your Angular code by adding routes to your module and adding a router outlet to its entry component. You can use the "application-dashboard" Angular module and the corresponding "applicationDashboard" Grails view and controller as a reference.

#### General Coding Guidelines

* Do not use jQuery within Angular components.
* Use as little jQuery as possible within the server pages.
* Use as little Bootstrap as possible within Angular components.
* Define global styles within the styles.scss and its sub files within the Angular project (e.g. colors within the colors.scss which is part of the styles.scss).




