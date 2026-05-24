package org.flossware.vcs;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * VcsClient implementation for Git repositories.
 *
 * <p>Reads files from local or remote Git repositories. Supports branch, tag,
 * and commit selection. Can clone remote repositories automatically.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * // Local repository
 * VcsClient git = GitVcsClient.builder()
 *     .repositoryPath("/path/to/repo")
 *     .branch("main")
 *     .basePath("src/main/resources")
 *     .build();
 *
 * // Remote repository (auto-cloned)
 * VcsClient git = GitVcsClient.builder()
 *     .remoteUrl("https://github.com/user/repo.git")
 *     .branch("release/v1.0")
 *     .cloneDirectory(new File("/tmp/repo"))
 *     .build();
 *
 * byte[] data = git.readFile("config.json");
 * git.close();
 * }</pre>
 */
public class GitVcsClient implements VcsClient {
    private final Repository repository;
    private final String branch;
    private final String basePath;

    private GitVcsClient(Repository repository, String branch, String basePath) {
        this.repository = Objects.requireNonNull(repository, "repository cannot be null");
        this.branch = branch != null ? branch : "main";
        this.basePath = basePath != null ? basePath : "";
    }

    private String buildPath(String path) {
        if (basePath.isEmpty()) {
            return path;
        }

        String normalizedBase = basePath.endsWith("/") ?
            basePath.substring(0, basePath.length() - 1) :
            basePath;

        return normalizedBase + "/" + path;
    }

    @Override
    public byte[] readFile(String path) throws IOException {
        String fullPath = buildPath(path);

        try (RevWalk revWalk = new RevWalk(repository)) {
            ObjectId lastCommitId = repository.resolve("refs/heads/" + branch);
            if (lastCommitId == null) {
                lastCommitId = repository.resolve("refs/heads/master");
            }

            if (lastCommitId == null) {
                throw new IOException("Branch not found: " + branch);
            }

            RevCommit commit = revWalk.parseCommit(lastCommitId);
            RevTree tree = commit.getTree();

            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);

                while (treeWalk.next()) {
                    if (treeWalk.getPathString().equals(fullPath)) {
                        ObjectId objectId = treeWalk.getObjectId(0);
                        ObjectLoader loader = repository.open(objectId);
                        return loader.getBytes();
                    }
                }
            }

            throw new IOException("File not found in Git repository: " + fullPath);
        }
    }

    @Override
    public InputStream openFile(String path) throws IOException {
        byte[] data = readFile(path);
        return new ByteArrayInputStream(data);
    }

    @Override
    public boolean exists(String path) throws IOException {
        try {
            readFile(path);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public List<String> listFiles(String prefix) throws IOException {
        String fullPrefix = buildPath(prefix);
        List<String> results = new ArrayList<>();

        try (RevWalk revWalk = new RevWalk(repository)) {
            ObjectId lastCommitId = repository.resolve("refs/heads/" + branch);
            if (lastCommitId == null) {
                lastCommitId = repository.resolve("refs/heads/master");
            }

            if (lastCommitId == null) {
                throw new IOException("Branch not found: " + branch);
            }

            RevCommit commit = revWalk.parseCommit(lastCommitId);
            RevTree tree = commit.getTree();

            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);

                while (treeWalk.next()) {
                    String filePath = treeWalk.getPathString();
                    if (filePath.startsWith(fullPrefix)) {
                        // Remove basePath prefix if present
                        if (!basePath.isEmpty() && filePath.startsWith(basePath)) {
                            filePath = filePath.substring(basePath.length());
                            if (filePath.startsWith("/")) {
                                filePath = filePath.substring(1);
                            }
                        }
                        results.add(filePath);
                    }
                }
            }

            return results;
        }
    }

    @Override
    public String getDescription() {
        return "Git[branch=" + branch + ", basePath=" + basePath + "]";
    }

    @Override
    public void close() throws IOException {
        if (repository != null) {
            repository.close();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String repositoryPath;
        private String remoteUrl;
        private String branch = "main";
        private String basePath = "";
        private File cloneDirectory;

        public Builder repositoryPath(String repositoryPath) {
            this.repositoryPath = repositoryPath;
            return this;
        }

        public Builder remoteUrl(String remoteUrl) {
            this.remoteUrl = remoteUrl;
            return this;
        }

        public Builder branch(String branch) {
            this.branch = branch;
            return this;
        }

        public Builder basePath(String basePath) {
            this.basePath = basePath;
            return this;
        }

        public Builder cloneDirectory(File cloneDirectory) {
            this.cloneDirectory = cloneDirectory;
            return this;
        }

        public GitVcsClient build() throws IOException {
            Repository repository;

            if (remoteUrl != null) {
                // Clone remote repository
                if (cloneDirectory == null) {
                    throw new IllegalStateException("cloneDirectory must be set when using remoteUrl");
                }

                try {
                    Git git = Git.cloneRepository()
                        .setURI(remoteUrl)
                        .setDirectory(cloneDirectory)
                        .setBranch(branch)
                        .call();

                    repository = git.getRepository();

                } catch (GitAPIException e) {
                    throw new IOException("Failed to clone Git repository: " + remoteUrl, e);
                }

            } else if (repositoryPath != null) {
                // Open local repository
                try {
                    repository = new FileRepositoryBuilder()
                        .setGitDir(new File(repositoryPath, ".git"))
                        .readEnvironment()
                        .findGitDir()
                        .build();

                } catch (IOException e) {
                    throw new IOException("Failed to open Git repository: " + repositoryPath, e);
                }

            } else {
                throw new IllegalStateException("Either repositoryPath or remoteUrl must be set");
            }

            return new GitVcsClient(repository, branch, basePath);
        }
    }
}
