/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.palette.dropdown;

import com.sun.source.util.TreePathScanner;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.*;
import org.netbeans.api.wicket.JavaForMarkupQuery;
import org.netbeans.modules.web.wicket.palette.Utilities;
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
        itemid = "DropDown",
//        name = "#NAME_wicket-dropdown",
        name = "DropDown List",
        icon16 = "org/netbeans/modules/web/wicket/palette/dropdown/dropdown_16.png",
        icon32 = "org/netbeans/modules/web/wicket/palette/dropdown/dropdown_32.png",
//        tooltip = "#HINT_wicket-dropdown"
        tooltip = "<html>dropdown</html>"
)
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

                    @Override
                    public void run(CompilationController compilationController) throws Exception {
                        compilationController.toPhase(org.netbeans.api.java.source.JavaSource.Phase.ELEMENTS_RESOLVED);

                        TreePathScanner<Void, Void> scanner = new Utilities.AddInvocationToConstructor(
                                source,
                                compilationController,
                                JAVA
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
