/*
 * Decompiled with CFR 0_130.
 * 
 * Could not load the following classes:
 *  org.netbeans.api.java.source.CompilationController
 *  org.netbeans.api.java.source.TreePathHandle
 *  org.openide.filesystems.FileObject
 */
package org.netbeans.modules.web.wicket.tree.util;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.NewClassTree;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.TreePathHandle;
import org.openide.filesystems.FileObject;

public interface ProblemFinderVisitor {

    public void visitWicketAddInvocation(Invocation var1, CompilationController var2, FileObject var3, Collection<? super Problem> var4);

    public void visitWicketComponentConstruction(NewClassTree var1, CompilationController var2, FileObject var3, List<String> var4, Collection<? super Problem> var5);

    public void visitWicketMarkupContainer(ClassTree var1, CompilationController var2, FileObject var3, Collection<? super Problem> var4);

    public static final class Problem {

        private Map<String, Object> keyValuePairs;
        private final TreePathHandle handle;
        private final String description;
        private final FileObject file;

        public void setKeyValuePairs(Map<String, Object> keyValuePairs) {
            this.keyValuePairs = keyValuePairs;
        }

        public Problem(String description, FileObject file, TreePathHandle handle) {
            assert (file != null);
            assert (description != null);
            this.description = description;
            this.file = file;
            this.handle = handle;
        }

        public final void put(String key, Object val) {
            if (this.keyValuePairs == null) {
                this.keyValuePairs = new HashMap<String, Object>();
            }
            this.keyValuePairs.put(key, val);
        }

        public Map getKeyValuePairs() {
            return Collections.unmodifiableMap(this.keyValuePairs);
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            Problem other = (Problem)obj;
            if (!(this.handle == other.handle || this.handle != null && this.handle.equals((Object)other.handle))) {
                return false;
            }
            if (!(this.description == other.description || this.description != null && this.description.equals(other.description))) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + (this.handle != null ? this.handle.hashCode() : 0);
            hash = 97 * hash + (this.description != null ? this.description.hashCode() : 0);
            return hash;
        }
    }

}
