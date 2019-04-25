import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tanruixing
 * Created on 2019-04-24
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("es migrate tools start ...");
        ParseOption parseOption = new ParseOption(args);
        ES srcEs = new ES(ParseOption.srcEs);
        ES dstEs = new ES(ParseOption.dstEs);
        logger.info("srcEs:{} dstEs:{} srcIndex:{} dstIndex:{}", ParseOption.srcEs, ParseOption.dstEs, ParseOption.srcIndex, ParseOption.dstIndex);
        ES.migrateDoc(srcEs, ParseOption.srcIndex, dstEs, ParseOption.dstIndex);
    }
}