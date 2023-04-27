package Utils;

import Entity.Commit;
import Entity.FileTouched;
import Entity.Release;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RetrieveGitInfoTicket {
    private static final String JIRA_REGEX = "\\b(BOOKKEEPER-[0-9]+)\\b"; // Regex per trovare le key di JIRA
    private static final Pattern JIRA_PATTERN = Pattern.compile(JIRA_REGEX);
    public void commitInfo() throws Exception {
            // Set the path to the local Git repository
            String localPath = "C:\\Users\\Roberto\\Documents\\GitHub\\bookkeeper";

            // Open the Git repository
            Repository repo = new FileRepository(localPath + "/.git");

            // Create a Git object to interact with the repository
            Git git = new Git(repo);

            // Use a RevWalk to traverse the commit graph
            try (RevWalk walk = new RevWalk(repo)) {
                // Get the HEAD commit
                RevCommit head = walk.parseCommit(repo.resolve("HEAD"));

                // Traverse the commit graph from HEAD to the first commit
                walk.markStart(head);
                for (RevCommit commit : walk) {
                    // Get the ObjectId of the commit
                    ObjectId id = commit.getId();  //cosa fa questo metodo? stampa l'id del commit
                    // Do something with each commit, such as print the commit message
                    System.out.println(commit.getFullMessage());
                    /*System.out.println(id.getName()); //stampa l'id del commit
                    System.out.println(commit.getAuthorIdent().getName()); //stampa l'autore del commit
                    System.out.println(commit.getAuthorIdent().getEmailAddress()); //stampa l'email dell'autore del commit
                    Date date = commit.getAuthorIdent().getWhen();
                    DataManipulation dataManipulation = new DataManipulation();
                    String dataString = dataManipulation.convertDateToString(date);
                    System.out.println(dataString); //stampa la data del commit*/
                    //stampa l'id del commit
                    System.out.println("\n");
                }
            }

            // Close the Git object and the repository
            git.close();
            repo.close();

            //stampa tutte le release
            ManageRelease manageRelease = new ManageRelease();
            List<Release> list = manageRelease.retrieveReleases("BOOKKEEPER");
            for (Release release : list) {
                System.out.println(release.getReleaseName());
                DataManipulation DataManipulation = new DataManipulation();
                String dataString = DataManipulation.convertDateToString(release.getDate());
                System.out.println(dataString);
            }

        }
    public void JiraKeywordsExtractor () throws IOException { //metodo per estrarre le key di JIRA dai commit message
        // Set the path to the local Git repository
        String localPath = "C:\\Users\\Roberto\\Documents\\GitHub\\bookkeeper";

        // Open the Git repository
        Repository repo = Git.open(new File(localPath + "/.git")).getRepository();

        // Create a Git object to interact with the repository
        Git git = new Git(repo);

        // Use a RevWalk to traverse the commit graph
        try (RevWalk walk = new RevWalk(repo)) {
            // Get the HEAD commit
            RevCommit head = walk.parseCommit(repo.resolve("HEAD"));

            // Traverse the commit graph from HEAD to the first commit
            walk.markStart(head);
            for (RevCommit commit : walk) {
                // Get the commit message
                String commitMessage = commit.getFullMessage();

                // Find JIRA keywords in the commit message
                Matcher matcher = JIRA_PATTERN.matcher(commitMessage);
                while (matcher.find()) {
                    String jiraKeyword = matcher.group(1);
                    System.out.println("Commit " + commit.getName() + " contains JIRA keyword: " + jiraKeyword);
                }
            }
        }

        // Close the Git object and the repository
        git.close();
        repo.close();
    }

    public List<Commit> gitCommitClasses() throws IOException, GitAPIException, ParseException {
            List<Commit> commits = new ArrayList<>();
            List<FileTouched> fileToucheds = new ArrayList<>();
            Commit commitGit = null;
            // Path to the local Git repository
            String localPath = "C:\\Users\\Roberto\\Documents\\GitHub\\bookkeeper";
            // Open the Git repository
            Repository repo = Git.open(new File(localPath + "/.git")).getRepository();
            // Get the commit ID you want to scan
            // Use a RevWalk to traverse the commit graph
        try (RevWalk walk = new RevWalk(repo)) {
            // Get the HEAD commit
            RevCommit head = walk.parseCommit(repo.resolve("HEAD"));

            // Traverse the commit graph from HEAD to the first commit
            walk.markStart(head);

            for (RevCommit commit : walk) {
                // Get the tree for the commit
                RevTree tree = commit.getTree();
                ManageRelease manageRelease = new ManageRelease();
                List<Release> releases = manageRelease.retrieveReleases("BOOKKEEPER");
                Date commitDate = commit.getAuthorIdent().getWhen();
                for (Release release : releases) {
                    if (commitDate.before(release.getDate())) { //se la data del commit è successiva alla data della release
                        commitGit = new Commit(commit.getName(), commit.getAuthorIdent().getName(), commitDate, release);
                        break;
                    }
                }
                // Create a TreeWalk to scan the tree
                try (TreeWalk treeWalk = new TreeWalk(repo)) { //cos'è il treeWalk? è un oggetto che serve per scansionare l'albero dei file del commit e trovare i file java
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(true); //cos'è? serve per scansionare l'albero in modo ricorsivo e trovare tutti i file java presenti nel commit

                    // List of classes found in the commit
                    List<String> classes = new ArrayList<>();
                    // Scan the tree and add any Java files to the list of classes
                    while (treeWalk.next()) {
                        String path = treeWalk.getPathString();
                        if (path.endsWith(".java")) {
                            String className = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.'));
                            //FileTouched fileTouched = new FileTouched(className);
                            //fileToucheds.add(fileTouched);
                            classes.add(className);

                        }
                    }




                }

                if(commitGit != null) { //se il commit è stato creato con successo (ovvero se la data del commit è successiva alla data della release) allora aggiungo i file java al commit
                    //commitGit.addAllFilesInChangedFiles(fileToucheds); //aggiungo i file java al commit
                    commits.add(commitGit); //aggiungo il commit alla lista dei commit
                }
            }
        }
                repo.close(); // Close the repository
                return commits; //ritorno la lista dei commit
        }

    public static void main(String[] args) throws IOException, GitAPIException {
        // Apriamo il repository git
        Path gitPath = Path.of("C:\\Users\\Roberto\\Documents\\GitHub\\bookkeeper");
        Git git = Git.open(gitPath.toFile()); //apre il repository git in locale
        Repository repo = git.getRepository(); //recupera il repository git in locale
        // Elenco dei commit da analizzare
        List<String> commitIds = new ArrayList<>();


        // Iteriamo attraverso i commit
        for (String commitId : commitIds) {
            ObjectId objectId = repo.resolve(commitId);
            if (objectId != null) {
                // Recuperiamo il commit
                RevWalk revWalk = new RevWalk(repo);
                RevCommit commit = revWalk.parseCommit(objectId);
                revWalk.dispose();

                // Recuperiamo il testo del commit
                String commitText = commit.getFullMessage();
                System.out.println("Commit " + commitId + ":\n" + commitText);

                // Recuperiamo le classi modificate nel commit
                RevCommit parentCommit = commit.getParent(0);
                if (parentCommit != null) {
                    CanonicalTreeParser oldTreeIter = new CanonicalTreeParser(); //crea un albero per il commit precedente
                    oldTreeIter.reset(repo.newObjectReader(), parentCommit.getTree().getId()); //resetta l'albero per il commit precedente
                    CanonicalTreeParser newTreeIter = new CanonicalTreeParser(); //crea un albero per il commit corrente
                    newTreeIter.reset(repo.newObjectReader(), commit.getTree().getId()); //resetta l'albero per il commit corrente
                    DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE); //crea un oggetto per formattare la differenza tra i due alberi
                    diffFormatter.setRepository(repo); //setta il repository
                    List<DiffEntry> diffs = diffFormatter.scan(oldTreeIter, newTreeIter); //scansiona i due alberi e crea una lista di DiffEntry
                    for (DiffEntry diff : diffs) {
                        String oldPath = diff.getOldPath();
                        String newPath = diff.getNewPath();
                        if (oldPath.endsWith(".java") && newPath.endsWith(".java")) {
                            // Recuperiamo il contenuto della classe modificata
                            String classContent = new String(Files.readAllBytes(gitPath.resolve(newPath)));
                            System.out.println("Class " + newPath + ":\n" + classContent);
                        }
                    }
                }
            }
        }
        git.close();
    }


}








