# How to contribute to OpenSpeedMonitor

*Thank you for taking the time* :+1:

There are two major ways in which you are able to contribute this project:

* [Report bugs or suggest improvements](#github-issues)

* [Enhance the codebase](#submit-code)


## GitHub Issues
Submit GitHub Issues to inform us about bugs or desireable improvements. To avoid duplicates it is very much appreciated if you check the existing issues first. Please include as many details as possible in your issue since the OpenSpeedMonitor has an extensive codebase and feature set. The usage of a clear and descriptive title is also encouraged.


## Submit Code

In order to enhance our codebase we strongly recommend the following procedure:
1. Clone this repository.
2. Create your [branch](#branching-guidelines) from *develop*
3. Add or change [code](#coding-guidelines) within your branch.
4. Create a Pull Request  for your branch


### Branching Guidelines
Your branch should be named in a way to describe the issue it is addressing shortly. Depending on your issue please also choose one of the following prefixes :
* *bugfix/[yourBranchNameHere]* 
* *feature/[yourBranchNameHere]*
* *refactoring/[yourBranchNameHere]*

### Coding Guidelines

Generally try to avoid extensive comments instead use descriptive names. [more here](#general-coding-guidelines)

At the moment we are migrating from a classical web application with serverside rendered pages to a web application with Angular frontend. New features are  implemented with Angular in a multipage setup with a slim REST Backend. [more here](#adding-angular-components)

#### Adding Angular Components

Create a module for your component with the help of the Angular CLI within the */frontend* folder or choose an existing module. Create your components within your module also by using the Angular CLI. The only task that cannot be performed by the Angular CLI is testing. Please use the Gradle task "angularTest" instead.

In order to serve your components they need to be added to a view. Every Angular module therefore needs an entry component which is embedded in the view's *.gsp file. The views are found in the */grails-app/views* folder. Controllers with corresponding names implement the REST Endpoints for views. If you add a new view please change the URL Mappings of the Grail Application for its controller to */[yourViewAndControllerNameHere]/rest* manually. You can also access URL parameters within your Angular code by adding a routes to your module and adding a router outlet to its entry component. Check out the "application-dashboard" Angular module and the corresponding "applicationDashboard" Grails view and controller for more info.

#### General Coding Guidelines

* Do not use jQuery within Angular components.
* Use as little Bootstrap as possible within Angular components.
* Define global styles within the styles.scss within the Angular project.




