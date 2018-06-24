/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.api.html.lexer.HTMLTokenId
 *  org.netbeans.api.lexer.Language
 *  org.netbeans.api.lexer.Token
 *  org.netbeans.api.lexer.TokenHierarchy
 *  org.netbeans.api.lexer.TokenId
 *  org.netbeans.api.lexer.TokenSequence
 *  org.openide.util.Exceptions
 */
package org.netbeans.modules.web.wicket.tree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.web.wicket.tree.JavaTreeBuilder;
import org.netbeans.modules.web.wicket.tree.MarkupContainerTree;
import org.openide.util.Exceptions;

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
        TagContentsParser p = new TagContentsParser(result.root);
        p.parse((TokenSequence<HTMLTokenId>)ts, this.visitors);
        return result;
    }

    private static final class TagContentsParser {

        private final TagContentsParser parent;
        private final MarkupContainerTree.N<String> n;

        TagContentsParser(MarkupContainerTree.N<String> n) {
            this(null, n);
        }

        TagContentsParser(TagContentsParser parent, MarkupContainerTree.N<String> n) {
            this.parent = parent;
            this.n = n;
        }

        boolean parse(TokenSequence<HTMLTokenId> ts, Map<String, Set<TagVisitor>> allVisitors) {
            String txt;
            Token myTag = ts.token();
            Set<TagVisitor> visitors = null;
            if (allVisitors != null && myTag != null && (visitors = allVisitors.get(txt = myTag.text().toString())) != null) {
                TagVisitor curr = null;
                try {
                    Iterator<TagVisitor> iterator = visitors.iterator();
                    while (iterator.hasNext()) {
                        TagVisitor v;
                        curr = v = iterator.next();
                        v.visitTag(txt, ts.offset());
                    }
                } catch (RuntimeException e) {
                    visitors.remove(curr);
                    Exceptions.printStackTrace((Throwable)e);
                }
            }
            boolean foundWicketIdDeclaration = false;
            boolean foundOperatorAfterWicketId = false;
            String wicketId = null;
            String lastKey = null;
            HashMap<String, String> keyValuePairs = new HashMap<String, String>();
            int offset = -1;
            boolean result = false;
            MarkupContainerTree.N<String> myNode = this.n;
            block21:
            while (ts.moveNext()) {
                Token tok = ts.token();
                Iterator<TagVisitor> v;
                TagVisitor curr;
                TagVisitor v2;
                switch ((HTMLTokenId)tok.id()) {
                    case WS:
                    case ERROR:
                    case EOL:
                    case TAG_OPEN_SYMBOL: {
                        continue block21;
                    }
                    case CHARACTER:
                    case DECLARATION:
                    case SCRIPT:
                    case SGML_COMMENT:
                    case BLOCK_COMMENT:
                    case TEXT: {
                        foundWicketIdDeclaration = false;
                        foundOperatorAfterWicketId = false;
                        continue block21;
                    }
                    case TAG_CLOSE_SYMBOL: {
                        if (!"/>".equals(tok.text().toString())) {
                            continue block21;
                        }
                        if (visitors != null) {
                            TagVisitor curr2 = null;
                            try {
                                Iterator<TagVisitor> iterator = visitors.iterator();
                                while (iterator.hasNext()) {
                                    curr2 = iterator.next();
                                    curr2.tagClosed(ts.offset());
                                }
                            } catch (RuntimeException e) {
                                visitors.remove(curr2);
                                Exceptions.printStackTrace((Throwable)e);
                            }
                        }
                        return result || wicketId != null;
                    }
                    case TAG_OPEN: {
                        TagContentsParser sub = new TagContentsParser(this, myNode);
                        result |= sub.parse(ts, allVisitors);
                        continue block21;
                    }
                    case TAG_CLOSE: {
                        if (visitors != null) {
                            curr = null;
                            try {
                                v = visitors.iterator();
                                while (v.hasNext()) {
                                    curr = v2 = v.next();
                                    v2.tagClosed(ts.offset());
                                }
                            } catch (RuntimeException e) {
                                visitors.remove(curr);
                                Exceptions.printStackTrace((Throwable)e);
                            }
                        }
                        return result || wicketId != null;
                    }
                    case ARGUMENT: {
                        lastKey = tok.text().toString();
                        foundWicketIdDeclaration = lastKey.equals("wicket:id");
                        if (visitors == null) {
                            continue block21;
                        }
                        curr = null;
                        try {
                            Iterator<TagVisitor> e = visitors.iterator();
                            while (e.hasNext()) {
                                curr = v2 = e.next();
                                v2.visitArgument(lastKey, ts.offset());
                            }
                            continue block21;
                        } catch (RuntimeException e) {
                            visitors.remove(curr);
                            Exceptions.printStackTrace((Throwable)e);
                            continue block21;
                        }
                    }
                    case OPERATOR: {
                        foundOperatorAfterWicketId = true;
                        continue block21;
                    }
                    case VALUE: {
                        String value = tok.text().toString();
                        if (lastKey != null) {
                            keyValuePairs.put(lastKey, value);
                        }
                        if (foundWicketIdDeclaration && foundOperatorAfterWicketId) {
                            wicketId = JavaTreeBuilder.unquote(value);
                            offset = ts.offset();
                            String currTag = myTag == null ? null : myTag.text().toString();
                            myNode = new MarkupContainerTree.N<String>(wicketId, this.n, offset, currTag);
                            myNode.setAttributesMap(keyValuePairs);
                            this.n.add(myNode);
                            foundWicketIdDeclaration = false;
                            foundOperatorAfterWicketId = false;
                        }
                        if (visitors == null) {
                            continue block21;
                        }
                        TagVisitor curr3 = null;
                        try {
                            String txt2 = tok.text().toString();
                            Iterator<TagVisitor> iterator = visitors.iterator();
                            while (iterator.hasNext()) {
                                TagVisitor v3;
                                curr3 = v3 = iterator.next();
                                v3.visitArgumentValue(txt2, ts.offset());
                            }
                            continue block21;
                        } catch (RuntimeException e) {
                            visitors.remove(curr3);
                            Exceptions.printStackTrace((Throwable)e);
                            continue block21;
                        }
                    }
                }
            }
            return result;
        }

        public int depth() {
            TagContentsParser p = this.parent;
            int ix = 0;
            while (p != null) {
                p = p.parent;
                ++ix;
            }
            return ix;
        }

        public String toString() {
            return super.toString() + " depth " + this.depth();
        }
    }

    public static interface TagVisitor {

        public void visitTag(String var1, int var2);

        public void visitArgument(String var1, int var2);

        public void visitArgumentValue(String var1, int var2);

        public void tagClosed(int var1);
    }

}
