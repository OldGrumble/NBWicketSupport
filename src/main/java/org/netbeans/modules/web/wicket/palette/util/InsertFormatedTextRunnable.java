/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.palette.util;

import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.openide.util.Exceptions;

/**
 *
 * @author Tim Boudreau
 */
class InsertFormatedTextRunnable implements Runnable {

    final String string;
    final JTextComponent target;
    final StyledDocument doc;

    InsertFormatedTextRunnable(String string, JTextComponent target, StyledDocument doc) {
        this.string = string;
        this.target = target;
        this.doc = doc;
    }

    @Override
    public void run() {
        try {
            insertFormated(this.string, this.target, this.doc);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace((Throwable)ex);
        }
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

}
