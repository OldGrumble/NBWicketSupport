/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.palette.ajaxfield;

import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.*;
import org.netbeans.api.wicket.JavaForMarkupQuery;
import org.netbeans.modules.web.wicket.palette.util.AddInvocationToConstructor;
import org.netbeans.modules.web.wicket.palette.util.NewMethodProperties;
import org.netbeans.modules.web.wicket.palette.util.PaletteSupportUtilities;
import org.netbeans.spi.palette.PaletteItemRegistration;
import org.openide.filesystems.FileObject;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Exceptions;

/**
 *
 * @author Tim Boudreau
 */
@PaletteItemRegistration(
        paletteid = "HTMLPalette",
        category = "Wicket",
        itemid = "AjaxField",
        //        name = "#NAME_wicket-ajaxfield",
        name = "AJAX Text Input",
        icon16 = "org/netbeans/modules/web/wicket/palette/ajaxfield/ajaxfield_16.png",
        icon32 = "org/netbeans/modules/web/wicket/palette/ajaxfield/ajaxfield_32.png",
        //        tooltip = "#HINT_wicket-ajaxfield"
        tooltip = "<html>\n"
        + "&lt;input type=\"text\" wicket:id=\"countries\" size=\"50\"/&gt;\n"
        + "</html>"
)
public class AjaxField implements ActiveEditorDrop {

    private static final String JAVA = "\n"
            + "final AutoCompleteTextField field = new AutoCompleteTextField(\"countries\", new Model(\"\")) {\n"
            + "   @Override\n"
            + "   protected Iterator getChoices(String input) {\n"
            + "      if (Strings.isEmpty(input)) {\n"
            + "         return Collections.EMPTY_LIST.iterator();\n"
            + "      }\n"
            + "      List choices = new ArrayList(10);\n"
            + "      Locale[] locales = Locale.getAvailableLocales();\n"
            + "      for (int i = 0; i < locales.length; i++) {\n"
            + "           final Locale locale = locales[i];\n"
            + "           final String country = locale.getDisplayCountry();\n"
            + "           if (country.toUpperCase().startsWith(input.toUpperCase())) {\n"
            + "               choices.add(country);\n"
            + "               if (choices.size() == 10) {\n"
            + "                   break;\n"
            + "               }\n"
            + "           }\n"
            + "      }\n"
            + "      return choices.iterator();\n"
            + "    }\n"
            + "};\n"
            + "return field;";

    private static class TaskImpl implements Task<CompilationController> {

        private final JavaSource source;

        public TaskImpl(JavaSource source) {
            this.source = source;
        }

        @Override
        public void run(CompilationController compilationController)
                throws Exception {
            compilationController.toPhase(org.netbeans.api.java.source.JavaSource.Phase.ELEMENTS_RESOLVED);

            TreePathScanner<Void, Void> scanner = new AddInvocationToConstructor(
                    source,
                    compilationController,
                    "getAutoCompleteTextField()"
            );
            scanner.scan(compilationController.getCompilationUnit(), null);

        }
    }

    public AjaxField() {
    }

    @Override
    public boolean handleTransfer(JTextComponent targetComponent) {
        String body = "\n<input type=\"text\" wicket:id=\"countries\" size=\"50\"/>\n";
        FileObject javaFo = JavaForMarkupQuery.find(PaletteSupportUtilities.getFileObject(targetComponent));
        final JavaSource source = JavaSource.forFileObject(javaFo);
        final Set<String> imports = new TreeSet<>();
        imports.add("AutoCompleteTextField");
        NewMethodProperties props = new NewMethodProperties("getAutoCompleteTextField", "org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField");
        props.setBodyContent(JAVA);
        props.setRequiredImports(imports);
        if (source != null) {
            PaletteSupportUtilities.addMethodToClass(source, props);
            try {
                source.runUserActionTask(new TaskImpl(source), true);
                PaletteSupportUtilities.insertHTML(targetComponent, body);
                return true;
            } catch (IOException | BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }
}
