/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.palette.label;

import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.wicket.JavaForMarkupQuery;
import org.netbeans.modules.web.wicket.palette.util.AddInvocationToConstructorTask;
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
        itemid = "Label",
        //        name = "#NAME_wicket-label",
        name = "Label",
        icon16 = "org/netbeans/modules/web/wicket/palette/label/label_16.png",
        icon32 = "org/netbeans/modules/web/wicket/palette/label/label_32.png",
        //        tooltip = "#HINT_wicket-label"
        tooltip = "<html>\n"
        + "&lt;span wicket:id=\"<i>value_1</i>\"&gt;<i>value_2</i>&lt;/span&gt;\n"
        + "</html>"
)
public class Label implements ActiveEditorDrop {

    private String wicketId = "";
    private String placeholderText = "";
    private String initialValue = "";

    @Override
    public boolean handleTransfer(JTextComponent targetComponent) {
        LabelCustomizer c = new LabelCustomizer(this, targetComponent);
        if (c.showDialog()) {
            try {
                FileObject javaFo = JavaForMarkupQuery.find(PaletteSupportUtilities.getFileObject(targetComponent));
                final JavaSource source = JavaSource.forFileObject(javaFo);
                if (source != null) {
                    String htmlText = "\n<span wicket:id=\"" + wicketId + "\">" + placeholderText + "</span>\n";
                    String javaText = "new Label(\"" + wicketId + "\", \"" + initialValue + "\")";
                    Task<CompilationController> task = new AddInvocationToConstructorTask(source, javaText);
                    source.runUserActionTask(task, true);
                    PaletteSupportUtilities.insert(htmlText, targetComponent);
                    return true;
                }
            } catch (BadLocationException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }

    public String getWicketId() {
        return wicketId;
    }

    public void setWicketId(String wicketId) {
        this.wicketId = wicketId;
    }

    public String getPlaceholderText() {
        return placeholderText;
    }

    public void setPlaceholderText(String placeholderText) {
        this.placeholderText = placeholderText;
    }

    public String getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(String initialValue) {
        this.initialValue = initialValue;
    }
}
