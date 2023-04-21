package org.ideplugins.vale_cli_plugin.annotator;

import com.google.gson.JsonObject;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiFile;

import java.util.List;

class InitialAnnotatorInfo {

    final Document document;
    final PsiFile file;
    final List<JsonObject> results;


    public InitialAnnotatorInfo(Document doc, PsiFile psiFile, List<JsonObject> data) {
        document = doc;
        file = psiFile;
        results = data;
    }

}
