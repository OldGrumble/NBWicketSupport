/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.palette.ajaxfield;

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

/**
 *
 * @author Tim Boudreau
 */
public class AjaxField implements ActiveEditorDrop {

    private final String JAVA;

    public AjaxField() {
        JAVA = "\nfinal AutoCompleteTextField field = new AutoCompleteTextField(\"countries\", new Model(\"\")) {   @Override\n   protected Iterator getChoices(String input) {\n      if (Strings.isEmpty(input)) {\n         return Collections.EMPTY_LIST.iterator();\n      }\n      List choices = new ArrayList(10);\n      Locale[] locales = Locale.getAvailableLocales();\n      for (int i = 0; i < locales.length; i++) {\n           final Locale locale = locales[i];\n           final String country = locale.getDisplayCountry();\n           if (country.toUpperCase().startsWith(input.toUpperCase())) {\n               choices.add(country);\n               if (choices.size() == 10) {\n                   break;\n               }\n           }\n      }\n      return choices.iterator();\n    }\n};\nreturn field";
    }

    @Override
    public boolean handleTransfer(JTextComponent targetComponent) {
        String body = "\n<input type=\"text\" wicket:id=\"countries\" size=\"50\"/>\n";
        FileObject javaFo = JavaForMarkupQuery.find(Utilities.getFileObject(targetComponent));
        final JavaSource source = JavaSource.forFileObject(javaFo);
        Utilities.addMethodToClass(source, "getAutoCompleteTextField", "AutoCompleteTextField", JAVA);
        try {
            source.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController compilationController)
                        throws Exception {
                    compilationController.toPhase(org.netbeans.api.java.source.JavaSource.Phase.ELEMENTS_RESOLVED);

                    TreePathScanner<Void, Void> scanner = new Utilities.AddInvocationToConstructor(
                            source,
                            compilationController,
                            "getAutoCompleteTextField()"
                    );
                    scanner.scan(compilationController.getCompilationUnit(), null);

                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        try {
            Utilities.insert(body, targetComponent);
        } catch (BadLocationException ble) {
            return false;
        }
        return true;
    }
}
