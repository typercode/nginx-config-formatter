package com.typercode.nginxfmt;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPromoter;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.ArrayList;
import java.util.List;

public class NginxConfigFormatActionPromoter implements ActionPromoter {
    private static final String FORMAT_ACTION_ID = "NginxConfigFormatter.Format";

    @Override
    public List<AnAction> promote(List<? extends AnAction> actions, DataContext context) {
        VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(context);
        if (!NginxConfigMatcher.looksLikeNginxConfig(file)) {
            return null;
        }

        List<AnAction> promoted = new ArrayList<>();
        ActionManager actionManager = ActionManager.getInstance();
        for (AnAction action : actions) {
            if (FORMAT_ACTION_ID.equals(actionManager.getId(action))) {
                promoted.add(action);
            }
        }

        return promoted.isEmpty() ? null : promoted;
    }
}
