package application;

import analyzer.JavadocAnalyzer;
import model.AnalyzeOptions;
import model.AnalyzeResult;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("p", "path", true, "Path to root directory of analysis.");
        options.addOption("anyjth", true, "AnyJ Threshold");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        Path path = null;
        double anyJThreshold = 0.8; // DEFAULT

        if (cmd.hasOption("path")) {
            path = Paths.get(cmd.getOptionValue("path"));
        } else {
            System.exit(1);
        }
        if (cmd.hasOption("anyjth")) {
            try {
                anyJThreshold = Double.parseDouble(cmd.getOptionValue("anyjth"));
            } catch (NumberFormatException e) {
                System.err.println("AnyJ Threshold value could not correctly formated (Double value between 0.0 and 1.0");
            }
        }

        AnalyzeResult result = JavadocAnalyzer.analyze(getJavaPaths(path));

        if (result.anyJ() < anyJThreshold) {
            System.err.printf("AnyJ Value of %.2f does not meet required %.2f threshold.", result.anyJ(), anyJThreshold);
            System.exit(1);
        }


        IO.println(String.format("Hello and welcome!"));
    }
    
    private static List<Path> getJavaPaths(Path root) {
        List<Path> result = new ArrayList<>();

        PathMatcher javaMatcher = FileSystems.getDefault().getPathMatcher("glob:**.java");
        try (Stream<Path> paths = Files.walk(root)) {
            paths.forEach(p -> {
                if (javaMatcher.matches(p.getFileName())) {
                    result.add(p);
                }
            });
        } catch (IOException e) {
            System.err.println("An error occurred while traversing directory");
            System.exit(1);
        }

        return result;
    }
}
