package flows;

import control.Weka;
import entity.FileTouched;
import entity.Release;
import utils.CostSensitivityType;
import utils.RetrieveProject;


import java.util.List;

public class WekaFlow {
    public WekaFlow(RetrieveProject project, List<Release> halfReleases, List<FileTouched> javaClasses) throws Exception {
        Weka weka = new Weka(project, halfReleases, javaClasses, true, null, CostSensitivityType.SENSITIVITY_LEARNING);
        weka.wekaWork();
    }
}
