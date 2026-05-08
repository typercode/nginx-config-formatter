package com.typercode.nginxfmt;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Locale;

final class NginxConfigMatcher {
    private static final String CONF_EXTENSION = "conf";

    private NginxConfigMatcher() {
    }

    static boolean looksLikeNginxConfig(VirtualFile file) {
        if (file == null || file.isDirectory()) {
            return false;
        }

        if (isNginxConfigurationFileType(file.getFileType())) {
            return true;
        }

        String name = file.getName().toLowerCase(Locale.ROOT);
        if (name.equals("nginx.conf") || name.endsWith(".nginx.conf")) {
            return true;
        }

        String path = file.getPath().replace('\\', '/').toLowerCase(Locale.ROOT);
        if (!path.contains("/nginx/")) {
            return false;
        }

        if (path.contains("/sites-available/") || path.contains("/sites-enabled/")) {
            return true;
        }

        String extension = file.getExtension();
        return CONF_EXTENSION.equalsIgnoreCase(extension);
    }

    private static boolean isNginxConfigurationFileType(FileType fileType) {
        return containsNginxConfiguration(fileType.getName())
                || containsNginxConfiguration(fileType.getDescription());
    }

    private static boolean containsNginxConfiguration(String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        return normalized.contains("nginx") && normalized.contains("configuration");
    }
}
