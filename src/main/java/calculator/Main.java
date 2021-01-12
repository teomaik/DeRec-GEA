package calculator;

import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class Main {

    public static void main(String[] argv) throws IOException {

        Options options = addOptions();
        CommandLine cmd = getCmd(options, argv);
//        checkArgs(cmd.getArgs());

//        if (cmd.getArgs().length != 0) {
        String projectRoot = cmd.getArgs()[0].replace("\\", "/");
        MetricsCalculator.start(projectRoot);
        System.out.println(IOUtils.toString(MetricsCalculator.printResults()));
//        }
    }

    private static CommandLine getCmd(Options options, String[] args){
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }
        return cmd;
    }

    private static Options addOptions(){
        return new Options();
    }

    private static void checkArgs(String[] argv){
        if(argv.length != 0 && argv.length != 1){
            System.out.println("Error - Missing arguments");
            System.exit(0);
        }
    }

}
