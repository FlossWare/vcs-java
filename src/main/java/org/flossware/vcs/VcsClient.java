package org.flossware.vcs;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Universal interface for version control system operations.
 *
 * <p>Provides a unified API for reading files from Git repositories (local and remote).
 * Supports branch/tag/commit selection and file listing.</p>
 *
 * <h2>Supported Systems</h2>
 * <ul>
 *   <li>Git - Local and remote repositories</li>
 * </ul>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * // Local Git repository
 * VcsClient git = GitVcsClient.builder()
 *     .repositoryPath("/path/to/repo")
 *     .branch("main")
 *     .basePath("src/main/java")
 *     .build();
 *
 * byte[] data = git.readFile("com/example/Main.java");
 * git.close();
 * }</pre>
 *
 * @see GitVcsClient
 */
public interface VcsClient extends Closeable {

    /**
     * Reads a file from the repository.
     *
     * @param path The file path relative to basePath
     * @return The file contents as bytes
     * @throws IOException If the file cannot be read or does not exist
     */
    byte[] readFile(String path) throws IOException;

    /**
     * Opens a file as an input stream.
     *
     * @param path The file path
     * @return An input stream for reading the file
     * @throws IOException If the file cannot be opened
     */
    InputStream openFile(String path) throws IOException;

    /**
     * Checks if a file exists in the repository.
     *
     * @param path The file path
     * @return true if exists, false otherwise
     * @throws IOException If the check fails
     */
    boolean exists(String path) throws IOException;

    /**
     * Lists files matching the given path prefix.
     *
     * @param prefix The path prefix (empty for all files)
     * @return A list of file paths
     * @throws IOException If the list operation fails
     */
    List<String> listFiles(String prefix) throws IOException;

    /**
     * Gets a human-readable description of this VCS client.
     *
     * @return A description string (e.g., "Git[/path/to/repo@main]")
     */
    String getDescription();

    /**
     * Closes the VCS client and releases all resources.
     *
     * @throws IOException If an error occurs during cleanup
     */
    @Override
    void close() throws IOException;
}
