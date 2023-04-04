import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.example.utils.Bug;
import org.example.utils.InfoVersion;
import org.example.utils.RetrieveInfoJira;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final String PR_NAME ="Avro";
    private static final String LINK ="https://github.com/Zudel/avro";
    public String getIDJira(String shortestMessage){
        String s=null;
        Pattern pattern= Pattern.compile(PR_NAME.toUpperCase()+"-\\d+");
        Matcher matcher= pattern.matcher(shortestMessage);
        if(matcher.find()){
            s=matcher.group(0);
        }
        return s;
    }


}
