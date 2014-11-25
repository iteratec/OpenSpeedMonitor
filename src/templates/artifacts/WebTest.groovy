/* 
* OpenSpeedMonitor (OSM)
* Copyright 2014 iteratec GmbH
* 
* Licensed under the Apache License, Version 2.0 (the "License"); 
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
* 	http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software 
* distributed under the License is distributed on an "AS IS" BASIS, 
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
* See the License for the specific language governing permissions and 
* limitations under the License.
*/

class @webtest.name.caps@Test extends grails.util.WebTest {

    // Unlike unit tests, functional tests are often sequence dependent.
    // Specify that sequence here.
    void suite() {
        test@webtest.name.caps@ListNewDelete()
        // add tests for more operations here
    }

    def test@webtest.name.caps@ListNewDelete() {
        webtest('@webtest.name.caps@ basic operations: view list, create new entry, view, edit, delete, view') {
            invoke(url:'@webtest.name.lower@')
            verifyText(text:'Home')

            verifyListPage(0)

            clickLink(label:'New @webtest.name.caps@')
            verifyText(text:'Create @webtest.name.caps@')
            clickButton(label:'Create')
            verifyText(text:'Show @webtest.name.caps@', description:'Detail page')
            clickLink(label:'List', description:'Back to list view')

            verifyListPage(1)

            group(description:'edit the one element') {
                clickLink(label:'Show', description:'go to detail view')
                clickButton(label:'Edit')
                verifyText(text:'Edit @webtest.name.caps@')
                clickButton(label:'Update')
                verifyText(text:'Show @webtest.name.caps@')
                clickLink(label:'List', description:'Back to list view')
            }

            verifyListPage(1)

            group(description:'delete the only element') {
                clickLink(label:'Show', description:'go to detail view')
                clickButton(label:'Delete')
                verifyXPath(xpath:"//div[@class='message']", text:/@webtest.name.caps@.*deleted./, regex:true)
            }

            verifyListPage(0)
        }
    }

    String ROW_COUNT_XPATH = "count(//td[@class='actionButtons']/..)"

    def verifyListPage(int count) {
        ant.group(description:"verify @webtest.name.caps@ list view with $count row(s)") {
            verifyText(text:'@webtest.name.caps@ List')
            verifyXPath(xpath:ROW_COUNT_XPATH, text:count, description:"$count row(s) of data expected")
        }
    }
}