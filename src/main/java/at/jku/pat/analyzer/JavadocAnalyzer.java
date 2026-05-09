package at.jku.pat.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import at.jku.pat.model.AnalyzeResult;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.JavadocBlockTag.Type;
import com.github.javaparser.javadoc.description.JavadocDescription;
import com.github.javaparser.javadoc.description.JavadocDescriptionElement;

import java.io.IOException;
import java.nio.file.Path;
import java.text.BreakIterator;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JavadocAnalyzer {

    private final static boolean LOG = true;
    /**
     * Processes a list of Java files to extract documentation metrics. It configures a
     * JavaParser based on the specified language level, parses each file, and tallies
     * method documentation coverage along with readability statistics (word, sentence,
     * and syllable counts).
     *
     * @param javaPaths List of paths to the Java source files to be analyzed.
     * @param javaVersion The integer representing the Java language level (e.g., 8, 11, 17, 21).
     * @return An {@link AnalyzeResult} containing the aggregated metrics for all parsed files.
     */
    public static AnalyzeResult analyze (List<Path> javaPaths, int javaVersion) {
        AnalyzeResult result = new AnalyzeResult(0, 0, 0, 0,0,0,0);
        ParserConfiguration cfg = new ParserConfiguration();

        ParserConfiguration.LanguageLevel level = switch (javaVersion) {
            case 8 -> ParserConfiguration.LanguageLevel.JAVA_8;
            case 11 -> ParserConfiguration.LanguageLevel.JAVA_11;
            case 16 -> ParserConfiguration.LanguageLevel.JAVA_16;
            case 17 -> ParserConfiguration.LanguageLevel.JAVA_17;
            case 21 -> ParserConfiguration.LanguageLevel.JAVA_21;
            default -> ParserConfiguration.LanguageLevel.JAVA_17; // Fallback
        };

        cfg.setLanguageLevel(level);
        JavaParser parser = new JavaParser(cfg);

        for (Path path : javaPaths) {
            try {
                CompilationUnit parsed = parser.parse(path).getResult().orElse(null);

                if (parsed == null) {
                    System.err.printf("Java File %s could not be parsed%n", path.toAbsolutePath());
                    continue;
                }

                List<MethodDeclaration> methods = parsed.findAll(MethodDeclaration.class);

                long nMethodsDocumented = 0;
                long nItems = 0;
                long nDocumentedItems = 0;
                long wordCount = 0;
                long sentenceCount = 0;
                long syllablesCount = 0;

                for (MethodDeclaration m : methods) {

                    // Get actual method items
                    Set<String> parameters = m.getParameters()
                            .stream()
                            .map(NodeWithSimpleName::getNameAsString).collect(Collectors.toSet());
                    Set<String> exceptions = m.getThrownExceptions().stream().map(com.github.javaparser.ast.type.Type::asString).collect(Collectors.toSet());

                    nItems = nItems + parameters.size() + exceptions.size();
                    if (!m.getType().asString().equals("void")) nItems++;

                    // Get documented parameters
                    Optional<Javadoc> javadoc = m.getJavadoc();
                    if (javadoc.isPresent()) {
                        nMethodsDocumented++;
                        Javadoc jd = javadoc.get();
                        for (JavadocBlockTag blockTag : jd.getBlockTags()) {
                            Type type = blockTag.getType();
                            switch (type) {
                                case PARAM: {
                                    if (parameters.removeIf(p -> p.equals(blockTag.getName().orElse(null)))){
                                        nDocumentedItems++;
                                    }
                                    break;
                                }
                                case RETURN: {
                                    nDocumentedItems++;
                                    break;
                                }
                                case EXCEPTION, THROWS: {
                                    if (exceptions.removeIf(e -> e.equals(blockTag.getName().orElse(null)))) {
                                        nDocumentedItems++;
                                    }
                                    break;
                                }
                            }
                        }

                        for(JavadocDescriptionElement desc: jd.getDescription().getElements())
                        {
                            wordCount += countWords(desc.toText());
                            sentenceCount += countSentences(desc.toText());
                            syllablesCount += countSyllablesInText(desc.toText());
                        }
                    }
                }

                AnalyzeResult r = new AnalyzeResult(
                        methods.size(),
                        nMethodsDocumented,
                        nItems,
                        nDocumentedItems,
                        wordCount,
                        sentenceCount,
                        syllablesCount
                );
                log(path, r);
                result = result.combine(r);
            } catch (IOException e) {
                System.err.printf("Java File %s could not be parsed.%n", path.toAbsolutePath());
            }

        }

        return result;
    }

    /**
     * Outputs the analysis metrics for a specific file to the console if logging is enabled.
     * Displays calculated values such as ANY_J, DIR, WJPD, and Kincaid scores.
     *
     * @param p The path of the file that was analyzed.
     * @param result The analysis results associated with the specific file.
     */
    private static void log(Path p, AnalyzeResult result) {
        if (LOG) {
            System.out.printf("ANALYZED: \t%s%nRESULT: %n\tANY_J: \t%.2f%n\tDIR: \t%.2f%n \t WJPD: \t%.2f%n \t KINCAID: \t%.2f%n", p.normalize().toAbsolutePath(), result.anyJ(), result.dir(), result.wjpd(),result.kincaid());
        }
    }

    /**
     * Calculates the number of words in a given string by splitting the text
     * based on whitespace.
     *
     * @param text The string to evaluate.
     * @return The total number of words found.
     */
    private static int countWords(String text) {
        if (text == null || text.isEmpty()) return 0;
        // Basic split by whitespace, ignoring punctuation-only "words"
        String[] words = text.trim().split("\\s+");
        return words.length;
    }

    /**
     * Counts the number of sentences in the provided text using a {@link BreakIterator}
     * configured for the US locale.
     *
     * @param text The string to evaluate.
     * @return The total number of sentences found.
     */
    private static int countSentences(String text) {
        if (text == null || text.isEmpty()) return 0;
        BreakIterator boundary = BreakIterator.getSentenceInstance(Locale.US);
        boundary.setText(text);
        int count = 0;
        while (boundary.next() != BreakIterator.DONE) {
            count++;
        }
        return count;
    }

    /**
     * Estimates the total syllable count for a block of text by summing the
     * syllables of each individual word.
     *
     * @param text The string to evaluate.
     * @return The total estimated syllable count.
     */
    private static int countSyllablesInText(String text) {
        String[] words = text.split("\\s+");
        int total = 0;
        for (String word : words) {
            total += countSyllablesInWord(word);
        }
        return total;
    }

    /**
     * Estimates the number of syllables in a single word using heuristic rules,
     * such as counting vowel groups and adjusting for silent trailing vowels.
     *
     * @param word The specific word to evaluate.
     * @return The estimated number of syllables (minimum of 1).
     */
    private static int countSyllablesInWord(String word) {
        word = word.toLowerCase().replaceAll("[^a-z]", "");
        if (word.length() <= 3) return 1; // Short words like "the", "it", "a"

        // Count vowel groups
        Pattern vowelPattern = Pattern.compile("[aeiouy]{1,2}");
        Matcher matcher = vowelPattern.matcher(word);
        int count = 0;
        while (matcher.find()) {
            count++;
        }

        // Drop silent 'e' at the end
        if (word.endsWith("e")) count--;

        return Math.max(1, count);
    }
}
