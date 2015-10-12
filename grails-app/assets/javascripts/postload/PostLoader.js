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
function PostLoader(){
    var head
    this.init = function(){
        head = document.getElementsByTagName("head")[0];
    }
    this.loadJavascript = function(url, async){

        async = async || true;

        var script = document.createElement("script");
        script.setAttribute("src",url);
        script.setAttribute("type","text/javascript");
        script.setAttribute("async",async);
        //script.setAttribute("charset","ISO-8859-1");
        head.appendChild(script);

    }
    this.loadStylesheet = function(url){
        var link = document.createElement("link");
        link.rel = "stylesheet";
        link.type = "text/css";
        link.href = url;
        link.media = "all";
        head.appendChild(link);
    }
    this.init();
}