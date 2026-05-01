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
        options.addOption("p", "path", true, "Path to directory with Java Files (Can be used multiple times)");
        options.addOption("anyj", true, "AnyJ Threshold");
        options.addOption("l", "level", true, "Java Language Level");
        options.addOption("x", "exclude", true, "Path to excluded Java Files");
        options.addOption("dir", true, "Dir Threshold");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        List<Path> paths = new ArrayList<>();
        List<Path> pathsToExclude = new ArrayList<>();
        double anyJThreshold = 0;
        double dirThreshold = 0;
        int javaLevel = 25; // DEFAULT

        for (Option opt : cmd.getOptions()) {
            if (opt.getOpt().equals("p")) {
                paths.add(Paths.get(opt.getValue()));
            } else if (opt.getOpt().equals("anyj")) {
                try {
                    anyJThreshold = Double.parseDouble(opt.getValue());
                    if (anyJThreshold <= 0 || anyJThreshold > 1.0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    System.err.println("AnyJ Threshold value not correctly formated (Double value between 0.0 and 1.0)");
                    System.exit(1);
                }
            } else if (opt.getOpt().equals("l")) {
                try {
                    javaLevel = Integer.parseInt(cmd.getOptionValue("level"));
                } catch (NumberFormatException e) {
                    System.err.println("Java Level value not correctly formated.");
                    System.exit(1);
                }
            } else if (opt.getOpt().equals("x")) {
                pathsToExclude.add(Paths.get(opt.getValue()));
            } else if (opt.getOpt().equals("dir")) {
                try {
                    dirThreshold = Double.parseDouble(opt.getValue());
                    if (dirThreshold <= 0 || dirThreshold > 1.0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Dir Threshold value not correctly formated (Double value between 0.0 and 1.0)");
                    System.exit(1);
                }
            }
        }


        if (paths.isEmpty()) {
            System.err.println("No path specified, exiting...");
            System.exit(1);
        }

        AnalyzeResult result = JavadocAnalyzer.analyze(getJavaPaths(paths, pathsToExclude), javaLevel);

        if (anyJThreshold != 0.0 && result.anyJ() < anyJThreshold) {
            System.err.printf("AnyJ Value of %.2f does not meet required %.2f threshold.\n", result.anyJ(), anyJThreshold);
            System.exit(1);
        }
    }

    private static List<Path> getJavaPaths(List<Path> include, List<Path> exclude) {
        List<Path> result = new ArrayList<>();

        PathMatcher javaMatcher = FileSystems.getDefault().getPathMatcher("glob:**.java");
        for (Path includedPath : include) {
            try (Stream<Path> paths = Files.walk(includedPath)) {
                paths.filter(Files::isRegularFile)
                        .filter(javaMatcher::matches)
                        .filter(p -> exclude.stream().noneMatch(excludePath -> isChildOf(excludePath, p)))
                        .forEach(result::add);
            } catch (IOException e) {
                System.err.println("An error occurred while traversing directory");
                System.exit(1);
            }
        }

        return result;
    }

    private static boolean isChildOf(Path parent, Path child) {
        Path normalizedParent = parent.normalize();
        Path normalizedChild = child.normalize();

        try {
            Path relative = normalizedParent.relativize(normalizedChild);

            return !relative.toString().startsWith("..");
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
