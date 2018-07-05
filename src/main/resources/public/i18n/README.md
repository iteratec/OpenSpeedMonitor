**Do not edit i18n json files in this directory manually!**

They get generated automatically from gradle build and all files created or edited manually here get removed.

The frontend i18n json files get created based on grails i18n message bundles (located in `/grails-app/i18n/`).
Every entry in these bundles with the prefix `frontend.` get transfered to frontend json files. 

So, for OpenSpeedMonitor the grails message bundles are the single truth for translated content and everything 
prefixed with `frontend.` is propageted to frontends i18n json bundles. 
