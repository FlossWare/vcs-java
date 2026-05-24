# JVCS

Universal version control system abstraction library for Java. Provides a simple, unified API for reading files from Git repositories (local and remote).

## Features

- ✅ **Unified API** - Single interface for version control systems
- ✅ **Git Support** - Local and remote repositories
- ✅ **Builder Pattern** - Fluent, type-safe configuration
- ✅ **Branch/Tag/Commit Selection** - Read from any ref
- ✅ **Auto-Clone** - Automatic remote repository cloning
- ✅ **Thread-Safe** - Concurrent read operations supported
- ✅ **AutoCloseable** - Proper resource management
- ✅ **Minimal Dependencies** - Java 11+, JGit is optional

## Quick Start

### Maven Dependency

```xml
<dependency>
    <groupId>org.flossware</groupId>
    <artifactId>jvcs</artifactId>
    <version>1.0</version>
</dependency>

<!-- Add JGit dependency -->
<dependency>
    <groupId>org.eclipse.jgit</groupId>
    <artifactId>org.eclipse.jgit</artifactId>
    <version>7.6.0.202603022253-r</version>
</dependency>
```

### Basic Usage

```java
import org.flossware.vcs.VcsClient;
import org.flossware.vcs.GitVcsClient;

// Local repository
VcsClient git = GitVcsClient.builder()
    .repositoryPath("/path/to/repo")
    .branch("main")
    .basePath("src/main/resources")
    .build();

// Read a file
byte[] data = git.readFile("config.json");

// List files
List<String> files = git.listFiles("configs/");

// Check if file exists
if (git.exists("settings.yaml")) {
    System.out.println("File exists!");
}

// Clean up
git.close();
```

## Git Support

### Local Repository

```java
VcsClient git = GitVcsClient.builder()
    .repositoryPath("/home/user/projects/myapp")
    .branch("main")
    .basePath("config")
    .build();
```

### Remote Repository (Auto-Clone)

```java
VcsClient git = GitVcsClient.builder()
    .remoteUrl("https://github.com/user/repo.git")
    .branch("release/v1.0")
    .cloneDirectory(new File("/tmp/my-repo"))
    .basePath("src/main/resources")
    .build();
```

**Features:**
- Local and remote repositories
- Branch, tag, and commit support
- Automatic cloning for remote URLs
- Base path for file scoping
- Recursive file listing

**Dependency:**
```xml
<dependency>
    <groupId>org.eclipse.jgit</groupId>
    <artifactId>org.eclipse.jgit</artifactId>
    <version>7.6.0.202603022253-r</version>
</dependency>
```

## API Reference

```java
public interface VcsClient extends AutoCloseable {
    byte[] readFile(String path) throws IOException;
    InputStream openFile(String path) throws IOException;
    boolean exists(String path) throws IOException;
    List<String> listFiles(String prefix) throws IOException;
    String getDescription();
    void close() throws IOException;
}
```

## Common Use Cases

### Configuration Management

```java
// Read configuration from specific branch
VcsClient git = GitVcsClient.builder()
    .repositoryPath("/opt/config-repo")
    .branch("production")
    .basePath("configs")
    .build();

byte[] dbConfig = git.readFile("database.yaml");
byte[] apiConfig = git.readFile("api.json");
```

### CI/CD Integration

```java
// Clone and read files from release branch
VcsClient git = GitVcsClient.builder()
    .remoteUrl("https://github.com/company/configs.git")
    .branch("release/v2.0")
    .cloneDirectory(new File("/tmp/ci-configs"))
    .build();

List<String> configFiles = git.listFiles("deployment/");
for (String file : configFiles) {
    byte[] content = git.readFile(file);
    // Process deployment config
}
```

### Multi-Environment Setup

```java
// Development
VcsClient dev = GitVcsClient.builder()
    .repositoryPath("/opt/app-config")
    .branch("develop")
    .basePath("env/dev")
    .build();

// Production
VcsClient prod = GitVcsClient.builder()
    .repositoryPath("/opt/app-config")
    .branch("main")
    .basePath("env/prod")
    .build();

byte[] devConfig = dev.readFile("app.yaml");
byte[] prodConfig = prod.readFile("app.yaml");
```

## Best Practices

1. **Always use try-with-resources or close():**
   ```java
   try (VcsClient git = builder.build()) {
       byte[] data = git.readFile("file.txt");
   }
   ```

2. **Use basePath for scoping:**
   ```java
   // Scope to specific directory
   .basePath("src/main/resources")
   ```

3. **Specify branch explicitly:**
   ```java
   // Good - explicit branch
   .branch("release/v1.0")
   
   // Risky - depends on repo default
   // (omitting branch defaults to "main")
   ```

4. **Clean up cloned repositories:**
   ```java
   File cloneDir = new File("/tmp/my-clone");
   try (VcsClient git = builder.cloneDirectory(cloneDir).build()) {
       // Use repo
   } finally {
       // Optionally delete cloneDir
       FileUtils.deleteDirectory(cloneDir);
   }
   ```

## Versioning and Releases

This project uses **X.Y semantic versioning** (e.g., 1.0, 1.1, 2.0). Versions are automatically incremented on commits to the main branch and published to packagecloud.io.

### Maven Repository

```xml
<repositories>
    <repository>
        <id>packagecloud-flossware</id>
        <url>https://packagecloud.io/flossware/java/maven2</url>
    </repository>
</repositories>
```

## Building from Source

```bash
git clone https://github.com/FlossWare/jvcs.git
cd jvcs
mvn clean install
```

## License

Apache License 2.0

## Related Projects

- [jcloudstorage](https://github.com/FlossWare/jcloudstorage) - Cloud storage abstraction (S3, Azure, GCS, Google Drive, Dropbox, OneDrive)
- [jfiletransfer](https://github.com/FlossWare/jfiletransfer) - File transfer abstraction (SFTP, WebDAV, SMB/CIFS, FTP/FTPS)
- [jmessaging](https://github.com/FlossWare/jmessaging) - Messaging abstraction (Kafka, RabbitMQ, Redis)
- [jcontainer](https://github.com/FlossWare/jcontainer) - Container abstraction (Kubernetes, Docker, Hazelcast)
- [jclassloader](https://github.com/FlossWare/jclassloader) - Dynamic class loading from 34+ transport protocols
