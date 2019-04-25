import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author tanruixing
 * Created on 2019-04-24
 */
public class ParseOption {
    private static final Logger logger = LoggerFactory.getLogger(ParseOption.class);
    private static final String seOp = "se";
    private static final String deOp = "de";
    private static final String siOp = "si";
    private static final String diOp = "di";

    public static String srcEs;
    public static String srcIndex;
    public static String dstEs;
    public static String dstIndex;

    public ParseOption(String[] args) {
        try {
            Options ops = new Options();
            Option opSrcEs = new Option(seOp, "src-es", true, "src es address ip:port");
            Option opDstEs = new Option(deOp, "dst-es", true, "dst es address");
            Option opSrcIndex = new Option(siOp, "src-index", true, "src es index name");
            Option opDstIndex = new Option(diOp, "dst-index", true, "dst es index name");

            opSrcEs.setRequired(true);
            opDstEs.setRequired(true);
            opSrcIndex.setRequired(true);
            opDstIndex.setRequired(true);

            ops.addOption(opSrcEs);
            ops.addOption(opDstEs);
            ops.addOption(opSrcIndex);
            ops.addOption(opDstIndex);

            CommandLineParser parser = new GnuParser();
            CommandLine commandLine = parser.parse(ops, args);
            if (commandLine.hasOption(seOp)) {
                srcEs = commandLine.getOptionValue(seOp);
            }

            if (commandLine.hasOption(deOp)) {
                dstEs = commandLine.getOptionValue(deOp);
            }

            if (commandLine.hasOption(siOp)) {
                srcIndex = commandLine.getOptionValue(siOp);
            }

            if (commandLine.hasOption(diOp)) {
                dstIndex = commandLine.getOptionValue(diOp);
            }
        } catch (ParseException e) {
            logger.error("parse.parse error:{}", e);
            System.exit(-1);
        }
    }
}
