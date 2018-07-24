/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.wicket.tree.util;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.openide.util.Exceptions;

/**
 *
 * @author Peter Nabbefeld
 */
public final class WicketCompilationUtils {

    /**
     * Check if Wicket is available on classpath.
     *
     * @param cc The controller for the current compilation process.
     * @return True, if Wicket is on the classpath.
     */
    public static boolean isWicketOnClasspath(CompilationController cc) {
        try {
            cc.toPhase(JavaSource.Phase.RESOLVED);
            TypeElement markupContainerType = cc.getElements().getTypeElement("org.apache.wicket.MarkupContainer");
            if (markupContainerType != null) {
                return true;
            }
            Exceptions.printStackTrace(new ClassNotFoundException("Wicket not found on classpath"));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    /**
     * Get class trees (usually one).
     *
     * @param cc The controller for the current compilation process.
     * @return list containing the class trees of the current compilation unit.
     */
    @SuppressWarnings("unchecked")
    public static List<ClassTree> getClassTrees(CompilationController cc) {
        ArrayList types = new ArrayList(cc.getCompilationUnit().getTypeDecls());
        Iterator it = types.iterator();
        while (it.hasNext()) {
            Tree tree = (Tree)it.next();
            TypeMirror mirror = cc.getTrees().getTypeMirror(TreePath.getPath(cc.getCompilationUnit(), tree));
            if (!(tree instanceof ClassTree) || !Utils.isWebMarkupContainer(mirror, cc.getTypes())) {
                it.remove();
            }
        }
        return (List<ClassTree>)types;
    }
}
