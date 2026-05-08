package com.typercode.nginxfmt;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class FormatNginxConfigAction extends AnAction {
    private final NginxFormatter formatter = new NginxFormatter();

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null) {
            return;
        }

        Document document = editor.getDocument();
        String formatted = formatter.format(document.getText());
        if (formatted.equals(document.getText())) {
            return;
        }

        WriteCommandAction.runWriteCommandAction(
                project,
                "Format Nginx Config",
                null,
                () -> document.setText(formatted)
        );
    }

    @Override
    public void update(AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        event.getPresentation().setEnabledAndVisible(
                project != null && editor != null && NginxConfigMatcher.looksLikeNginxConfig(file)
        );
    }
}
