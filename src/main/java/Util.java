import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tanruixing
 * Created on 2019-04-25
 */
public class Util {
    private static Logger logger = LoggerFactory.getLogger(Util.class);

    public static String jsonEncode(JSONObject jsonObject) {
        return jsonObject.toJSONString();
    }

    public static JSONObject jsonDecode(String jsonStr) {
        try {
            Object obj = new JSONParser().parse(jsonStr);
            if (obj instanceof JSONObject) {
                return (JSONObject)obj;
            }
        } catch (ParseException e) {
            logger.error("jsonDecode jsonStr:{} e:{}", jsonStr, e);
            return  null;
        }

        return null;
    }
}
