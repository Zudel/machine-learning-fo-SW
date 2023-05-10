package Utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;


import java.util.ArrayList;
import java.util.List;

public class FilePath {
        List<String> javaFiles = new ArrayList<>();
        Git git;
        public FilePath(Git git) {
            this.git = git;

        }

        public List<String> getJavaFiles() throws Exception { // ottieni la lista di file .java che sono stati modificati nel progetto (HEAD) (master branch)
            Repository repository = git.getRepository();
            RevWalk revWalk = new RevWalk(repository);
            //RevCommit headCommit = revWalk.parseCommit(repository.resolve("HEAD")); // ottieni l'ultimo commit del progetto (HEAD)
            //il revCommit del master branch
            Iterable<RevCommit> commits = git.log().add(repository.resolve("master")).call();

            // Recupera la lista di commit che hanno modificato il file

            for (RevCommit commit : commits){
                RevTree tree = commit.getTree(); // ottieni l'albero del commit corrente (commit)
                //stampa tutti i file .java che sono stati modificati in ogni commit
                    try (TreeWalk treeWalk = new TreeWalk(repository)) {
                        treeWalk.addTree(tree);
                        treeWalk.setRecursive(true);

                        while (treeWalk.next()) {

                            if (treeWalk.getPathString().endsWith(".java") && !treeWalk.getPathString().contains("test")) {
                                javaFiles.add(treeWalk.getPathString());
                            }
                        }
                    }
                }
            return javaFiles;
        }

}

