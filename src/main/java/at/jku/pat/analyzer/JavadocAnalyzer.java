package at.jku.pat.analyzer;

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

    public static AnalyzeResult analyze (List<Path> javaPaths) {
        AnalyzeResult result = new AnalyzeResult(0, 0);

        for (Path path : javaPaths) {
            try {
                CompilationUnit parsed = StaticJavaParser.parse(path);

                List<MethodDeclaration> methods = parsed.findAll(MethodDeclaration.class);

                AnalyzeResult r = new AnalyzeResult(
                        methods.stream().count(),
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
