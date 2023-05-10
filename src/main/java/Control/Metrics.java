package Control;


import Entity.FileTouched;
import Entity.Release;
import Utils.ManageRelease;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;


public class Metrics {
    private  List<Release> halfReleases;
    private  ManageRelease manageRelease;
    private int complexity;
    private Git git;
    private int linesOfCode;
    private int sum;
    private int com;
    private String projName;
    private Release release;
    private int changes;
    private int totalRevisions;
    private List<FileTouched> fileTouchedList;


    public Metrics(Git git) throws IOException, ParseException {
        this.git = git;
        this.complexity = 1;
    }
    public Metrics(Git git, String projName) throws IOException, ParseException {
        manageRelease = new ManageRelease();
        List<Release> releases = manageRelease.retrieveReleases(projName);
        halfReleases = manageRelease.getHalfRelease(releases);
        this.git = git;
        this.complexity = 1;
        this.projName = projName;
    }

    public int countCC(RevCommit commit) throws IOException, GitAPIException {

            TreeWalk treeWalk = new TreeWalk(git.getRepository()); // crea un TreeWalk per scorrere i file della commit corrente
                treeWalk.addTree(commit.getTree());
                treeWalk.setRecursive(true);

                while (treeWalk.next()) { // per ogni file nella commit corrente
                    if (!treeWalk.isSubtree()) {
                        String path = treeWalk.getPathString(); // ottieni il path del file corrente
                        if (path.endsWith(".java") && !path.contains("test")) {
                            complexity = calculateComplexity(path);}
                    }
                }

            return complexity;
        }


    public int countLOC(String path) throws IOException, GitAPIException {
        if (path.endsWith(".java") || !path.contains("/test/")) {
                        BufferedReader reader = new BufferedReader(new FileReader(path)); // crea un BufferedReader per leggere il contenuto del file
                        linesOfCode = 0;
                        String line;
                        while ((line = reader.readLine()) != null) {
                            //se la riga non è vuota e non è un commento, incrementa il contatore
                            if (!line.isEmpty() && !line.startsWith("//"))
                                linesOfCode++;
                        }
                    }
                return linesOfCode;
    }

    public List<FileTouched> computeMetrics() throws GitAPIException, IOException, ParseException {
            Repository repo = git.getRepository();
            Iterable<RevCommit> commits = git.log().add(repo.resolve("master")).call();
            //ordino i commit dal primo a quello più recente
            List<RevCommit> commitList = new ArrayList<>();


            for (RevCommit commit : commits) {
                commitList.add(commit);
            }
            commitList.sort((o1, o2) -> {
                if (o1.getCommitTime() > o2.getCommitTime()) {
                    return 1;
                } else if (o1.getCommitTime() < o2.getCommitTime()) {
                    return -1;
                } else {
                    return 0;
                }
            });


            for (RevCommit commit : commitList) {
                totalRevisions=0;
                fileTouchedList = new ArrayList<>();
                DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
                diffFormatter.setRepository(git.getRepository());
                diffFormatter.setDiffComparator(RawTextComparator.DEFAULT); // imposta il comparatore di diff di default (per file di testo)
                diffFormatter.setDetectRenames(true); // imposta il rilevamento dei file rinominati a true (per ottenere le diff dei file rinominati)

                if (commit.getParentCount() == 0) { // se il commit non ha parent, continua con il prossimo commit
                    continue;
                }

                RevCommit parent = commit.getParent(0); // ottieni il primo parent del commit corrente
                List<DiffEntry> diffs = diffFormatter.scan(parent, commit); // ottieni le differenze tra il commit corrente e il suo parent
                List<PersonIdent> authors = new ArrayList<>();
                if(!authors.contains(commit.getAuthorIdent())){
                    authors.add(commit.getAuthorIdent());
                }
                for (DiffEntry diff : diffs) { // per ogni differenza
                    if (diff.getChangeType() == DiffEntry.ChangeType.DELETE) // se il file è stato cancellato, continua con il prossimo file
                        continue;
                    //se il file è stato fatto il refatoring, continua con il prossimo file
                    if (diff.getChangeType() == DiffEntry.ChangeType.RENAME)
                        continue;
                    if (!diff.getNewPath().endsWith(".java") || diff.getNewPath().contains("test"))  //il file deve terminere con la dicitura .java
                        continue;
                    File file = new File(diff.getNewPath());
                    if (!file.exists()) {
                        continue;
                    }
                    //togli i file che non finiscono con .java e che contengono la dicitura test
                    EditList editList = diffFormatter.toFileHeader(diff).toEditList(); // ottieni la lista delle modifiche del file corrente (diff)
                    int linesAdded = 0;
                    int linesDeleted = 0;
                    Date dateCommit = commit.getAuthorIdent().getWhen();
                    for(Release release : halfReleases){
                        if(dateCommit.getTime() <= release.getDate().getTime()){
                            this.release = release;
                            break;
                        }
                        //se il commit è dopo l'ultima release allora  ritorna
                        if(dateCommit.getTime() > halfReleases.get(halfReleases.size()-1).getDate().getTime()){
                            return fileTouchedList;
                        }
                    }

                    changes = 0;

                    for (Edit edit : editList) { // per ogni modifica
                        linesAdded += edit.getEndB() - edit.getBeginB(); // incrementa il contatore delle linee aggiunte
                        linesDeleted += edit.getEndA() - edit.getBeginA(); // incrementa il contatore delle linee cancellate
                        changes += edit.getEndB() - edit.getBeginB();
                    }
                    //somma sulle revisione del progetto le linee aggiunte e cancellate per ogni file toccato in ogni commit
                    sum = linesAdded + linesDeleted;

                    int totalChanges = changes;
                    //calcola le linne di codice
                    if(diff.getNewPath().equals("bookkeeper-server/src/main/java/org/apache/bookkeeper/client/LedgerEntry.java")) {
                        System.out.println("File: " + diff.getNewPath() + " release: " + release.getId() + " " + release.getReleaseName()); // stampa il nome del file
                        linesOfCode = countLOC(diff.getNewPath());
                        System.out.println("Lines of code: " + linesOfCode);
                        System.out.println("Lines touched: " + sum);
                        System.out.println("Change Set Size: " + totalChanges);
                        System.out.println("Complexity: " + calculateComplexity(diff.getNewPath()));
                        System.out.println("commenti: " + calculateComment(diff.getNewPath()));
                        System.out.println("numero di revisioni: " + countNRev(diff.getNewPath(), commitList, release));
                        System.out.println("numero di autori: " + countNAut(diff.getNewPath(), commitList, release));
                        System.out.println("numero di fixed bug: " + countDefectsFixed(commit));
                        System.out.println("eta': " + calculateAge(commit));
                        System.out.println("------------------------------------------------------------------");
                        FileTouched fileTouched = new FileTouched(diff.getNewPath(), release.getId());

                        fileTouched.setLoc_Touched(sum);
                        fileTouched.setCc(calculateComplexity(diff.getNewPath()));
                        fileTouched.setNAuth(countNAut(diff.getNewPath(), commit));
                        fileTouched.setLocm(calculateComment(diff.getNewPath()));
                        fileTouchedList.add(fileTouched);
                    }

                }
            }
            return fileTouchedList;

    }

    private int countNAut(String newPath, List<RevCommit> commitList, Release release) throws ParseException, IOException {
        //calcola il numero di autori che hanno modificato il file in una release specifica e ritorna il numero di autori che hanno modificato il file
        List<String> authors = new ArrayList<>();
        for (RevCommit commit : commitList) {
            if(commit.getAuthorIdent().getWhen().getTime() <= release.getDate().getTime()) {
                //controlla i file nel commit se c'è il file che stiamo cercando "newPath" con treeWalk
                Repository repository = git.getRepository();
                TreeWalk treeWalk = new TreeWalk(repository);
                treeWalk.addTree(commit.getTree());
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(newPath));
                if (treeWalk.next() && treeWalk.getPathString().equals(newPath)) {
                        if (!authors.contains(commit.getAuthorIdent().getName())) {
                            authors.add(commit.getAuthorIdent().getName());
                        }
                    }

            }
        }
        return authors.size();
    }

    private int calculateAge(RevCommit commit) throws  ParseException {
        int age = 0;
        //calcola l'eta del file in base alla data del commit e la data della release in cui è stato fatto il commit
        //ossia calcolo l'età di ogni commit rispetto alla prima data utile di rilascio della release.
        for (Release release : halfReleases) {
            if (commit.getAuthorIdent().getWhen().getTime() <= release.getDate().getTime()) {
                //calcola la sua differenza in giorni
                age = (int) ((release.getDate().getTime() - commit.getAuthorIdent().getWhen().getTime()) / (1000 * 60 * 60 * 24));
                break;
            }
        }
        return age;
    }

    private int countNAut(String newPath, RevCommit commit) {
        int autori = 0;
                return autori;
    }


    public int calculateComplexity(String path) throws IOException {

            String filePath = path;
            complexity = 1;
            if (filePath.endsWith(".java") || !filePath.contains("test")) {
                // Crea un FileInputStream per leggere il contenuto del file (path
                FileInputStream fileInputStream = new FileInputStream(path);
                CompilationUnit compilationUnit = StaticJavaParser.parse(fileInputStream);

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

       //scrivi un metodo che calcola la media del numero di commenti all'interno di un file Java
    public int calculateComment(String path)  {
        int commentLines =0;
        if (path.endsWith(".java") && !path.contains("test")) {
            File file = new File(path);
            Scanner scanner;
            try {
                scanner = new Scanner(file);
            } catch (FileNotFoundException e) {
                return -1;
            }
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

    public int countNRev(String filePath, List<RevCommit> commit, Release release) throws IOException, ParseException {
        Repository repository = git.getRepository();
        RevWalk revWalk = new RevWalk(repository);
        List<RevCommit> commitList = new ArrayList<>();
            for(RevCommit revCommit : commit){
                if (revCommit.getAuthorIdent().getWhen().getTime() > release.getDate().getTime()) {
                    break;
                }
                RevTree tree = revCommit.getTree(); // ottieni l'albero del commit corrente (commit)
                //stampa tutti i file .java che sono stati modificati in ogni commit

                try (TreeWalk treeWalk = new TreeWalk(repository)) { // crea un nuovo TreeWalk per il repository corrente
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true);
                    treeWalk.setFilter(PathFilter.create(filePath)); // filtra i commit che hanno modificato il file corrente (filePath)
                    if (treeWalk.next()) { // se il commit ha modificato il file corrente (filePath)
                        if(treeWalk.getPathString().equals(filePath))
                            commitList.add(revCommit); // aggiungi il commit alla lista dei commit


                    }
                }
            }
            revWalk.close();
            repository.close();
        return commitList.size();
    }

    public int countDefectsFixed(RevCommit commit) throws  GitAPIException {

        // Initialize a counter for the number of defects fixed
        int numDefectsFixed = 0;

        // Iterate through the commits and count the number of defects fixed
            // Check if the commit message contains keywords related to defects or bug fixes
            String commitMessage = commit.getFullMessage().toLowerCase();
            if (commitMessage.contains("fix")) {
                // Increment the counter for each defect-related commit
                numDefectsFixed++;
            }


        return numDefectsFixed;
    }


}
