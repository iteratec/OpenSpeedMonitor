package de.iteratec.osm

import grails.boot.GrailsApp
import grails.util.Environment
import groovy.transform.CompileDynamic
import org.grails.io.watch.DirectoryWatcher
import org.grails.io.watch.FileExtensionFileChangeListener

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.concurrent.ConcurrentLinkedQueue

class FrontendWatcher {

    private static DirectoryWatcher directoryWatcher

    static void initializeFrontendWatcher() {
        FrontendWatcher frontendWatcher = new FrontendWatcher()
        frontendWatcher.startFrontendWatcher(Environment.getCurrent())
    }

    @CompileDynamic
    protected void startFrontendWatcher(Environment environment) {

        String location = environment.getReloadLocation()
        def nodeLocation = new File(new FileNameByRegexFinder().getFileNames(location, "/build/nodejs/([^\\\\/]*)/bin/node")[0]).getParent()

        if (location && nodeLocation) {

            Thread.start {
                def nodeModulesBin = "${location}/frontend/node_modules/.bin"
                ProcessBuilder watchBuilder = new ProcessBuilder(['sh', '-c', 'ng build --watch'])
                        .redirectErrorStream(true).directory(new File(location, "frontend"))
                watchBuilder.environment().put("PATH", "${nodeModulesBin}:${nodeLocation}")

                Process recompileFrontend = watchBuilder.start()
                recompileFrontend.in.eachLine { line -> println line }
            }

            directoryWatcher = new DirectoryWatcher()

            Queue<File> changedFiles = new ConcurrentLinkedQueue<>()

            directoryWatcher.addListener(new FileExtensionFileChangeListener(['css', 'js']) {
                @Override
                void onChange(File file, List<String> extensions) {
                    changedFiles << file.canonicalFile
                }

                @Override
                void onNew(File file, List<String> extensions) {
                    changedFiles << file.canonicalFile
                }
            })

            directoryWatcher.addWatchDirectory(new File(location, "frontend/dist"), ['css', 'js'])

            Thread.start {
                while (GrailsApp.developmentModeActive) {

                    def uniqueChangedFiles = changedFiles as Set
                    def uniqueChangedFilesSize = uniqueChangedFiles.size()

                    try {
                        if (uniqueChangedFilesSize >= 1) {
                            changedFiles.clear()

                            println "\n Updating ${uniqueChangedFilesSize} frontend files:\n${uniqueChangedFiles}...\n"

                            for (File file : uniqueChangedFiles) {
                                Path src = file.toPath()
                                Path dst

                                if (src.toString().endsWith('.css')) {
                                    dst = Paths.get("${location}/grails-app/assets/stylesheets/frontend/${file.getName()}")
                                } else {
                                    dst = Paths.get("${location}/grails-app/assets/javascripts/frontend/${file.getName()}")
                                }
                                Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING)
                            }
                            sleep(2000)
                        }
                    } catch (Exception e) {
                        log.error("Exception:  $e.message", e)
                    }
                    sleep(2000)
                }
            }
            directoryWatcher.start()
        }
    }
}