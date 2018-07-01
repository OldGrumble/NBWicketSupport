/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.util;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

/**
 * 
 * @author Tim Boudreau
 */
public final class Invocation implements Comparable<Invocation> {

    private final MethodInvocationTree invocation;
    private final Element target;
    private final Element argument;
    private final NewClassTree src;
    private final NewClassTree parent;
    private final long end;
    private final long start;
    private final TypeMirror occursIn;

    public Invocation(TypeMirror occursIn, MethodInvocationTree invocation, Element target, Element argument, NewClassTree constructionOfAddedComponent, NewClassTree constructionOfParentComponent, long start, long end) {
        this.occursIn = occursIn;
        this.invocation = invocation;
        this.target = target;
        this.src = constructionOfAddedComponent;
        this.parent = constructionOfParentComponent;
        this.argument = argument;
        this.start = start;
        this.end = end;
    }

    public NewClassTree getParent() {
        return this.parent;
    }

    public TypeMirror getOccursIn() {
        return this.occursIn;
    }

    public Element getArgument() {
        return this.argument;
    }

    public long getEnd() {
        return this.end;
    }

    public long getStart() {
        return this.start;
    }

    public MethodInvocationTree getInvocation() {
        return this.invocation;
    }

    public NewClassTree getSrc() {
        return this.src;
    }

    public Element getTarget() {
        return this.target;
    }

    public String toString() {
        return this.invocation + " target is " + this.target + " arg is " + this.argument + " src is " + this.src + " occurs in type " + this.occursIn;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Invocation other = (Invocation)obj;
        if (!(this.invocation == other.invocation || this.invocation != null && this.invocation.equals(other.invocation))) {
            return false;
        }
        if (!(this.target == other.target || this.target != null && this.target.equals(other.target))) {
            return false;
        }
        if (!(this.argument == other.argument || this.argument != null && this.argument.equals(other.argument))) {
            return false;
        }
        if (!(this.src == other.src || this.src != null && this.src.equals(other.src))) {
            return false;
        }
        if (!(this.parent == other.parent || this.parent != null && this.parent.equals(other.parent))) {
            return false;
        }
        if (this.end != other.end) {
            return false;
        }
        if (this.start != other.start) {
            return false;
        }
        if (!(this.occursIn == other.occursIn || this.occursIn != null && this.occursIn.equals(other.occursIn))) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.invocation != null ? this.invocation.hashCode() : 0);
        hash = 59 * hash + (this.target != null ? this.target.hashCode() : 0);
        hash = 59 * hash + (this.argument != null ? this.argument.hashCode() : 0);
        hash = 59 * hash + (this.src != null ? this.src.hashCode() : 0);
        hash = 59 * hash + (this.parent != null ? this.parent.hashCode() : 0);
        hash = 59 * hash + (int)(this.end ^ this.end >>> 32);
        hash = 59 * hash + (int)(this.start ^ this.start >>> 32);
        hash = 59 * hash + (this.occursIn != null ? this.occursIn.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(Invocation o) {
        if (this.target == null && o.target != null) {
            return -1;
        }
        if (this.target != null && o.target == null) {
            return 1;
        }
        if (this.parent == null && o.parent != null) {
            return -1;
        }
        if (this.parent != null && o.parent == null) {
            return 1;
        }
        return (int)(o.start - this.start);
    }
}
