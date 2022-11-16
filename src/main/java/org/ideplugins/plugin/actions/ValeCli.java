package org.ideplugins.plugin.actions;

import com.intellij.psi.PsiFile;

import java.util.List;

public interface ValeCli {
    String executeValeCliOnFile(PsiFile file);

    String executeValeCliOnFiles(List<String> files);

    String executeValeCliOnProject();
}
