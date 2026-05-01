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
            switch (opt.getOpt()) {
                case "p":
                    paths.add(Paths.get(opt.getValue()));
                    break;
                case "anyj":
                    anyJThreshold=parsePercentage(opt,0,1);
                    break;
                case "dir":
                    dirThreshold=parsePercentage(opt,0,1);
                    break;
                case "l":
                    javaLevel = parseNumber(opt,8,25);
                    break;
                case "x":
                    pathsToExclude.add(Paths.get(opt.getValue()));
                    break;
                default:
                    System.err.println("Unknown option: " + opt.getOpt());
                    System.exit(1);
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

        if (dirThreshold != 0.0 && result.dir() < dirThreshold) {
            System.err.printf("Dir Value of %.2f does not meet required %.2f threshold.\n", result.dir(), dirThreshold);
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

    private static double parsePercentage(Option opt, double min, double max) {
        double perc=0;
        try {
            perc = Double.parseDouble(opt.getValue(0));
            if (perc <= min || perc > max) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.err.println(opt.getOpt()+" Threshold value not correctly formated (Double value between "+min+" and "+max+")");
            System.exit(1);
        }
        return perc;
    }

    private static int parseNumber(Option opt, double min, double max) {
        int num=0;
        try {
            num = Integer.parseInt(opt.getValue(0));
            if (num <= min || num > max) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.err.println(opt.getOpt()+" number value not correctly formated (Integer value between "+min+" and "+max+")");
            System.exit(1);
        }
        return num;
    }
}
