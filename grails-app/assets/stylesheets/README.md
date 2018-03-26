# Stylesheets and Asset Pipeline

## Guidelines
* Include third party components that don't need to be adapted via the asset pipelines `require` directive.
* Libraries that are adapted, like bootstrap, should be imported via less' `@import` statement. This suppresses
  duplicate code generation.
* If you design new global design elements, put the code in an extra file in this directory and `@import` it in
  [openspeedmonitor.less](openspeedmonitor.less), like
  [sidebar.less](sidebar.less), [cards.less](cards.less) and [variables-corporate.less](variables-corporate.less).

## File structure
 * [application.less](application.less) defines the global stylesheet bundle. No code belongs here.
 * [openspeedmonitor.less](openspeedmonitor.less) defines our main application styles, imports, adapts and uses
   bootstrap.
 * [node_modules/](node_modules) contains third party libraries and is generated via bower and gradle.
   Never put your own code in here.
 * Other files that are not part of the global bundle should be located in a separate directory named after the view
   it's used in.
