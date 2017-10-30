def basePath = 'http://localhost:8080'
def runJobPath = basePath + '/rest/job/run/277'
def getResultPath = basePath + '/rest/job/getThresholdResults/'

URL runJobUrl = runJobPath.toURL()

def slurper = new groovy.json.JsonSlurper()
def response = slurper.parseText(runJobUrl.getText())

def testId = response.target

getResultPath = getResultPath + testId
URL getResultUrl = getResultPath.toURL()

for(i = 0; i < 30; i++){
    sleep(5000)
    response = slurper.parseText(getResultUrl.getText())

    if(response.target.status == 200){
        break
    }
}

def allGood = response.target.results.every{
    it.evaluatedResult == "good"
}

def anyBad = response.target.results.any{
    it.evaluatedResult == "bad"
}

result = !anyBad
println result

assert result
