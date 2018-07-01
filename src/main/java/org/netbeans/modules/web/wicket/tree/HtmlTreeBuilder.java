/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;

/**
 *
 * @author Tim Boudreau
 */
public class HtmlTreeBuilder {

    private final Document doc;
    private MarkupContainerTree<String> tree;
    private Map<String, Set<TagVisitor>> visitors;

    public HtmlTreeBuilder(Document doc) {
        this.doc = doc;
    }

    public void addTagVistor(String tagType, TagVisitor visitor) {
        Set<TagVisitor> s;
        if (this.tree != null) {
            throw new IllegalStateException("Adding a TagVisitor after the parse has already run");
        }
        if (this.visitors == null) {
            this.visitors = new HashMap<String, Set<TagVisitor>>();
        }
        if ((s = this.visitors.get(tagType)) == null) {
            s = new HashSet<TagVisitor>();
            this.visitors.put(tagType, s);
        }
        s.add(visitor);
    }

    public MarkupContainerTree<String> getTree() {
        if (this.tree == null) {
            this.tree = this.analyze();
        }
        return this.tree;
    }

    private MarkupContainerTree<String> analyze() {
        MarkupContainerTree<String> result = new MarkupContainerTree<String>();
        TokenHierarchy hi = TokenHierarchy.get((Document)this.doc);
        assert (hi != null);
        TokenSequence ts = hi.tokenSequence(HTMLTokenId.language());
        HTMLTagContentsParser p = new HTMLTagContentsParser(result.getRoot());
        p.parse((TokenSequence<HTMLTokenId>)ts, this.visitors);
        return result;
    }

    public static interface TagVisitor {

        public void visitTag(String var1, int var2);

        public void visitArgument(String var1, int var2);

        public void visitArgumentValue(String var1, int var2);

        public void tagClosed(int var1);
    }

}
