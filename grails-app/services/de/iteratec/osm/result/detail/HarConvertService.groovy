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

package de.iteratec.osm.result.detail

import de.iteratec.osm.csi.Page
import de.iteratec.osm.result.EventResult
import de.iteratec.osm.result.JobResult

/**
 * HarConvertService
 * The whole purpose of this service is to convert HAR Archives to a readable format for our system
 */
class HarConvertService {

    static transactional = true


    public List<AssetGroup> convertHarToAssetGroups(Map har, JobResult result){
        List<AssetGroup> assetGroups = []
        if (result && har && !har.isEmpty()) {
            har.log.pages.each { page ->
                String harPageId = page.id
                List<Asset> assets = extractAssetListForPage(har, harPageId)
                assetGroups.add(createAssetGroupForPage(page,assets, result))
            }
        }
        return assetGroups
    }

    /**
     * Takes a page map and the matching asset list from har and merges them
     * @param page Page Map from HAR
     * @param assets Asset List from HAR
     * @param jobResult JobResult which belongs to the given har data
     */
    private static AssetGroup createAssetGroupForPage(Map page, List<Asset> assets, JobResult jobResult){
        AssetGroup assetGroup = new AssetGroup(page:getPageIDFromEventName(page._eventName as String?:""),
                jobGroup: jobResult.job.jobGroup.id,
                connectivity: getConnectivity(jobResult),
                jobResult: jobResult.id,
                location: jobResult.job.location.id,
                browser: jobResult.job.location.browser.id,
                assets:assets,
                date: jobResult.date.getTime(),
                cached: page._cached,
                eventName: page._eventName,
                title: page._title)
        return assetGroup
    }


    /**
     * Removes all parameter from a given URL
     * @param url String
     */
    private static String createURLWithoutParams(String url){
        String urlWithoutParams
        def paramIndex = url?.indexOf("?")
        if(paramIndex != null && paramIndex>0){
            urlWithoutParams = url.substring(0,paramIndex)
        } else{
            urlWithoutParams = url
        }
        return urlWithoutParams
    }

    /**
     * Takes the MimeType and splits it into media and subtype.
     * This will will always return a String[] with the size of 2.
     * If one type could'nt not be parsed, it will be "undefined".
     *
     * @param mimeType String
     * @return String[0] = mediaType, String[1]=subtype
     */
    private static String[] convertMimeTypesInAssetMap(String mimeType){
        String[] result = ["undefined","undefined"]
        if(!mimeType) return result

        String[] split = mimeType?.split("/")
        if(split.size()>=1)result[0] = split[0]
        if(split.size()>=2)result[1] = split[1]
        return result
    }

    /**
     * Filter the real event name from the eventName string and returns the ID
     * @param eventName
     * @return
     */
    private static long getPageIDFromEventName(String eventName){
        int pageEndIndex = eventName.indexOf("::")
        String pageName
        if(pageEndIndex>0){
            pageName = eventName.substring(0, pageEndIndex)
        } else{
            pageName = "undefined"
        }
        Page page = Page.findByName(pageName)
        if(page) {
            return page.id
        } else{
            Page.findByName("undefined").id
        }
    }

    /**
     * Get the connectivity name of a jobResult
     * @param result
     * @return
     */
    private static String getConnectivity(JobResult result) {
        //We don't want to persisted the connectivity in two ways, like it is within the jobresults,
        //So we only save the names
        EventResult eventResult = result?.eventResults[0]
        String profile = ""
        if(eventResult){
            if(eventResult.customConnectivityName != null){
                profile = eventResult?.customConnectivityName
            } else{
                profile = eventResult.connectivityProfile.name
            }
        }
        return profile
    }

    /**
     * Searches fo all assets which belongs to a given page id
     * @param har
     * @param pageID PageID do search
     * @return List of all matching Assets
     */
    private static List<Asset> extractAssetListForPage(def har, String pageID) {
        List assetMaps = []
        har.log.entries.each {
            if (it.pageref == pageID) assetMaps << it
        }
        List<Asset> assets = []
        assetMaps.each {
            String[] mimeType = convertMimeTypesInAssetMap(it._contentType as String)
            assets << new Asset(bytesIn: it._bytesIn,
                    bytesOut: it._bytesOut,
                    contentType: it._contentType,
                    connectTimeMs: it._connect_ms,
                    downloadTimeMs: it._download_ms,
                    fullURL: it._full_url,
                    host: it._host,
                    indexWithinHar: it._index,
                    loadTimeMs: it._load_ms,
                    timeToFirstByteMs: it._ttfb_ms,
                    mediaType: mimeType[0],
                    subtype: mimeType[1],
                    sslNegotiationTimeMs: it._ssl_ms,
                    urlWithoutParams: createURLWithoutParams(it._full_url)
            )
        }
        return assets
    }

}
