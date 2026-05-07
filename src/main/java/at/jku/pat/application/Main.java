package at.jku.pat.application;

import at.jku.pat.analyzer.JavadocAnalyzer;
import at.jku.pat.model.AnalyzeResult;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Main {

    /**
     * Entry point for the application. Parses command-line arguments to configure
     * analysis thresholds, language levels, and file paths for Javadoc analysis.
     *
     * @param args Command line arguments for paths and thresholds.
     * @throws ParseException If command line arguments cannot be parsed.
     */
    public static void main(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("p", "path", true, "Path to directory with Java Files (Can be used multiple times)");
        options.addOption("anyj", true, "AnyJ Threshold");
        options.addOption("l", "level", true, "Java Language Level");
        options.addOption("x", "exclude", true, "Path to excluded Java Files");
        options.addOption("dir", true, "Dir Threshold");
        options.addOption("wjpd", true, "WJPD Threshold");
        options.addOption("kincaid", true, "Kincaid Threshold");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        List<Path> paths = new ArrayList<>();
        List<Path> pathsToExclude = new ArrayList<>();
        double anyJThreshold = 0;
        double dirThreshold = 0;
        double wjpdThreshold = 0;
        double kincaidThreshold = 0;
        int javaLevel = 25;

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
                case "wjpd":
                    wjpdThreshold=parseNumber(opt,0, Optional.empty());
                    break;
                case "kincaid":
                    kincaidThreshold=parseNumber(opt,0, Optional.empty());
                    break;
                case "l":
                    javaLevel = parseNumber(opt,8, Optional.of(25));
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
        System.out.println("\n");
        System.out.printf("ANALYZE Results for REPO: \n \tANY_J: \t%.2f%n\tDIR: \t%.2f%n \t WJPD: \t%.2f%n \t KINCAID: \t%.2f%n", result.anyJ(), result.dir(), result.wjpd(),result.kincaid());
        boolean error = false;
        if (anyJThreshold != 0.0 && result.anyJ() < anyJThreshold) {
            System.err.printf("AnyJ Value of %.2f does not meet required %.2f threshold.\n", result.anyJ(), anyJThreshold);
            error = true;
        }

        if (dirThreshold != 0.0 && result.dir() < dirThreshold) {
            System.err.printf("Dir Value of %.2f does not meet required %.2f threshold.\n", result.dir(), dirThreshold);
            error = true;
        }

        if (wjpdThreshold != 0.0 && result.wjpd() > wjpdThreshold) {
            System.err.printf("WJPD Value of %.2fis above the threshold of %.2f.\n", result.wjpd(), wjpdThreshold);
            error = true;
        }

        if (kincaidThreshold != 0.0 && result.kincaid() > kincaidThreshold) {
            System.err.printf("Kincaid Value of %.2f is above the threshold of %.2f.\n", result.kincaid(), kincaidThreshold);
            error=true;
        }

        if (error) {
            System.exit(1);
        }
    }

    /**
     * Recursively retrieves a list of all Java files within the specified include paths,
     * filtering out any files located within the specified exclude paths.
     *
     * @param include List of directories or files to search.
     * @param exclude List of directories or files to ignore.
     * @return A list of Paths pointing to valid .java files.
     */
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

    /**
     * Checks if a specific file path is a child of, or contained within, a parent directory.
     *
     * @param parent The potential parent directory.
     * @param child The path to check.
     * @return {@code true} if the child is within the parent; {@code false} otherwise.
     */
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

    /**
     * Parses a command-line option value as a double and validates that it falls
     * within the specified percentage range.
     *
     * @param opt The command-line option containing the value.
     * @param min The minimum inclusive value.
     * @param max The maximum inclusive value.
     * @return The parsed double value.
     */
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

    /**
     * Parses a command-line option value as an integer and validates that it falls
     * within the specified minimum and optional maximum bounds.
     *
     * @param opt The command-line option containing the value.
     * @param min The minimum inclusive value.
     * @param max An Optional containing the maximum inclusive value, if applicable.
     * @return The parsed integer value.
     */
    private static int parseNumber(Option opt, int min, Optional<Integer> max) {
        int num=0;
        try {
            num = Integer.parseInt(opt.getValue(0));
            if (num <= min || (!max.isEmpty() && num > max.get() )) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            System.err.println(opt.getOpt()+" number value not correctly formated (Integer value between "+min+" and "+max+")");
            System.exit(1);
        }
        return num;
    }
}
