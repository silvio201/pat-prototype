package analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import model.AnalyzeResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class JavadocAnalyzer {

    public static AnalyzeResult analyze (List<Path> javaPaths) {
        AnalyzeResult result = new AnalyzeResult(0, 0);

        for (Path path : javaPaths) {
            try {
                CompilationUnit parsed = StaticJavaParser.parse(path);

                List<MethodDeclaration> methods = parsed.findAll(MethodDeclaration.class);
                for (MethodDeclaration method : methods) {
                    method.hasJavaDocComment();
                }

                AnalyzeResult r = new AnalyzeResult(
                        methods.stream().count(),
                        methods.stream().filter(NodeWithJavadoc::hasJavaDocComment).count()
                );
                result.combine(r);
            } catch (IOException e) {
                //TODO
                throw new RuntimeException(e);
            }

        }

        return result;
    }

    private static void log(Path p, AnalyzeResult result) {
        System.out.printf("ANALYZED: %s%nRESULT: %n\tANY_J: %.2f%n", p.normalize().toAbsolutePath(), result.anyJ());
    }
}
