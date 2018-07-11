/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.html.editor.api.gsf.CustomAttribute;
import org.netbeans.modules.html.editor.api.gsf.CustomTag;
import org.netbeans.modules.html.editor.api.gsf.HtmlExtension;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.elements.Attribute;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.Named;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.web.wicket.editor.model.WicketNodeType;
import org.netbeans.modules.web.wicket.editor.model.WicketNodesExtractor;
import org.netbeans.modules.web.wicket.editor.model.WicketNodesModel;
import org.netbeans.modules.web.wicket.editor.model.WicketToken;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 * This class extends HTML syntax, so wicket additions are not flagged as
 * errors.<br>
 * <br>
 * TODO: This class is still in early testing stage!
 *
 * @author Peter Nabbefeld
 */
@MimeRegistrations({
    @MimeRegistration(mimeType = "text/html", service = HtmlExtension.class)
})
public class WicketHtmlExtension extends HtmlExtension {

    private Collection<CustomTag> tags;

    public WicketHtmlExtension() {
    }

    @Override
    public boolean isApplicationPiece(HtmlParserResult result) {
        return true;
    }

    @Override
    public Map<OffsetRange, Set<ColoringAttributes>> getHighlights(HtmlParserResult result, SchedulerEvent event) {
        final Map<OffsetRange, Set<ColoringAttributes>> highlights = new HashMap<>();
        WicketNodesModel model = WicketNodesExtractor.getWicketNodesModel(result);
        for (Map.Entry<WicketNodeType, List<WicketToken>> entry : model.getWicketTokens().entrySet()) {
            Set<ColoringAttributes> coloringAttributes = entry.getKey().getColoringAttributes();
            for (WicketToken token : entry.getValue()) {
                highlights.put(token.getRange(), coloringAttributes);
            }
        }
        return highlights;
    }

    @Override
    public List<CompletionItem> completeOpenTags(CompletionContext context) {
        List<CompletionItem> items = new ArrayList<>();
        Collection<CustomTag> customTags = getCustomTags();
        for (CustomTag customTag : customTags) {
            items.add(new WicketTagCompletionItem(customTag.getName(), context.getCCItemStartOffset()));
        }
        return items;
    }

    @Override
    public List<CompletionItem> completeAttributes(CompletionContext context) {
        List<CompletionItem> items = new ArrayList<>();
        Element node = context.getCurrentNode();
        if (node.type() == ElementType.OPEN_TAG) {
            OpenTag tag = (OpenTag)node;
            Collection<CustomAttribute> customAttributes = getCustomAttributes(tag.name().toString());
            for (CustomAttribute customAttribute : customAttributes) {
                items.add(new WicketAttributeCompletionItem(customAttribute.getName(), context.getCCItemStartOffset(), customAttribute.isRequired(), customAttribute.getHelp()));
            }
        }
        return items;
    }

    @Override
    public List<CompletionItem> completeAttributeValue(CompletionContext context) {
        List<CompletionItem> items = new ArrayList<>();
        Element node = context.getCurrentNode();
        if (node.type() == ElementType.OPEN_TAG) {
            OpenTag tag = (OpenTag)node;
            if ("html".equals(tag.name().toString().toLowerCase())) {
                String attributeName = context.getAttributeName();
                if ("xmlns:wicket".equals(attributeName)) {
                    items.add(new WicketAttributeValueCompletionItem("http://wicket.apache.org", context.getCCItemStartOffset()));
                }
            }
        }
        return items;
    }

    @Override
    public boolean isCustomAttribute(Attribute attribute, HtmlSource source) {
        String tagNSName = attribute.name().toString();
//        String tagNS = attribute.namespacePrefix() == null ? null : attribute.namespacePrefix().toString();
//        String tagName = attribute.unqualifiedName().toString();
//        String value = attribute.value().toString();
//        System.out.println("Found Attribute: " + tagNSName + ", " + tagNS + ", " + tagName + ", " + value);
        return tagNSName.startsWith("wicket:") || "xmlns:wicket".equals(tagNSName);
    }

    @Override
    public Collection<CustomAttribute> getCustomAttributes(String elementName) {
        switch (elementName) {
            case "html":
                return Arrays.asList(new CustomAttribute[]{
                    new WicketCustomAttribute("xmlns:wicket")
                });
            default:
                return Arrays.asList(new CustomAttribute[]{
                    new WicketCustomAttribute("id")
                });
        }
    }

    @Override
    public boolean isCustomTag(Named element, HtmlSource source) {
        String tagNSName = element.name().toString();
//        String tagNS = element.namespacePrefix() == null ? null : element.namespacePrefix().toString();
//        String tagName = element.unqualifiedName().toString();
//        ElementType tagType = element.type();
//        Collection<ProblemDescription> problems = element.problems();
//        Collection<Attribute> attributes = null;
//        if (tagType == ElementType.OPEN_TAG) {
//            OpenTag t = (OpenTag)element;
//            attributes = t.attributes();
//        }
//        System.out.println("Found Tag: " + tagNSName + ", " + tagNS + ", " + tagName + ", " + tagType.name() + ", " + attributes);
        return tagNSName.startsWith("wicket:");
    }

    @Override
    public Collection<CustomTag> getCustomTags() {
        if (tags == null) {
            tags = WicketCustomTagsFactory.createAll();
        }
        return tags;
    }

//    @Override
//    public List<CompletionItem> completeOpenTags(CompletionContext context) {
//        List<CompletionItem> items = new ArrayList<>();
//        return items;
//    }
//
//    @Override
//    public DeclarationFinder.DeclarationLocation findDeclaration(ParserResult info, int caretOffset) {
//        FileObject fo = info.getSnapshot().getSource().getFileObject();
//        if (fo == null) {
//            return null;
//        }
//        return null;
//    }
}
