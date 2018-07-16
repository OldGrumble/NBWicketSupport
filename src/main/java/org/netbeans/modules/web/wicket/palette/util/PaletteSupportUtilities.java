/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.palette.util;

import java.io.IOException;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author Tim Boudreau
 */
public class PaletteSupportUtilities {

    public static void insertHTML(JTextComponent target, String htmlText) throws BadLocationException {
        StyledDocument doc = (StyledDocument)target.getDocument();
        if (doc == null) {
            return;
        }
        InsertFormatedTextRunnable insert = new InsertFormatedTextRunnable(htmlText, target, doc);
        NbDocument.runAtomicAsUser(doc, insert);
    }

    public static FileObject getFileObject(JTextComponent target) {
        StyledDocument doc = (StyledDocument)target.getDocument();
        DataObject dobj = NbEditorUtilities.getDataObject((Document)doc);
        FileObject fobj = dobj != null ? NbEditorUtilities.getDataObject((Document)doc).getPrimaryFile() : null;
        return fobj;
    }

    public static String getGetter(String methodName) {
        String firstLetterUpped = methodName.substring(0, 1).toUpperCase();
        String restofWord = methodName.substring(1);
        return "get" + firstLetterUpped + restofWord;
    }

    public static void addMethodToClass(JavaSource source, final NewMethodProperties props) {
        Task<WorkingCopy> task = new AddMethodToClassWorkerTask(props);
        try {
            ModificationResult result = source.runModificationTask(task);
            result.commit();
        } catch (IOException ex) {
            Exceptions.printStackTrace((Throwable)ex);
        }
    }

    public static void addMethodToClass(JavaSource source, final String methodName, final String methodReturnType, final String body, final Set<String> requiredImports) {
        Task<WorkingCopy> task = new AddMethodToClassWorkerTask(methodName, methodReturnType, body);
        try {
            ModificationResult result = source.runModificationTask(task);
            result.commit();
        } catch (IOException ex) {
            Exceptions.printStackTrace((Throwable)ex);
        }
    }
}
