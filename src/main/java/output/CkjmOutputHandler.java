package output;

import metrics.ClassMetrics;
import metrics.ProjectMetrics;

public interface CkjmOutputHandler {
    void handleProject(String paramString, ProjectMetrics paramProjectMetrics);
    void handleClass(String paramString, ClassMetrics paramClassMetrics);
}