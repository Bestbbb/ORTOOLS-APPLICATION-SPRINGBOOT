//package demo.bootstrap;
//
//import com.sun.jna.Platform;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.nio.file.*;
//import java.nio.file.attribute.BasicFileAttributes;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Objects;
//
//public class SpringBootOrToolsNativeLoader {
//
//    private static final String RESOURCE_PATH;
//    private static boolean loaded;
//
//    private static URI getNativeResourceURI() throws IOException {
//        ClassLoader loader = com.google.ortools.Loader.class.getClassLoader();
//        URL resourceURL = loader.getResource(RESOURCE_PATH);
//        Objects.requireNonNull(resourceURL, String.format("Resource %s was not found in ClassLoader %s", RESOURCE_PATH, loader));
//
//        try {
//            URI resourceURI = resourceURL.toURI();
//            return resourceURI;
//        } catch (URISyntaxException var4) {
//            throw new IOException(var4);
//        }
//    }
//
//    private static Path unpackNativeResources(URI resourceURI) throws IOException {
//        Path tempPath = Files.createTempDirectory("ortools-java");
//        tempPath.toFile().deleteOnExit();
//        CustomOrToolsNativeLoader.PathConsumer<?> visitor = (sourcePath) -> {
//            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
//                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                    Path newPath = tempPath.resolve(sourcePath.getParent().relativize(file).toString());
//                    Files.copy(file, newPath);
//                    newPath.toFile().deleteOnExit();
//                    return FileVisitResult.CONTINUE;
//                }
//
//                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                    Path newPath = tempPath.resolve(sourcePath.getParent().relativize(dir).toString());
//                    Files.copy(dir, newPath);
//                    newPath.toFile().deleteOnExit();
//                    return FileVisitResult.CONTINUE;
//                }
//            });
//        };
//
//        List<URI> uris = prepURIs(resourceURI);
//        visitUriRecursionHelper(FileSystems.getDefault(), uris, visitor);
//        return tempPath;
//    }
//
//    private static <E extends IOException> void visitUriRecursionHelper(FileSystem fs, List<URI> uris, PathConsumer<E> visitor) throws IOException {
//        URI uri = uris.get(0);
//
//        Path path;
//        if (fs.provider().getScheme().equals("jar")) {
//            // Custom URI parsing here because of a JDK bug
//            String spec = uri.getSchemeSpecificPart();
//            int sep = spec.lastIndexOf("!/");
//            if (sep == -1)
//                throw new IllegalArgumentException("URI: "
//                        + uri
//                        + " does not contain path info ex. jar:file:/c:/foo.zip!/BAR");
//            path = fs.getPath(spec.substring(sep + 1));
//        } else {
//            path = fs.provider().getPath(uri);
//        }
//
//        if (uris.size() <= 1) {
//            visitor.accept(path);
//        } else {
//            try (FileSystem newFs = FileSystems.newFileSystem(path, (ClassLoader) null)) {
//                visitUriRecursionHelper(newFs, uris.subList(1, uris.size()), visitor);
//            }
//        }
//    }
//
//    private static ArrayList<URI> prepURIs(URI uri) {
//        uri = fixSpringUri(uri);
//        ArrayList<URI> uris = new ArrayList<>();
//        uris.add(uri);
//        while (!uri.getScheme().equals("file")) {
//            String end = uri.getRawSchemeSpecificPart();
//            String newUri = end.substring(0, end.lastIndexOf("!/"));
//            try {
//                uri = new URI(newUri);
//            } catch (URISyntaxException e) {
//                throw new IllegalStateException(String.format("Unexpectedly formed an invalid URI: \"%s\"", newUri), e);
//            }
//            uris.add(uri);
//        }
//        Collections.reverse(uris);
//        return uris;
//    }
//
//    private static URI fixSpringUri(URI uri) {
//        int jars = numMatches(uri.toString(), "jar:");
//        int jarEnds = numMatches(uri.toString(), "!/");
//
//        while (jars < jarEnds) {
//            try {
//                uri = new URI("jar:" + uri);
//            } catch (URISyntaxException e) {
//                throw new IllegalStateException(String.format("Unexpectedly formed an invalid URI: \"jar:%s\"", uri), e);
//            }
//            jars++;
//        }
//
//        return uri;
//    }
//
//    private static int numMatches(final String s, final String toFind) {
//        int num = 0;
//        int index = 0;
//        while ((index = s.indexOf(toFind, index)) != -1) {
//            index += toFind.length();
//            num++;
//        }
//        return num;
//    }
//
//    public static synchronized void loadNativeLibraries() {
//        if (!loaded) {
//            try {
//                URI resourceURI = getNativeResourceURI();
//                Path tempPath = unpackNativeResources(resourceURI);
//                System.load(tempPath.resolve(RESOURCE_PATH).resolve(System.mapLibraryName("jniortools")).toString());
//                loaded = true;
//            } catch (IOException var2) {
//                throw new RuntimeException(var2);
//            }
//        }
//
//    }
//
//    static {
//        RESOURCE_PATH = "ortools-" + Platform.RESOURCE_PREFIX + "/";
//        loaded = false;
//    }
//
//    @FunctionalInterface
//    private interface PathConsumer<T extends IOException> {
//        void accept(Path var1) throws T;
//    }
//}
