/*
 * Some license issues have still to be clarified, especially for the "borrowed"
 * package, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.finders;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreeScanner;
import java.util.Set;

public class ConstructorFinder extends TreeScanner<Void, Set<MethodTree>> {

    @Override
    public Void visitMethod(MethodTree mt, Set<MethodTree> set) {
        if ("<init>".equals(mt.getName().toString())) {
            set.add(mt);
        }
        return (Void)super.visitMethod(mt, set);
    }
}
