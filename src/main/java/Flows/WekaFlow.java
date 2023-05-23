package Flows;

import Control.Weka;
import Entity.FileTouched;
import Entity.Release;
import Utils.RetrieveProject;


import java.util.List;

public class WekaFlow {
    public WekaFlow(RetrieveProject project, List<Release> halfReleases, List<FileTouched> javaClasses) throws Exception {
        Weka weka = new Weka(project, halfReleases, javaClasses, false, null, null);
        weka.wekaWork();
    }
}
