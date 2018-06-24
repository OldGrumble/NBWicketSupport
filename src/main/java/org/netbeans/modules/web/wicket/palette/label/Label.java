/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.api.java.source.CompilationController
 *  org.netbeans.api.java.source.CompilationInfo
 *  org.netbeans.api.java.source.JavaSource
 *  org.netbeans.api.java.source.JavaSource$Phase
 *  org.netbeans.api.java.source.Task
 *  org.openide.filesystems.FileObject
 *  org.openide.text.ActiveEditorDrop
 *  org.openide.util.Exceptions
 */
package org.netbeans.modules.web.wicket.palette.label;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.wicket.JavaForMarkupQuery;
import org.netbeans.modules.web.wicket.palette.Utilities;
import org.netbeans.modules.web.wicket.palette.label.LabelCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Exceptions;

public class Label
        implements ActiveEditorDrop {

    private String wicketId = "";
    private String placeholderText = "";
    private String initialValue = "";

    public boolean handleTransfer(JTextComponent targetComponent) {
        LabelCustomizer c = new LabelCustomizer(this, targetComponent);
        boolean accept = c.showDialog();
        if (accept) {
            try {
                String body = "\n<span wicket:id=\"" + this.getWicketId() + "\">" + this.getPlaceholderText() + "</span>\n";
                FileObject javaFo = JavaForMarkupQuery.find(Utilities.getFileObject(targetComponent));
                final JavaSource source = JavaSource.forFileObject((FileObject)javaFo);
                source.runUserActionTask((Task)new Task<CompilationController>() {

                    public void run(CompilationController compilationController) throws Exception {
                        compilationController.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

                        TreePathScanner<Void, Void> scanner = new Utilities.AddInvocationToConstructor(
                                source,
                                (CompilationInfo)compilationController,
                                "new Label(\"" + Label.this.getWicketId() + "\", \"" + Label.this.getInitialValue() + "\")"
                        );
                        scanner.scan(compilationController.getCompilationUnit(), null);
                    }
                }, true);
                Utilities.insert(body, targetComponent);
            } catch (BadLocationException | IOException ex) {
                Exceptions.printStackTrace((Throwable)ex);
                accept = false;
            }
        }
        return accept;
    }

    public String getWicketId() {
        return this.wicketId;
    }

    public void setWicketId(String wicketId) {
        this.wicketId = wicketId;
    }

    public String getPlaceholderText() {
        return this.placeholderText;
    }

    public void setPlaceholderText(String placeholderText) {
        this.placeholderText = placeholderText;
    }

    public String getInitialValue() {
        return this.initialValue;
    }

    public void setInitialValue(String initialValue) {
        this.initialValue = initialValue;
    }

}
