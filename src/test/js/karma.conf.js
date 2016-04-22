// Karma configuration
// Generated on Mon Dec 14 2015 12:27:49 GMT+0100 (CET)

module.exports = function (config) {

    var ASSET_PATH = 'grails-app/assets';
    var TEST_PATH = 'src/test/js';

    config.set({

        // base path that will be used to resolve all patterns (eg. files, exclude)
        basePath: '../../../',


        // frameworks to use
        // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
        frameworks: ['jasmine'],

        // list of files / patterns to load in the browser
        files: [
            { pattern: ASSET_PATH + '/javascripts/jquery/jquery-*.js', watched: true },
            { pattern: ASSET_PATH + '/javascripts/application.js', watched: true },
            { pattern: ASSET_PATH + '/javascripts/csi/defaultMappingCsvValidator.js', watched: true },
            { pattern: TEST_PATH + '/specs/**/*.js', watched: true }
        ],

        // test results reporter to use
        // possible values: 'dots', 'progress', 'remote'
        // available reporters: https://npmjs.org/browse/keyword/karma-reporter
        reporters: ['progress'],
        port: 9876,


        // enable / disable colors in the output (reporters and logs)
        colors: true,


        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_ERROR,


        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,


        // start these browsers
        // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
        browsers: ['PhantomJS'],


        // Continuous Integration mode
        // if true, Karma captures browsers, runs the tests and exits
        singleRun: true,

        // Concurrency level
        // how many browser should be started simultanous
        concurrency: Infinity,

        plugins: [
            'karma-jasmine',
            'karma-phantomjs-launcher',
            'karma-remote-reporter'
        ]
    })
};