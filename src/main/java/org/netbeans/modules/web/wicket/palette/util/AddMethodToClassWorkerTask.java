/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.palette.util;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author Tim Boudreau
 */
class AddMethodToClassWorkerTask implements Task<WorkingCopy> {

    private final NewMethodProperties newMethodProperties;

    public AddMethodToClassWorkerTask(final String methodName, final String methodReturnType, final String body) {
        newMethodProperties = new NewMethodProperties(methodName, methodReturnType);
        newMethodProperties.setBodyContent(body);
    }

    public AddMethodToClassWorkerTask(final NewMethodProperties newMethodProperties) {
        this.newMethodProperties = newMethodProperties;
    }

    @Override
    public void run(WorkingCopy workingCopy) throws IOException {
        // Resolve working copy, so trees are constructed
        workingCopy.toPhase(JavaSource.Phase.RESOLVED);
        // Get the root
        CompilationUnitTree cut = workingCopy.getCompilationUnit();
        TreeMaker treeMaker = workingCopy.getTreeMaker();
        List<ClassTree> listClassTrees = new ArrayList<>();
        for (Tree typeDecl : cut.getTypeDecls()) {
            if (typeDecl.getKind() == Tree.Kind.CLASS) {
                // Class tree found
                listClassTrees.add((ClassTree)typeDecl);
            }

        }
        if (listClassTrees.size() > 1) {
            throw new IllegalArgumentException("Multiple classes found, cannot determine where to insert code");
        }
        if (listClassTrees.isEmpty()) {
            throw new IllegalArgumentException("No class found, no destination to insert code");
        }
        ClassTree clazz = listClassTrees.get(0);
        ModifiersTree methodModifiers = treeMaker.Modifiers(
                // Flags
                Collections.singleton(Modifier.PUBLIC),
                // Annotations
                Collections.emptyList()
        );
        MethodTree newMethod = treeMaker.Method(
                // Modifiers (see above)
                methodModifiers,
                newMethodProperties.getMethodName(),
                treeMaker.Identifier(newMethodProperties.getReturnType()),
                // Parameter types
                Collections.emptyList(),
                // Parameter values
                Collections.emptyList(),
                // Throws clauses
                Collections.emptyList(),
                // Method body
                newMethodProperties.getBody(),
                // Default value (in case of annotations)
                null
        );
        ClassTree modifiedClazz = treeMaker.addClassMember(clazz, newMethod);

        Set<String> existingSpecificMemberImports = new TreeSet<>();
        Set<String> existingWildcardMemberImports = new TreeSet<>();
        if (newMethodProperties.hasRequiredImports()) {
            List<ImportTree> listImportTrees = new ArrayList<>();
            for (Tree impDecl : cut.getImports()) {
                if (impDecl.getKind() == Tree.Kind.IMPORT) {
                    // Class tree found
                    listImportTrees.add((ImportTree)impDecl);
                }
            }
            for (ImportTree importTree : listImportTrees) {
                if (!importTree.isStatic()) {
                    Tree identifierTree = importTree.getQualifiedIdentifier();
                    Tree.Kind kind = identifierTree.getKind();
                    System.out.println("IMPORT-TREE: kind = " + kind.name());
                    if (identifierTree.getKind() == Tree.Kind.MEMBER_SELECT) {
                        MemberSelectTree mst = (MemberSelectTree)identifierTree;
                        String identifier = mst.getIdentifier().toString();
                        System.out.println("EXPRESSION-KIND: " + mst.getExpression().getKind());
                        MemberSelectTree exp = (MemberSelectTree)mst.getExpression();
                        identifier = exp.getIdentifier().toString();
                        if (identifier.endsWith(".*")) {
                            existingWildcardMemberImports.add(identifier);
                        } else {
                            existingSpecificMemberImports.add(identifier.substring(0, identifier.length() - 1));
                        }
                    }
                }
            }
            Set<String> missingImports = new TreeSet<>();
            for (String reqImport : newMethodProperties.getRequiredImports()) {
                if (!existingSpecificMemberImports.contains(reqImport)) {
                    if (!matchesAnyStartValue(existingWildcardMemberImports, reqImport)) {
                        missingImports.add(reqImport);
                    }
                }
            }
            System.out.println("" + missingImports.size());
//            for (String missingImport : missingImports) {
//                MemberSelectTree newImport = treeMaker.MemberSelect(expression, missingImport)
//                        // Modifiers (see above)
//                        methodModifiers,
//                        newMethodProperties.getMethodName(),
//                        treeMaker.Identifier(newMethodProperties.getReturnType()),
//                        // Parameter types
//                        Collections.emptyList(),
//                        // Parameter values
//                        Collections.emptyList(),
//                        // Throws clauses
//                        Collections.emptyList(),
//                        // Method body
//                        newMethodProperties.getBody(),
//                        // Default value (in case of annotations)
//                        null
//                );
//            }
        }
        workingCopy.rewrite(clazz, modifiedClazz);
    }

    private boolean matchesAnyStartValue(Set<String> startValues, String query) {
        int p = query.lastIndexOf('.');
        String prefix = p < 0 ? null : query.substring(0, p + 1);
        if (prefix == null) {
            // Never insert import without package, so simulate existing value
            return true;
        } else {
            return startValues.contains(prefix);
        }
    }
}
