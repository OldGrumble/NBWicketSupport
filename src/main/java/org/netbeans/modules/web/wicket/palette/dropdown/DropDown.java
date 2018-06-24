// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) lnc 
// Source File Name:   DropDown.java
package org.netbeans.modules.web.wicket.palette.dropdown;

import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.*;
import org.netbeans.api.wicket.JavaForMarkupQuery;
import org.netbeans.modules.web.wicket.palette.Utilities;
import org.openide.filesystems.FileObject;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Exceptions;

public class DropDown implements ActiveEditorDrop {

    private static final String HTML = "\n<select wicket:id=\"names\">\n<option>option 1</option>\n<option>option 2</option>\n<option>option 3</option>\n</select>\n";
    private static final String JAVA = "new DropDownChoice(\"names\", new ArrayList())";

    public DropDown() {
    }

    @Override
    public boolean handleTransfer(JTextComponent targetComponent) {
        String body = HTML;
        try {
            FileObject javaFo = JavaForMarkupQuery.find(Utilities.getFileObject(targetComponent));
            final JavaSource source = JavaSource.forFileObject(javaFo);
            try {
                source.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController compilationController) throws Exception {
                        compilationController.toPhase(org.netbeans.api.java.source.JavaSource.Phase.ELEMENTS_RESOLVED);

                        TreePathScanner<Void, Void> scanner = new Utilities.AddInvocationToConstructor(
                                source,
                                (CompilationInfo)compilationController,
                                "new DropDownChoice(\"names\", new ArrayList())"
                        );
                        scanner.scan(compilationController.getCompilationUnit(), null);
                    }
                }, true);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            Utilities.insert(body, targetComponent);
        } catch (BadLocationException ble) {
            return false;
        }
        return true;
    }
}
