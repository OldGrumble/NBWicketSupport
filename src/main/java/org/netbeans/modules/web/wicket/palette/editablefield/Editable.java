/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.palette.editablefield;

import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.wicket.JavaForMarkupQuery;
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
        itemid = "Editable",
//        name = "#NAME_wicket-editable",
        name = "AJAX Editable Label",
        icon16 = "org/netbeans/modules/web/wicket/palette/editablefield/editable_16.png",
        icon32 = "org/netbeans/modules/web/wicket/palette/editablefield/editable_32.png",
//        tooltip = "#HINT_wicket-editable"
        tooltip = "<html>editable</html>"
)
public class Editable implements ActiveEditorDrop {

    private String wicketId = "";
    private String placeholderText = "";
    private String initialValue = "";

    private String createBody() {
        return "\n<span wicket:id=\"" + this.getWicketId() + "\" class=\"inline-edit\">" + this.getPlaceholderText() + "</span>\n";
    }

    @Override
    public boolean handleTransfer(JTextComponent targetComponent) {
        EditableCustomizer c = new EditableCustomizer(this, targetComponent);
        boolean accept = c.showDialog();
        if (accept) {
            try {
                String body = this.createBody();
                FileObject javaFo = JavaForMarkupQuery.find(PaletteSupportUtilities.getFileObject(targetComponent));
                final JavaSource source = JavaSource.forFileObject((FileObject)javaFo);
                source.runUserActionTask(new Task<CompilationController>() {

                    @Override
                    public void run(CompilationController compilationController) throws Exception {
                        compilationController.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        new AddModel(source, (CompilationInfo)compilationController, Editable.this.getWicketId(), Editable.this.getInitialValue()).scan(compilationController.getCompilationUnit(), null);
                    }
                }, true);
                source.runUserActionTask((Task)new Task<CompilationController>() {

                    @Override
                    public void run(CompilationController compilationController) throws Exception {
                        compilationController.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        new AddEditable(source, (CompilationInfo)compilationController, Editable.this.getWicketId(), Editable.this.getInitialValue()).scan(compilationController.getCompilationUnit(), null);
                    }
                }, true);
                source.runUserActionTask(new Task<CompilationController>() {

                    @Override
                    public void run(CompilationController compilationController) throws Exception {
                        compilationController.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        PaletteSupportUtilities.addMethodToClass(source, PaletteSupportUtilities.getGetter(Editable.this.getWicketId()), "String", "return " + Editable.this.getWicketId());
                    }
                }, true);
                PaletteSupportUtilities.insert(body, targetComponent);
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
