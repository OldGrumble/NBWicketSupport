/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.api.java.source.CompilationInfo
 *  org.netbeans.api.java.source.JavaSource
 *  org.netbeans.api.java.source.JavaSource$Phase
 *  org.netbeans.api.java.source.ModificationResult
 *  org.netbeans.api.java.source.Task
 *  org.netbeans.api.java.source.TreeMaker
 *  org.netbeans.api.java.source.WorkingCopy
 *  org.netbeans.modules.editor.NbEditorUtilities
 *  org.openide.filesystems.FileObject
 *  org.openide.loaders.DataObject
 *  org.openide.text.NbDocument
 *  org.openide.util.Exceptions
 */
package org.netbeans.modules.web.wicket.palette;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

public class Utilities {

    public static void insert(String s, JTextComponent target) throws BadLocationException {
        class InsertFormatedText implements Runnable {

            final String string;
            final JTextComponent target;
            final StyledDocument doc;

            InsertFormatedText(String string, JTextComponent target, StyledDocument doc) {
                this.string = string;
                this.target = target;
                this.doc = doc;
            }

            @Override
            public void run() {
                try {
                    Utilities.insertFormated(this.string, this.target, this.doc);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace((Throwable)ex);
                }
            }
        }
        StyledDocument doc = (StyledDocument)target.getDocument();
        if (doc == null) {
            return;
        }
        InsertFormatedText insert = new InsertFormatedText(s, target, doc);
        NbDocument.runAtomicAsUser((StyledDocument)doc, (Runnable)insert);
    }

    private static int insertFormated(String s, JTextComponent target, Document doc) throws BadLocationException {
        int start = -1;
        try {
            Caret caret = target.getCaret();
            int p0 = Math.min(caret.getDot(), caret.getMark());
            int p1 = Math.max(caret.getDot(), caret.getMark());
            doc.remove(p0, p1 - p0);
            start = caret.getDot();
            doc.insertString(start, s, null);
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace((Throwable)ble);
        }
        return start;
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

    public static void addMethodToClass(JavaSource source, final String methodName, final String methodReturnType, final String body) {
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (Tree.Kind.CLASS != typeDecl.getKind()) {
                        continue;
                    }
                    ClassTree clazz = (ClassTree)typeDecl;
                    ModifiersTree methodModifiers = make.Modifiers(Collections.singleton(Modifier.PUBLIC), Collections.emptyList());
                    MethodTree newMethod = make.Method(methodModifiers, (CharSequence)methodName, (Tree)make.Identifier((CharSequence)methodReturnType), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), "{ " + body + " }", null);
                    ClassTree modifiedClazz = make.addClassMember(clazz, (Tree)newMethod);
                    workingCopy.rewrite((Tree)clazz, (Tree)modifiedClazz);
                }
            }
        };
        try {
            ModificationResult result = source.runModificationTask((Task)task);
            result.commit();
        } catch (IOException ex) {
            Exceptions.printStackTrace((Throwable)ex);
        }
    }

    public static class AddInvocationToConstructor extends TreePathScanner<Void, Void> {

        private final CompilationInfo info;
        private final JavaSource source;
        private final String code;

        public AddInvocationToConstructor(JavaSource source, CompilationInfo info, String code) {
            this.info = info;
            this.source = source;
            this.code = code;
        }

        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = this.info.getTrees().getElement(this.getCurrentPath());
            if (el != null) {
                TypeElement te = (TypeElement)el;
                List<? extends Element> enclosedElements = te.getEnclosedElements();
                for (int i = 0; i < enclosedElements.size(); ++i) {
                    Element enclosedElement = enclosedElements.get(i);
                    if (enclosedElement.getKind() != ElementKind.CONSTRUCTOR) {
                        continue;
                    }
                    ExecutableElement exe = (ExecutableElement)enclosedElement;
                    final MethodTree constructor = this.info.getTrees().getTree(exe);
                    Task<WorkingCopy> task1 = new Task<WorkingCopy>() {

                        public void run(WorkingCopy workingCopy) throws IOException {
                            workingCopy.toPhase(JavaSource.Phase.PARSED);
                            workingCopy.rewrite((Tree)constructor.getBody(), (Tree)workingCopy.getTreeMaker().addBlockStatement(constructor.getBody(), (StatementTree)AddInvocationToConstructor.this.getExpressionStatement(workingCopy, AddInvocationToConstructor.this.code)));
                        }
                    };
                    try {
                        ModificationResult result = this.source.runModificationTask((Task)task1);
                        result.commit();
                        continue;
                    } catch (IOException ex) {
                        Exceptions.printStackTrace((Throwable)ex);
                    }
                }
            }
            return null;
        }

        private ExpressionStatementTree getExpressionStatement(WorkingCopy workingCopy, String code) {
            TreeMaker make = workingCopy.getTreeMaker();
            ExpressionStatementTree est = make.ExpressionStatement((ExpressionTree)make.MethodInvocation(Collections.emptyList(), (ExpressionTree)make.Identifier((CharSequence)"add"), Collections.singletonList(make.Identifier((CharSequence)code))));
            return est;
        }

    }

    public class AddVariableToConstructor
            extends TreePathScanner<Void, Void> {

        private CompilationInfo info;
        private final JavaSource source;

        public AddVariableToConstructor(JavaSource source, CompilationInfo info) {
            this.info = info;
            this.source = source;
        }

        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = this.info.getTrees().getElement(this.getCurrentPath());
            if (el != null) {
                TypeElement te = (TypeElement)el;
                List<? extends Element> enclosedElements = te.getEnclosedElements();
                for (int i = 0; i < enclosedElements.size(); ++i) {
                    Element enclosedElement = enclosedElements.get(i);
                    if (enclosedElement.getKind() != ElementKind.CONSTRUCTOR) {
                        continue;
                    }
                    ExecutableElement exe = (ExecutableElement)enclosedElement;
                    final MethodTree constructor = this.info.getTrees().getTree(exe);
                    Task<WorkingCopy> task1 = new Task<WorkingCopy>() {

                        public void run(WorkingCopy workingCopy) throws IOException {
                            workingCopy.toPhase(JavaSource.Phase.PARSED);
                            workingCopy.rewrite((Tree)constructor.getBody(), (Tree)workingCopy.getTreeMaker().addBlockStatement(constructor.getBody(), (StatementTree)AddVariableToConstructor.this.getVariable(workingCopy)));
                        }
                    };
                    try {
                        ModificationResult result = this.source.runModificationTask((Task)task1);
                        result.commit();
                        continue;
                    } catch (IOException ex) {
                        Exceptions.printStackTrace((Throwable)ex);
                    }
                }
            }
            return null;
        }

        private VariableTree getVariable(WorkingCopy workingCopy) {
            TreeMaker make = workingCopy.getTreeMaker();
            TypeElement listClass = workingCopy.getElements().getTypeElement("java.util.List");
            ExpressionTree importListClass = make.QualIdent((Element)listClass);
            TypeElement arrayListClass = workingCopy.getElements().getTypeElement("java.util.ArrayList");
            ExpressionTree importArrayListClass = make.QualIdent((Element)arrayListClass);
            NewClassTree mapConstructor = make.NewClass(null, Collections.emptyList(), importArrayListClass, Collections.emptyList(), null);
            VariableTree vt = make.Variable(make.Modifiers(Collections.emptySet(), Collections.emptyList()), (CharSequence)"NAMES", (Tree)importListClass, (ExpressionTree)mapConstructor);
            return vt;
        }
    }
}
