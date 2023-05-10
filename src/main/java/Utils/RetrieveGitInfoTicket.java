package Utils;

import Control.Metrics;
import Entity.Commit;
import Entity.FileTouched;
import Entity.IssueTicket;
import Entity.Release;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RetrieveGitInfoTicket {
    private static final String JIRA_REGEX = "\\b(BOOKKEEPER-[0-9]+)\\b"; // Regex per trovare le key di JIRA
    private static final Pattern JIRA_PATTERN = Pattern.compile(JIRA_REGEX);
    private String localPath = "C:\\Users\\Roberto\\Documents\\GitHub\\bookkeeper";
    private Repository repo = Git.open(new File(localPath + "/.git")).getRepository();

    private Git git = new Git(repo);

    public RetrieveGitInfoTicket() throws IOException {
    }

    public void JiraKeywordsExtractor () throws IOException { //metodo per estrarre le key di JIRA dai commit message
        // Set the path to the local Git repository


        // Open the Git repository


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



    public List<Release> retrieveTouchedFiles(Iterable<RevCommit> commits) throws IOException, ParseException, GitAPIException {
        Repository repo = Git.open(new File(localPath + "/.git")).getRepository();
        Git git = new Git(repo);
        ManageRelease manageRelease = new ManageRelease();
        List<Release> releases = manageRelease.retrieveReleases("BOOKKEEPER");
        List<Release> halfReleases = manageRelease.getHalfRelease(releases);
        List<Release> releaseTouchedFiles = new ArrayList<>();

        for(RevCommit commit : commits) {
            for(Release release : halfReleases) {
                if(commit.getAuthorIdent().getWhen().before(release.getDate())) {
                    Release releaseTouchedFile = new Release(release.getId(), release.getReleaseName(), release.getDate());
                    RevTree tree = commit.getTree();
                    try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
                        treeWalk.addTree(tree);
                        treeWalk.setRecursive(true);
                        while (treeWalk.next()) {
                            String path = treeWalk.getPathString();

                            if (path.endsWith(".java") && !path.contains("test")) {
                                FileTouched fileTouched = new FileTouched(path);
                                fileTouched.setReleaseIndex(release.getId());

                                //computo tutte le metriche per il file java
                                Metrics metrics = new Metrics(git);
                                int cc = metrics.countCC(commit);
                                System.out.println("cc: " + cc);
                                //int locm = metrics.CalculateComment(path);


                                if(!releaseTouchedFile.getFiles().contains(fileTouched)) { //se il file non è già presente nella release allora lo aggiungo
                                   // fileTouched.setLoc(loc);
                                    System.out.println("1");
                                    //fileTouched.setLocm(locm);
                                    fileTouched.setCc(cc);
                                    System.out.println("2");
                                    //fileTouched.setLoc_Touched(loc_Touched);
                                    System.out.println("3");
                                    releaseTouchedFile.addFile(fileTouched);
                                    System.out.println("4");
                                }
                                else { //se il file è già presente nella release allora aggiorno le metriche
                                    for(FileTouched file : releaseTouchedFile.getFiles()) {
                                        if(file.equals(fileTouched)) {
                                            //file.setLoc(file.getLoc() + loc);
                                            //file.setLocm(file.getLocm() + locm);
                                            file.setCc(file.getCc() + cc);
                                            //file.setLoc_Touched(file.getLoc_Touched() + loc_Touched);
                                        }
                                    }
                                }
                            }
                        } //fine while
                        //inserisco la release con i file java nella lista delle release
                        releaseTouchedFiles.add(releaseTouchedFile);
                    }
                }
            }
        }
        return releaseTouchedFiles;
    }
    private int getAddedLines(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {

        int addedLines = 0;
        for(Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {
            addedLines += edit.getEndA() - edit.getBeginA();

        }
        return addedLines;

    }

    private int getDeletedLines(DiffFormatter diffFormatter, DiffEntry entry) throws IOException {

        int deletedLines = 0;
        for(Edit edit : diffFormatter.toFileHeader(entry).toEditList()) {
            deletedLines += edit.getEndB() - edit.getBeginB();

        }
        return deletedLines;

    }

    /*This method initializes two lists:
     * - List of numbers of added lines by each commit; every entry is associated to one specific commit
     * - List of numbers of deleted lines by each commit; every entry is associated to one specific commit
     * These lists will be used to calculate sum, max & avg*/
    public void computeAddedAndDeletedLinesList(FileTouched javaClass) throws IOException {

        for(RevCommit comm : javaClass.getCommits()) {
            try(DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {

                RevCommit parentComm = comm.getParent(0);

                diffFormatter.setRepository(this.repo);
                diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);

                List<DiffEntry> diffs = diffFormatter.scan(parentComm.getTree(), comm.getTree());
                for(DiffEntry entry : diffs) {
                    if(entry.getNewPath().equals(javaClass.getPathname())) {
                        javaClass.getAddedLinesList().add(getAddedLines(diffFormatter, entry));
                        javaClass.getDeletedLinesList().add(getDeletedLines(diffFormatter, entry));

                    }

                }

            } catch(ArrayIndexOutOfBoundsException e) {
                //commit has no parents: skip this commit, return an empty list and go on

            }

        }


    }


    public List<RevCommit> retrieveAllCommits(Git git) throws IOException, GitAPIException {
        List<RevCommit> allCommitsList = new ArrayList<>();
        List<Ref> branchesList = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();

        //Branches loop
        for(Ref branch : branchesList) {
            Iterable<RevCommit> commitsList = git.log().add(this.repo.resolve(branch.getName())).call();

            for(RevCommit commit : commitsList) {
                if(!allCommitsList.contains(commit)) {
                    allCommitsList.add(commit);
                }

            }

        }
        return allCommitsList;
    }

    public List<ReleaseCommits> getRelCommAssociations(List<RevCommit> allCommitsList, List<Release> releases) throws ParseException {
        List<ReleaseCommits> relCommAssociations = new ArrayList<>();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date firstDate = formatter.parse("1900-00-01");	//firstDate is the date of the previous release; for the first release we take 01/01/1900 as lower bound

        for(Release rel : releases) {
            relCommAssociations.add(ReleaseCommitsUtil.getCommitsOfRelease(allCommitsList, rel, firstDate));
            firstDate = rel.getDate();

        }
        return relCommAssociations;
    }

    /*This method, for each ReleaseCommits instance (i.e. for each release), retrieves all the classes that were present in project repository
     * on release date, and then sets these classes as attribute of the instance*/
    public void getRelClassesAssociations(List<ReleaseCommits> relCommAssociations) throws IOException {

        for(ReleaseCommits relComm : relCommAssociations) {
            Map<String, String> javaClasses = getClasses(relComm.getLastCommit());
            relComm.setJavaClasses(javaClasses);

        }

    }
    private Map<String, String> getClasses(RevCommit commit) throws IOException {

        Map<String, String> javaClasses = new HashMap<>();

        RevTree tree = commit.getTree();	//We get the tree of the files and the directories that were belonging to the repository when commit was pushed
        TreeWalk treeWalk = new TreeWalk(this.repo);	//We use a TreeWalk to iterate over all files in the Tree recursively
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);

        while(treeWalk.next()) {
            //We are keeping only Java classes that are not involved in tests
            if(treeWalk.getPathString().contains(".java") && !treeWalk.getPathString().contains("/test/")) {
                //We are retrieving (name class, content class) couples
                javaClasses.put(treeWalk.getPathString(), new String(this.repo.open(treeWalk.getObjectId(0)).getBytes(), StandardCharsets.UTF_8));
            }
        }
        treeWalk.close();

        return javaClasses;

    }
    /*This function:
     * - Retrieves the commits associated with the specified ticket through the getTicketCommits function (remember that we are looping on the tickets)
     * - For each commit, retrieves the associated release and the modified classes through the getReleaseOfCommit and getModifiedClasses functions
     * - For each class modified by a commit, labels it as buggy in all the releases between the IV of the ticket and the release related to that commit
     * 	 through the updateJavaClassBuggyness function*/
    private void doLabeling(List<FileTouched> javaClasses, IssueTicket ticket, List<ReleaseCommits> relCommAssociations) throws GitAPIException, IOException {

        List<RevCommit> commitsAssociatedWIssue = getTicketCommits(ticket);

        for(RevCommit commit : commitsAssociatedWIssue) {
            Release associatedRelease = ReleaseCommitsUtil.getReleaseOfCommit(commit, relCommAssociations);
            //associatedRelease can be null if commit date is after last release date; in that case we ignore the commit
            //(it is trying to fix a issue that hypothetically should be already closed)
            if(associatedRelease != null) {
                List<String> modifiedClasses = getModifiedClasses(commit);

                for(String modifClass : modifiedClasses) {
                    JavaClassUtil.updateJavaClassBuggyness(javaClasses, modifClass, ticket.getInjectedVersion(), associatedRelease);

                }

            }

        }

    }

    /*The purpose of this method is to return a list of JavaClass instances with:
     * - Class name
     * - Class content
     * - Release
     * - Binary value "isBuggy"
     * The buildAllJavaClasses function instantiates the JavaClass instances and determines class names, class contents and releases.
     * On the other hand, the doLabeling function determines if the value of "isBuggy" is true or false*/
    public List<FileTouched> labelClasses(List<ReleaseCommits> relCommAssociations, List<IssueTicket> ticketsWithAV) throws GitAPIException, IOException {

        List<FileTouched> javaClasses = JavaClassUtil.buildAllJavaClasses(relCommAssociations);

        for(IssueTicket ticket : ticketsWithAV) {
            doLabeling(javaClasses, ticket, relCommAssociations);

        }
        return javaClasses;

    }

    /*This method, for each JavaClass instance, retrieves a list of ALL the commits (not only the ones associated with some ticket) that have modified
     * the specified class for the specified release (class and release are JavaClass attributes)*/
    public void assignCommitsToClasses(List<FileTouched> javaClasses, List<RevCommit> commits, List<ReleaseCommits> relCommAssociations) throws IOException {

        for(RevCommit commit : commits) {
            Release associatedRelease = ReleaseCommitsUtil.getReleaseOfCommit(commit, relCommAssociations);
            if(associatedRelease != null) {		//There are also commits with no associatedRelease because their date is latter than last release date
                System.out.println("Commit: " + commit.getName() + " Release: " + associatedRelease.getId());
                List<String> modifiedClasses = getModifiedClasses(commit);
                for(String modifClass : modifiedClasses) {
                    JavaClassUtil.updateJavaClassCommits(javaClasses, modifClass, associatedRelease, commit);

                }

            }

        }

    }
    private List<String> getModifiedClasses(RevCommit commit) throws IOException {

        List<String> modifiedClasses = new ArrayList<>();	//Here there will be the names of the classes that have been modified by the commit

        try(DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            ObjectReader reader = this.repo.newObjectReader()) {

            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            ObjectId newTree = commit.getTree();
            newTreeIter.reset(reader, newTree);
            RevCommit commitParent;
            commitParent = commit.getParent(0);   //It's the previous commit of the commit we are considering
            //if commit not has parent, it is the first commit of the project, so we can't compare it with the previous commit

                CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                ObjectId oldTree = commitParent.getTree();
                oldTreeIter.reset(reader, oldTree);

                diffFormatter.setRepository(this.repo);
                List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);

                //Every entry contains info for each file involved in the commit (old path name, new path name, change type (that could be MODIFY, ADD, RENAME, etc.))
                for (DiffEntry entry : entries) {
                    //We are keeping only Java classes that are not involved in tests
                    if (entry.getChangeType().equals(DiffEntry.ChangeType.MODIFY) && entry.getNewPath().contains(".java") && !entry.getNewPath().contains("/test/")) {
                        modifiedClasses.add(entry.getNewPath());
                    }

                }

        }catch (ArrayIndexOutOfBoundsException e){
            //return empty list
            return modifiedClasses;
        }

        return modifiedClasses;

    }
    private List<RevCommit> getTicketCommits(IssueTicket ticket) throws GitAPIException, IOException {

        //Here there will be the commits involving the affected versions of ticket
        //Commits have a ticket ID included in their comment (full message)
        List<RevCommit> associatedCommits = new ArrayList<>();
        List<Ref> branchesList = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();

        //Branches loop
        for(Ref branch : branchesList) {
            Iterable<RevCommit> commitsList = git.log().add(repo.resolve(branch.getName())).call();

            //Commits loop within a specific branch
            for(RevCommit commit : commitsList) {
                String comment = commit.getFullMessage();

                //We are keeping only commits related to Jira tickets previously found
                if((comment.contains(ticket.getKey() + ":") || comment.contains(ticket.getKey() + "]") || comment.contains(ticket.getKey() + " ")) && !associatedCommits.contains(commit)) {
                    associatedCommits.add(commit);
                }

            }

        }
        return associatedCommits;

    }
}








