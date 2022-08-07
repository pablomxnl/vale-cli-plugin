package org.ideplugins.plugin.actions;

import com.intellij.psi.PsiFile;

import java.util.List;

public interface ValeCli {
    public String executeValeCliOnFile(PsiFile file);

    public String executeValeCliOnFiles(List<String> files);

    public String executeValeCliOnProject();
}
