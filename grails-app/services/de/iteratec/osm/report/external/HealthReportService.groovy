package de.iteratec.osm.report.external

import com.codahale.metrics.Gauge
import com.codahale.metrics.MetricFilter
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.graphite.Graphite
import com.codahale.metrics.graphite.GraphiteReporter
import com.codahale.metrics.jvm.GarbageCollectorMetricSet
import com.codahale.metrics.jvm.MemoryUsageGaugeSet
import com.codahale.metrics.jvm.ThreadStatesGaugeSet
import org.springframework.boot.actuate.endpoint.SystemPublicMetrics
import org.springframework.boot.actuate.metrics.Metric

import java.lang.management.ManagementFactory
import java.lang.management.OperatingSystemMXBean
import java.util.concurrent.TimeUnit


class HealthReportService {
    def reporterMap = [:]
    SystemPublicMetrics systemPublicMetrics

    def handleGraphiteServer(GraphiteServer graphiteServer){
        if (graphiteServer.reportHealthMetrics){
            try{
                stopReportingToServer(graphiteServer) // remove old report if it exist. Necessary in case only a prefix change
                startReportingToServer(graphiteServer)
            }catch(UnknownHostException e){
                log.warn("Wasn't able to start reporting to GraphiteServer ${graphiteServer.serverAdress}")
            }
        }else{
            try{
                stopReportingToServer(graphiteServer)
            }catch(UnknownHostException e){
                log.warn("Wasn't able to stop reporting to GraphiteServer ${graphiteServer.serverAdress}")

            }
        }

    }

    private startReportingToServer(GraphiteServer graphiteServer){
        log.debug("Trying to start GraphiteReporter for ${graphiteServer.serverAdress}.")
        MetricRegistry metricRegistry = new MetricRegistry()
        metricRegistry.register(graphiteServer.garbageCollectorPrefix, new GarbageCollectorMetricSet())
        metricRegistry.register(graphiteServer.memoryReportPrefix, new MemoryUsageGaugeSet())
        metricRegistry.register(graphiteServer.threadStatesReportPrefix, new ThreadStatesGaugeSet())
        metricRegistry.register(graphiteServer.processCpuLoadPrefix, new Gauge() {
            @Override
            Object getValue() {
                OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                bean.getProcessCpuLoad()
            }
        })
        systemPublicMetrics.metrics().each{  m->

            metricRegistry.register("system."+m.getName(), new Gauge() {

                @Override
                Object getValue() {
                    long result
                    systemPublicMetrics.metrics().each{
                        if (it.getName() == m.getName()) result = it.getValue().longValue()
                    }
                    return result
                }
            })
        }
        final Graphite graphite = new Graphite(new InetSocketAddress(graphiteServer.getServerInetAddress(),graphiteServer.port))
        GraphiteReporter reporter = GraphiteReporter
                .forRegistry(metricRegistry)
                .prefixedWith(graphiteServer.healthMetricsReportPrefix)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .build(graphite)
        reporter.start(graphiteServer.timeBetweenReportsInSeconds, TimeUnit.SECONDS)
        reporterMap[graphiteServer.id]= reporter
        log.debug("Started GraphiteReporter for ${graphiteServer.serverAdress}.")

    }

    private stopReportingToServer(GraphiteServer graphiteServer){
        if(reporterMap[graphiteServer.id]){
            log.debug("Trying to stop GraphiteReporter for ${graphiteServer.serverAdress}.")
            reporterMap[graphiteServer.id].stop()
            reporterMap.remove(graphiteServer.id)
            log.debug("Stoped GraphiteReporter for ${graphiteServer.serverAdress}.")
        }

    }

}
