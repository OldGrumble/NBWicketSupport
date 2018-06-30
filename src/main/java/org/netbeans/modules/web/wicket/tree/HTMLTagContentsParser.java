/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.wicket.tree;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.web.wicket.util.StringUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author peter
 */
final class HTMLTagContentsParser {

    private final HTMLTagContentsParser parent;
    private final NodeImpl<String> node;

    HTMLTagContentsParser(Node<String> n) {
        this(null, n);
    }

    HTMLTagContentsParser(HTMLTagContentsParser parent, Node<String> n) {
        this.parent = parent;
        this.node = (NodeImpl<String>)n;
    }

    boolean parse(TokenSequence<HTMLTokenId> ts, Map<String, Set<HtmlTreeBuilder.TagVisitor>> allVisitors) {
        String txt;
        Token myTag = ts.token();
        Set<HtmlTreeBuilder.TagVisitor> visitors = null;
        if (allVisitors != null && myTag != null && (visitors = allVisitors.get(txt = myTag.text().toString())) != null) {
            HtmlTreeBuilder.TagVisitor curr = null;
            try {
                Iterator<HtmlTreeBuilder.TagVisitor> iterator = visitors.iterator();
                while (iterator.hasNext()) {
                    HtmlTreeBuilder.TagVisitor v;
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
        HashMap<String, String> keyValuePairs = new HashMap<>();
        int offset;
        boolean result = false;
        NodeImpl<String> myNode = this.node;
        while (ts.moveNext()) {
            Token tok = ts.token();
            Iterator<HtmlTreeBuilder.TagVisitor> v;
            HtmlTreeBuilder.TagVisitor curr;
            HtmlTreeBuilder.TagVisitor v2;
            switch ((HTMLTokenId)tok.id()) {
                case WS:
                case ERROR:
                case EOL:
                case TAG_OPEN_SYMBOL:
                    break;
                case CHARACTER:
                case DECLARATION:
                case SCRIPT:
                case SGML_COMMENT:
                case BLOCK_COMMENT:
                case TEXT: {
                    foundWicketIdDeclaration = false;
                    foundOperatorAfterWicketId = false;
                    break;
                }
                case TAG_CLOSE_SYMBOL: {
                    if (!"/>".equals(tok.text().toString())) {
                        break;
                    }
                    if (visitors != null) {
                        HtmlTreeBuilder.TagVisitor curr2 = null;
                        try {
                            Iterator<HtmlTreeBuilder.TagVisitor> iterator = visitors.iterator();
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
                    HTMLTagContentsParser sub = new HTMLTagContentsParser(this, myNode);
                    result |= sub.parse(ts, allVisitors);
                    break;
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
                        break;
                    }
                    curr = null;
                    try {
                        Iterator<HtmlTreeBuilder.TagVisitor> e = visitors.iterator();
                        while (e.hasNext()) {
                            curr = e.next();
                            curr.visitArgument(lastKey, ts.offset());
                        }
                        break;
                    } catch (RuntimeException e) {
                        visitors.remove(curr);
                        Exceptions.printStackTrace((Throwable)e);
                        break;
                    }
                }
                case OPERATOR: {
                    foundOperatorAfterWicketId = true;
                    break;
                }
                case VALUE: {
                    String value = tok.text().toString();
                    if (lastKey != null) {
                        keyValuePairs.put(lastKey, value);
                    }
                    if (foundWicketIdDeclaration && foundOperatorAfterWicketId) {
                        wicketId = StringUtils.unquote(value);
                        offset = ts.offset();
                        String currTag = myTag == null ? null : myTag.text().toString();
                        myNode = new NodeImpl<>(wicketId, this.node, offset, currTag);
                        myNode.setAttributesMap(keyValuePairs);
                        this.node.add(myNode);
                        foundWicketIdDeclaration = false;
                        foundOperatorAfterWicketId = false;
                    }
                    if (visitors == null) {
                        break;
                    }
                    HtmlTreeBuilder.TagVisitor curr3 = null;
                    try {
                        String txt2 = tok.text().toString();
                        Iterator<HtmlTreeBuilder.TagVisitor> iterator = visitors.iterator();
                        while (iterator.hasNext()) {
                            curr  = iterator.next();
                            curr.visitArgumentValue(txt2, ts.offset());
                        }
                        break;
                    } catch (RuntimeException e) {
                        visitors.remove(curr3);
                        Exceptions.printStackTrace((Throwable)e);
                        break;
                    }
                }
            }
        }
        return result;
    }

    public int depth() {
        HTMLTagContentsParser p = this.parent;
        int ix = 0;
        while (p != null) {
            p = p.parent;
            ++ix;
        }
        return ix;
    }

    @Override
    public String toString() {
        return super.toString() + " depth " + this.depth();
    }
}
