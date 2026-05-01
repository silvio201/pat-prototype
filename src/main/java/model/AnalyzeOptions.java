package model;

import java.nio.file.Path;
import java.util.List;

public record AnalyzeOptions(List<Path> javaPaths, double anyJThreshold) {
}
