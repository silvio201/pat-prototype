package at.jku.pat.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import at.jku.pat.model.AnalyzeResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class JavadocAnalyzer {

    private final static boolean LOG = true;

    public static AnalyzeResult analyze (List<Path> javaPaths, int javaVersion) {
        AnalyzeResult result = new AnalyzeResult(0, 0);
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

                AnalyzeResult r = new AnalyzeResult(
                        methods.size(),
                        methods.stream().filter(NodeWithJavadoc::hasJavaDocComment).count()
                );
                log(path, r);
                result = result.combine(r);
            } catch (IOException e) {
                //TODO
                throw new RuntimeException(e);
            }

        }

        return result;
    }

    private static void log(Path p, AnalyzeResult result) {
        if (LOG) {
            System.out.printf("ANALYZED: \t%s%nRESULT: %n\tANY_J: \t%.2f%n", p.normalize().toAbsolutePath(), result.anyJ());
        }
    }
}
