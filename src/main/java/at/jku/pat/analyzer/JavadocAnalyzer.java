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

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class JavadocAnalyzer {

    private final static boolean LOG = true;

    public static AnalyzeResult analyze (List<Path> javaPaths, int javaVersion) {
        AnalyzeResult result = new AnalyzeResult(0, 0, 0, 0);
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

                for (MethodDeclaration m : methods) {
                    nMethodsDocumented++;

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

                    }
                }


                AnalyzeResult r = new AnalyzeResult(
                        methods.size(),
                        nMethodsDocumented,
                        nItems,
                        nDocumentedItems
                );
                log(path, r);
                result = result.combine(r);
            } catch (IOException e) {
                System.err.printf("Java File %s could not be parsed.%n", path.toAbsolutePath());
            }

        }

        return result;
    }

    private static void log(Path p, AnalyzeResult result) {
        if (LOG) {
            System.out.printf("ANALYZED: \t%s%nRESULT: %n\tANY_J: \t%.2f%n\tDIR: \t%.2f%n", p.normalize().toAbsolutePath(), result.anyJ(), result.dir());
        }
    }
}
