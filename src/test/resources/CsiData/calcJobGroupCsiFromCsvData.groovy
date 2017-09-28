import groovy.util.XmlSlurper

File csv = new File('./weekly_page.csv')

Map<String, Map<String, Double>> counters = [:].withDefault{ [:].withDefault{0} }
Map<String, Map<String, Double>> sums = [:].withDefault{ [:].withDefault{0} }

int lineNumber = 0
csv.eachLine{line->

    if(lineNumber > 0){

        List<String> splitted = line.split(';')
        String browser = splitted[0]
        String page = splitted[1]
        String dateWithHour = "${splitted[3]}_${splitted[4][0..1]}"
        String browserPageDateWithHour = "${browser}_${page}_${dateWithHour}"
        Double customerSatisfaction = Double.valueOf(splitted[8])

        counters[page][browserPageDateWithHour]++
        sums[page][browserPageDateWithHour] += customerSatisfaction

    }
    lineNumber++

}

counters.each{currentPage,v->
    Double sumByPage = 0
    Double countByPage = 0
    v.each{currentBrowserPageDateWithHour,count->
        Double avgOfBrowserPageAndHour = Double.valueOf(sums[currentPage][currentBrowserPageDateWithHour]) / Double.valueOf(count)
        sumByPage += avgOfBrowserPageAndHour
        countByPage++
        // println "currentPage=${currentPage} | currentBrowserPageDateWithHour=${currentBrowserPageDateWithHour} | count=${Double.valueOf(count)} | avgOfBrowserPageAndHour=${avgOfBrowserPageAndHour}"
    }
    Double avgByPage = sumByPage / countByPage
    println "page=${currentPage} | sumByPage=${sumByPage} | countByPage=${countByPage} | avgByPage=${avgByPage}"
}

return null
