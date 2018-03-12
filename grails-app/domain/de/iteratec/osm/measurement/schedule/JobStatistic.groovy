package de.iteratec.osm.measurement.schedule

class JobStatistic {

    Double percentageSuccessfulTestsOfLast5
    Double percentageSuccessfulTestsOfLast25
    Double percentageSuccessfulTestsOfLast150

    static belongsTo = Job

    static constraints = {
        percentageSuccessfulTestsOfLast5(nullable: true)
        percentageSuccessfulTestsOfLast25(nullable: true)
        percentageSuccessfulTestsOfLast150(nullable: true)
    }
    static transients = ['jobStatusLast5CssClass', 'jobStatusLast25CssClass', 'jobStatusLast150CssClass']

    public String getJobStatusLast5CssClass(){
        return getJobStatusCssClass(percentageSuccessfulTestsOfLast5)
    }
    public String getJobStatusLast25CssClass(){
        return getJobStatusCssClass(percentageSuccessfulTestsOfLast25)
    }
    public String getJobStatusLast150CssClass(){
        return getJobStatusCssClass(percentageSuccessfulTestsOfLast150)
    }

    private String getJobStatusCssClass(Double percentageSuccessfulTests){
         if (percentageSuccessfulTests == null){
             return "job-status-nodata"
         }
        String statusLast5CssClass
        if (percentageSuccessfulTests >= 90){
            statusLast5CssClass = "job-status-good"
        }else if (percentageSuccessfulTests >= 80){
            statusLast5CssClass = "job-status-warn"
        }else {
            statusLast5CssClass = "job-status-error"
        }
        return statusLast5CssClass
    }
}
