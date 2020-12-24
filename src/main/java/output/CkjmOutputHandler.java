package output;

import metrics.ClassMetrics;
import metrics.ProjectMetrics;

public abstract interface CkjmOutputHandler
{
  public abstract void handleProject(String paramString, ProjectMetrics paramProjectMetrics);

  public abstract void handleClass(String paramString, ClassMetrics paramClassMetrics);
}