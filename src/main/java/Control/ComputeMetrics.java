package Control;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import java.util.List;
import java.util.Scanner;

import Entity.FileTouched;
import Utils.RetrieveGitInfoTicket;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;

public class ComputeMetrics {

    private List<RevCommit> commits;
    private RetrieveGitInfoTicket retGitInfo;
    private List<FileTouched> javaClassesList;
    private int complexity;
    private Git git;

    public ComputeMetrics(RetrieveGitInfoTicket retGitInfo, List<FileTouched> javaClassesList, List<RevCommit> commits, Git git) {
        this.retGitInfo = retGitInfo;
        this.javaClassesList = javaClassesList;
        this.git = git;
        this.commits = commits;


    }

    private void computeSize() {

        for (FileTouched javaClass : this.javaClassesList) {
            String[] lines = javaClass.getContent().split("\r\n|\r|\n");
            javaClass.setSize(lines.length);


        }

    }
    private void computeNR() {

        for (FileTouched javaClass : this.javaClassesList) {
            javaClass.setNRev(javaClass.getCommits().size());

        }

    }

    private void computeNAuth() {

        for (FileTouched javaClass : this.javaClassesList) {
            List<String> classAuthors = new ArrayList<>();

            for (RevCommit commit : javaClass.getCommits()) {
                if (!classAuthors.contains(commit.getAuthorIdent().getName())) {
                    classAuthors.add(commit.getAuthorIdent().getName());
                }

            }
            javaClass.setNAuth(classAuthors.size());

        }

    }

    private void computeLocAndChurnMetrics(FileTouched javaClass) {

        int sumLOC = 0;
        int maxLOC = 0;
        double avgLOC = 0;
        int churn = 0;
        int maxChurn = 0;
        double avgChurn = 0;

        for (int i = 0; i < javaClass.getAddedLinesList().size(); i++) {

            int currentLOC = javaClass.getAddedLinesList().get(i);
            int currentDiff = Math.abs(javaClass.getAddedLinesList().get(i) - javaClass.getDeletedLinesList().get(i));

            sumLOC = sumLOC + currentLOC;
            churn = churn + currentDiff;

            if (currentLOC > maxLOC) {
                maxLOC = currentLOC;
            }
            if (currentDiff > maxChurn) {
                maxChurn = currentDiff;
            }

        }
        //If a class has 0 revisions, its AvgLocAdded and AvgChurn are 0 (see initialization above).
        if (!javaClass.getAddedLinesList().isEmpty()) {
            avgLOC = 1.0 * sumLOC / javaClass.getAddedLinesList().size();
        }
        if (!javaClass.getAddedLinesList().isEmpty()) {
            avgChurn = 1.0 * churn / javaClass.getAddedLinesList().size();
        }

        javaClass.setLocAdded(sumLOC);
        javaClass.setMaxLocAdded(maxLOC);
        javaClass.setAvgLocAdded(avgLOC);
        javaClass.setChurn(churn);
        javaClass.setMaxChurn(maxChurn);
        javaClass.setAvgChurn(avgChurn);

    }

    private void computeLocAndChurn() throws IOException {

        for (FileTouched javaClass : this.javaClassesList) {

            this.retGitInfo.computeAddedAndDeletedLinesList(javaClass);
            computeLocAndChurnMetrics(javaClass);

        }

    }

    public int calculateComplexity(String path, String content) {

        String filePath = path;
        System.out.println("File: " + filePath);
        complexity = 1;
        if (filePath.endsWith(".java") || !filePath.contains("test")) {

            CompilationUnit compilationUnit = StaticJavaParser.parse(content);

            // Visita l'AST e calcola la complessità ciclomatica
            new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(IfStmt n, Void arg) {
                    super.visit(n, arg);
                    // Incrementa la complessità per ogni ramo dell'if
                    complexity += n.getElseStmt().isPresent() ? 2 : 1;
                }

                @Override
                public void visit(ForStmt n, Void arg) {
                    super.visit(n, arg);
                    // Incrementa la complessità per ogni loop
                    complexity++;
                }

                @Override
                public void visit(ForEachStmt n, Void arg) {
                    super.visit(n, arg);
                    // Incrementa la complessità per ogni loop
                    complexity++;
                }

                @Override
                public void visit(WhileStmt n, Void arg) {
                    super.visit(n, arg);
                    // Incrementa la complessità per ogni loop
                    complexity++;
                }

                @Override
                public void visit(DoStmt n, Void arg) {
                    super.visit(n, arg);
                    // Incrementa la complessità per ogni loop
                    complexity++;
                }

                @Override
                public void visit(SwitchStmt n, Void arg) {
                    super.visit(n, arg);
                    // Incrementa la complessità per ogni caso nello switch
                    complexity += n.getEntries().size();
                }

                @Override
                public void visit(TryStmt n, Void arg) {
                    super.visit(n, arg);
                    // Incrementa la complessità per ogni catch nel try
                    complexity += n.getCatchClauses().size();
                }

                @Override
                public void visit(CatchClause n, Void arg) {
                    super.visit(n, arg);
                    // Incrementa la complessità per ogni catch nel try
                    complexity++;
                }
            }.visit(compilationUnit, null);
        }
        return complexity;
    }

    public int calculateComment(String path, String content) {
        int commentLines = 0;
        if (path.endsWith(".java") && !path.contains("test")) {
            Scanner scanner;
            scanner = new Scanner(content);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("//")) {
                    commentLines++;
                }
                if (line.startsWith("/*")) {
                    commentLines++;
                    while (!line.endsWith("*/")) {
                        line = scanner.nextLine();
                        commentLines++;
                    }
                }
            }

        }
        return commentLines;
    }



    public List<FileTouched> doAllMetricsComputation() throws IOException {
        //When possible, the following metrics are applied just on one single release (i.e. the release as attribute of JavaClass element)
        computeSize();    //Size = lines of code (LOC) in the class
        computeNR();    //NR = number of commits that have modified the class
        computeNAuth();    //NAuth = number of authors of the class
        computeLocAndChurn();
        for (FileTouched javaClass : javaClassesList) {
            computeCcAndComment(javaClass);
        }
        /* LocAdded = sum of number of added LOC in all the commit of the given release
         * MaxLocAdded = max number of added LOC in all the commit of the given release
         * AvgLocAdded = average number of added LOC in all the commit of the given release
         * Churn = sum of |number of added LOC - number of deleted LOC| in all the commit of the given release
         * MaxChurn = max |number of added LOC - number of deleted LOC| in all the commit of the given release
         * Churn = average of |number of added LOC - number of deleted LOC| in all the commit of the given release
         * Cc = cyclomatic complexity of the class
         * Comment = number of comment lines in the class
         * */
        return javaClassesList;
    }

    private void computeCcAndComment(FileTouched javaClass) throws IOException {

            /*DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            diffFormatter.setRepository(git.getRepository());
            diffFormatter.setDiffComparator(RawTextComparator.DEFAULT); // imposta il comparatore di diff di default (per file di testo)
            diffFormatter.setDetectRenames(true); // imposta il rilevamento dei file rinominati a true (per ottenere le diff dei file rinominati)
            if (commit.getParentCount() == 0) { // se il commit non ha parent, continua con il prossimo commit
                continue;
            }
            RevCommit parent = commit.getParent(0); // ottieni il primo parent del commit corrente
            List<DiffEntry> diffs = diffFormatter.scan(parent, commit); // ottieni le differenze tra il commit corrente e il suo parent
            for (DiffEntry diff : diffs) { // per ogni differenza
                if (diff.getChangeType() == DiffEntry.ChangeType.DELETE) // se il file è stato cancellato, continua con il prossimo file
                    continue;
                //se il file è stato fatto il refatoring, continua con il prossimo file
                if (diff.getChangeType() == DiffEntry.ChangeType.RENAME)
                    continue;
                if (!diff.getNewPath().endsWith(".java") || diff.getNewPath().contains("test"))  //il file deve terminere con la dicitura .java
                    continue;*/
                javaClass.setCc(calculateComplexity(javaClass.getPathname(), javaClass.getContent()));
                javaClass.setLocm(calculateComment(javaClass.getPathname(), javaClass.getContent()));
            }
}