/*
 * Decompiled with CFR 0_130.
 */
package org.netbeans.modules.web.wicket.tree;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreeScanner;
import java.util.Set;
import javax.lang.model.element.Name;

public class ConstructorFinder
extends TreeScanner<Void, Set<MethodTree>> {
    @Override
    public Void visitMethod(MethodTree mt, Set<MethodTree> set) {
        if ("<init>".equals(mt.getName().toString())) {
            set.add(mt);
        }
        return (Void)super.visitMethod(mt, set);
    }
}

