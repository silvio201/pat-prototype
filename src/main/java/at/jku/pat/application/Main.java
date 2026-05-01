package at.jku.pat.application;

import at.jku.pat.analyzer.JavadocAnalyzer;
import at.jku.pat.model.AnalyzeResult;
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
        options.addOption("anyj", true, "AnyJ Threshold");
        options.addOption("l", "level", true, "Java Language Level");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        Path path = null;
        double anyJThreshold = 0;
        int javaLevel = 25; // DEFAULT

        if (cmd.hasOption("path")) {
            path = Paths.get(cmd.getOptionValue("path"));
        } else {
            System.err.println("No path specified, exiting...");
            System.exit(1);
        }
        if (cmd.hasOption("anyj")) {
            try {
                anyJThreshold = Double.parseDouble(cmd.getOptionValue("anyj"));
                if (anyJThreshold <= 0 || anyJThreshold > 1.0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                System.err.println("AnyJ Threshold value not correctly formated (Double value between 0.0 and 1.0)");
                System.exit(1);
            }
        }
        if (cmd.hasOption("level")) {
            try {
                javaLevel = Integer.parseInt(cmd.getOptionValue("level"));
            } catch (NumberFormatException e) {
                System.err.println("Java Level value not correctly formated.");
                System.exit(1);
            }
        }

        AnalyzeResult result = JavadocAnalyzer.analyze(getJavaPaths(path), javaLevel);

        if (anyJThreshold != 0.0 && result.anyJ() < anyJThreshold) {
            System.err.printf("AnyJ Value of %.2f does not meet required %.2f threshold.\n", result.anyJ(), anyJThreshold);
            System.exit(1);
        }
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
