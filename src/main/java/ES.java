import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.json.simple.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.json.simple.JSONObject;

/**
 * @author tanruixing
 * Created on 2019-04-24
 */
public class ES {
    private static Logger logger = LoggerFactory.getLogger(ES.class);
    private List<HttpHost> httpHostList = new ArrayList<>();
    private RestClient client;

    public ES(String esNodeListStr) {
        List<String> esNodeList = Arrays.asList(esNodeListStr.split(","));
        for (String esNode : esNodeList) {
            String httpHost = esNode.split(":")[0];
            Integer httpPort = Integer.parseInt(esNode.split(":")[1]);
            httpHostList.add(new HttpHost(httpHost, httpPort));
        }
        HttpHost[] httpHosts = httpHostList.toArray(new HttpHost[httpHostList.size()]);
        RestClientBuilder builder = RestClient.builder(httpHosts);
        client = builder.build();
    }

    public static RestClient getRestClient(List<HttpHost> httpHostList) {
        HttpHost[] httpHosts = httpHostList.toArray(new HttpHost[httpHostList.size()]);
        RestClientBuilder builder = RestClient.builder(httpHosts);
        RestClient client = builder.build();
        return client;
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            logger.error("es close error");
        }
    }

    public static JSONArray getDocsFromScrollRsp(String response) {
        JSONObject jsonObject = Util.jsonDecode(response);
        Object level1Obj = jsonObject.get("hits");
        if (level1Obj instanceof JSONObject) {
            JSONObject hitsLevel1Obj = (JSONObject)level1Obj;
            Object level2Obj = hitsLevel1Obj.get("hits");
            if (level2Obj instanceof JSONArray) {
                JSONArray hitsLevel2Obj = (JSONArray)level2Obj;
                return hitsLevel2Obj;
            } else {
                logger.error("this is not a correct scroll response in level2:{}", response);
                return null;
            }
        } else {
            logger.error("this is not a correct scroll response in level1:{}", response);
            return null;
        }
    }

    public static String getIndexDocsBody(JSONArray hitDocs, String dstIndex) {
       String body = "";
       for (Object hitDoc : hitDocs) {
           if (hitDoc instanceof JSONObject) {
               JSONObject hitDocObj = (JSONObject)hitDoc;
               JSONObject indexDocObj = new JSONObject();
               indexDocObj.put("_index", dstIndex);
               indexDocObj.put("_type", hitDocObj.get("_type"));
               indexDocObj.put("_id", hitDocObj.get("_id"));
               JSONObject indexObj = new JSONObject();
               indexObj.put("index", indexDocObj);
               body += Util.jsonEncode(indexObj) + "\n";
               Object sourceObj = hitDocObj.get("_source");
               if (sourceObj instanceof JSONObject) {
                   JSONObject sourceJsonObj = (JSONObject)sourceObj;
                   body += Util.jsonEncode(sourceJsonObj) + "\n";
               } else {
                   logger.error("parse response sourceObj is not json");
                   break;
               }
           }
       }
       return body;
    }

    public static void migrateDoc(ES srcEs, String srcIndex, ES dstEs, String dstIndex) {
        String response = srcEs.scrollDoc(srcIndex);
        JSONObject jsonObject = Util.jsonDecode(response);
        String scrollId = jsonObject.get("_scroll_id").toString();
        do {
            JSONArray hitDocs = getDocsFromScrollRsp(response);
            if (hitDocs.size() == 0) {
                break;
            }
            String body = getIndexDocsBody(hitDocs, dstIndex);
            dstEs.bulkDoc(body);
            response = srcEs.scrollDocById(srcIndex, scrollId);
        } while (true);

        logger.info("migrateDoc finish");
        srcEs.close();
        dstEs.close();
    }

    public String scrollDoc(String indexName) {
        return scrollDocById(indexName, null);
    }

    public String scrollDocById(String indexName, String scrollId) {
        String uri = "/" + indexName + "/_search?scroll=1m";
        String body = null;

        if (scrollId != null) {
            JSONObject jsonObject = new JSONObject();
            uri = "/_search/scroll";
            jsonObject.put("scroll", "1m");
            jsonObject.put("scroll_id", scrollId);
            body = jsonObject.toJSONString();
        }

        Request request = new Request("POST", uri);
        request.setJsonEntity(body);

        try {
            Response response = this.client.performRequest(request);
            int statusCode = response.getStatusLine().getStatusCode();
            String content = EntityUtils.toString(response.getEntity());
            if (statusCode != 200) {
                logger.error("request error statusCode:{} content:{}", statusCode, content);
                return null;
            }

            return content;
        } catch (IOException e) {
            logger.error("error performRequest uri:{} e:{}", uri, e);
            return null;
        }
    }

    public void bulkDoc(String body) {
        String uri = "/_bulk";
        Request request = new Request("POST", uri);
        request.setJsonEntity(body);
        try {
            Response response = this.client.performRequest(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                logger.info("bulk docs success done!");
                return;
            }
        } catch (IOException e) {
            logger.error("error performRequest bulkDoc uri:{} e:{}", uri, e);
            return;
        }
    }
}
