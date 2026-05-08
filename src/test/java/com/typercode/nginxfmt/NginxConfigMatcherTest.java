package com.typercode.nginxfmt;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.fileTypes.FileType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NginxConfigMatcherTest {
    @Test
    void matchesExplicitNginxConfigNames() {
        assertTrue(NginxConfigMatcher.looksLikeNginxConfig(file("/tmp/nginx.conf")));
        assertTrue(NginxConfigMatcher.looksLikeNginxConfig(file("/tmp/prod.nginx.conf")));
    }

    @Test
    void matchesNginxDirectoryConfigFiles() {
        assertTrue(NginxConfigMatcher.looksLikeNginxConfig(file("/etc/nginx/conf.d/default.conf")));
        assertTrue(NginxConfigMatcher.looksLikeNginxConfig(file("/etc/nginx/sites-enabled/default")));
        assertTrue(NginxConfigMatcher.looksLikeNginxConfig(file("/usr/local/etc/nginx/sites-available/api")));
    }

    @Test
    void matchesFilesRecognizedByTheNginxConfigurationPlugin() {
        assertTrue(NginxConfigMatcher.looksLikeNginxConfig(
                file("/tmp/default.conf", "Nginx Configuration", "Nginx configuration file")
        ));
    }

    @Test
    void doesNotMatchUnrelatedConfFiles() {
        assertFalse(NginxConfigMatcher.looksLikeNginxConfig(file("/etc/apache2/sites-enabled/default.conf")));
        assertFalse(NginxConfigMatcher.looksLikeNginxConfig(file("/tmp/app.conf")));
    }

    private static VirtualFile file(String path) {
        return file(path, "Plain Text", "Plain text");
    }

    private static VirtualFile file(String path, String fileTypeName, String fileTypeDescription) {
        return new TestVirtualFile(path, new TestFileType(fileTypeName, fileTypeDescription));
    }

    private static final class TestVirtualFile extends VirtualFile {
        private final String path;
        private final FileType fileType;

        private TestVirtualFile(String path, FileType fileType) {
            this.path = path;
            this.fileType = fileType;
        }

        @Override
        public String getName() {
            int lastSlash = path.lastIndexOf('/');
            return lastSlash < 0 ? path : path.substring(lastSlash + 1);
        }

        @Override
        public VirtualFileSystem getFileSystem() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public FileType getFileType() {
            return fileType;
        }

        @Override
        public boolean isWritable() {
            return true;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public VirtualFile getParent() {
            return null;
        }

        @Override
        public VirtualFile[] getChildren() {
            return EMPTY_ARRAY;
        }

        @Override
        public OutputStream getOutputStream(Object requestor, long newModificationStamp, long newTimeStamp) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public byte[] contentsToByteArray() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getTimeStamp() {
            return 0;
        }

        @Override
        public long getLength() {
            return 0;
        }

        @Override
        public void refresh(boolean asynchronous, boolean recursive, Runnable postRunnable) {
            if (postRunnable != null) {
                postRunnable.run();
            }
        }

        @Override
        public InputStream getInputStream() throws IOException {
            throw new UnsupportedOperationException();
        }
    }

    private record TestFileType(String name, String description) implements FileType {
        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getDefaultExtension() {
            return "conf";
        }

        @Override
        public javax.swing.Icon getIcon() {
            return null;
        }

        @Override
        public boolean isBinary() {
            return false;
        }
    }
}
