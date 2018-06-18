package de.iteratec.osm

import grails.boot.GrailsApp
import grails.util.Environment
import groovy.transform.CompileDynamic
import org.grails.io.watch.DirectoryWatcher
import org.grails.io.watch.FileExtensionFileChangeListener

import java.util.concurrent.ConcurrentLinkedQueue

class FrontendWatcher {

    private static DirectoryWatcher directoryWatcher

    static void runFrontendWatcher(Environment environment) {
        if (GrailsApp.developmentModeActive) {
            FrontendWatcher frontendWatcher = new FrontendWatcher()
            frontendWatcher.enableFileWatcher(environment)
        }
    }

    @CompileDynamic
    protected void enableFileWatcher(Environment environment) {

        def location = environment.getReloadLocation()

        if (location) {

            directoryWatcher = new DirectoryWatcher()

            Queue<File> changedFiles = new ConcurrentLinkedQueue<>()

            directoryWatcher.addListener(new FileExtensionFileChangeListener(['ts', 'html', 'css', 'js']) {
                @Override
                void onChange(File file, List<String> extensions) {
                    changedFiles << file.canonicalFile
                }

                @Override
                void onNew(File file, List<String> extensions) {
                    changedFiles << file.canonicalFile
                }
            })

            directoryWatcher.addWatchDirectory(new File(location, "frontend/src"), ['ts', 'css', 'html', 'js'])

            Thread.start {
                while (GrailsApp.developmentModeActive) {

                    def uniqueChangedFiles = changedFiles as Set
                    def uniqueChangedFilesSize = uniqueChangedFiles.size()

                    try {
                        if (uniqueChangedFilesSize >= 1) {
                            changedFiles.clear()

                            println "\n Frontend Files $uniqueChangedFiles changed, recompiling frontend..."

                            def recompileFrontend = new ProcessBuilder(['sh', '-c', './gradlew syncFrontendStylesheets syncFrontendJavascript'])
                                    .redirectErrorStream(true).start()

                            recompileFrontend.in.eachLine { line -> println line }
                            recompileFrontend.waitFor()

                            println('\n Finished frontend recompiling with exit code ' + recompileFrontend.exitValue())
                            sleep(1000)
                        }
                    } catch (Exception e) {
                        log.error("Exception:  $e.message", e)
                    }
                    sleep(5000)
                }
            }
            directoryWatcher.start()
        }
    }
}