/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.modules.web.wicket.tree.util;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * 
 * @author Tim Boudreau
 */
public final class Utils {

    public static List<TypeMirror> toTypeMirrors(Iterable<? extends TypeMirrorHandle> types, CompilationInfo info) {
        ArrayList<TypeMirror> result = new ArrayList<>();
        for (TypeMirrorHandle h : types) {
            result.add(h.resolve(info));
        }
        return result;
    }

    public static List<TypeMirrorHandle> toTypeMirrorHandles(Iterable<? extends TypeMirror> types) {
        ArrayList<TypeMirrorHandle> result = new ArrayList<>();
        for (TypeMirror h : types) {
            result.add(TypeMirrorHandle.create((TypeMirror)h));
        }
        return result;
    }

    @SuppressWarnings("null")
    public static List<Tree> toTrees(Iterable<? extends TreePathHandle> handles, CompilationInfo info) {
        ArrayList<Tree> result = new ArrayList<>(handles instanceof Collection ? ((Collection)handles).size() : 11);
        for (TreePathHandle handle : handles) {
            TreePath path = handle.resolve(info);
            if (path == null) {
                try {
                    JavaSource src = JavaSource.forFileObject((FileObject)handle.getFileObject());
                    TreePathResolver res = new TreePathResolver(handle);
                    if (res.cancelled) {
                        break;
                    }
                    path = res.path;
                    src.runUserActionTask((Task)res, true);
                } catch (IOException ex) {
                    Exceptions.printStackTrace((Throwable)ex);
                }
            }
            if (path == null) {
                continue;
            }
            result.add(path.getLeaf());
        }
        return result;
    }

    public static List<TreePathHandle> toHandles(TreePath parent, Iterable<? extends Tree> trees, CompilationInfo info) {
        ArrayList<TreePathHandle> result = new ArrayList<>(trees instanceof Collection ? ((Collection)trees).size() : 11);
        for (Tree tree : trees) {
            TreePath path = TreePath.getPath(parent, tree);
            TreePathHandle handle = TreePathHandle.create((TreePath)path, (CompilationInfo)info);
            result.add(handle);
            assert (handle.resolve(info) != null);
            assert (handle.resolve(info).getLeaf() != null);
        }
        return result;
    }

    public static List<TreePathHandle> toHandles(Iterable<? extends Tree> trees, CompilationInfo info) {
        ArrayList<TreePathHandle> result = new ArrayList<>(trees instanceof Collection ? ((Collection)trees).size() : 11);
        for (Tree tree : trees) {
            TreePath path = TreePath.getPath(info.getCompilationUnit(), tree);
            if (path == null) {
                throw new IllegalArgumentException(tree + " does not belong to " + "the same compilation unit passed to this method");
            }
            TreePathHandle handle = TreePathHandle.create((TreePath)path, (CompilationInfo)info);
            result.add(handle);
            assert (handle.resolve(info) != null);
            assert (handle.resolve(info).getLeaf() != null);
        }
        return result;
    }

    public static <T extends Element> List<ElementHandle<T>> toHandles(Iterable<? extends T> elements) {
        ArrayList<ElementHandle<T>> result = new ArrayList<>(elements instanceof Collection ? ((Collection)elements).size() : 11);
        for (Element element : elements) {
            ElementHandle handle = ElementHandle.create((Element)element);
            assert (handle != null);
            result.add(handle);
        }
        return result;
    }

    public static <T extends Element> Map<Element, ElementHandle> mapHandles(Iterable<? extends T> elements) {
        HashMap<Element, ElementHandle> result = new HashMap<>(elements instanceof Collection ? ((Collection)elements).size() : 11);
        for (Element element : elements) {
            ElementHandle handle = ElementHandle.create((Element)element);
            assert (handle != null);
            result.put(element, handle);
        }
        return result;
    }

    public static <T extends Element> List<Element> fromElementHandles(Iterable<ElementHandle<T>> handles, CompilationInfo info) {
        ArrayList<Element> result = new ArrayList<>(handles instanceof Collection ? ((Collection)handles).size() : 0);
        for (ElementHandle<T> h : handles) {
            Element element = h.resolve(info);
            assert (element != null);
            result.add(element);
        }
        return result;
    }

    public static TypeMirror hackFqn(TypeMirror mirror, TreeMaker maker, Trees trees, CompilationInfo info) {
        Tree typeTree = maker.Type(mirror);
        TreePath path = TreePath.getPath(info.getCompilationUnit(), typeTree);
        TypeMirror result = null;
        if (path != null) {
            try {
                result = trees.getTypeMirror(path);
            } catch (NullPointerException npe) {
                Exceptions.printStackTrace((Throwable)npe);
            }
        }
        return result == null ? mirror : result;
    }

    public static Tree cloneTree(Tree old, Element element, TreeMaker maker, WorkingCopy wc) {
        Tree result = null;
        switch (element.getKind()) {
            case METHOD: {
                MethodTree mt = (MethodTree)old;
                ArrayList<? extends VariableTree> parameters = new ArrayList<>(mt.getParameters());
                ArrayList<? extends TypeParameterTree> typeParameters = new ArrayList<>(mt.getTypeParameters());
                ArrayList<? extends ExpressionTree> throes = new ArrayList<>(mt.getThrows());
                Tree ret = mt.getReturnType();
                BlockTree body = mt.getBody();
                ExpressionTree defaultValue = (ExpressionTree)mt.getDefaultValue();
                String name = mt.getName().toString();
                ModifiersTree modifiers = mt.getModifiers();
                result = maker.Method(modifiers, (CharSequence)name, ret, typeParameters, parameters, throes, body, defaultValue);
                break;
            }
            case CONSTRUCTOR: {
                MethodTree mt = (MethodTree)old;
                ArrayList<? extends VariableTree> parameters = new ArrayList<>(mt.getParameters());
                ArrayList<? extends TypeParameterTree> typeParameters = new ArrayList<>(mt.getTypeParameters());
                ArrayList<? extends ExpressionTree> throes = new ArrayList<>(mt.getThrows());
                BlockTree body = mt.getBody();
                CompilationUnitTree cut = wc.getCompilationUnit();
                SourcePositions sp = wc.getTrees().getSourcePositions();
                int start = (int)sp.getStartPosition(cut, body);
                int end = (int)sp.getEndPosition(cut, body);
                String bodyText = wc.getText().substring(start, end);
                ExpressionTree defaultValue = (ExpressionTree)mt.getDefaultValue();
                ModifiersTree modifiers = mt.getModifiers();
                result = maker.Method(modifiers, (CharSequence)"<init>", null, typeParameters, parameters, throes, bodyText, defaultValue);
                break;
            }
            case FIELD: {
                VariableTree ft = (VariableTree)old;
                VariableElement ve = (VariableElement)element;
                Tree typeTree = maker.Type(ve.asType());
                ModifiersTree mt = ft.getModifiers();
                result = maker.Variable(mt, (CharSequence)ft.getName().toString(), typeTree, ft.getInitializer());
                break;
            }
            case CLASS: {
                ClassTree ct = (ClassTree)old;
                ModifiersTree modifiers = ct.getModifiers();
                String simpleName = ct.getSimpleName().toString();
                ArrayList<? extends TypeParameterTree> typeParameters = new ArrayList<>(ct.getTypeParameters());
                ArrayList<? extends Tree> implementsClauses = new ArrayList<>(ct.getImplementsClause());
                ArrayList<? extends Tree> memberDecls = new ArrayList<>(ct.getMembers());
                result = maker.Class(modifiers, (CharSequence)simpleName, typeParameters, result, implementsClauses, memberDecls);
                break;
            }
            default: {
                result = null;
            }
        }
        return result;
    }

    public static List<TreePath> toPaths(TreePath parent, List<? extends Tree> trees, CompilationInfo info) {
        ArrayList<TreePath> result = new ArrayList<>(trees.size());
        for (Tree tree : trees) {
            TreePath path = TreePath.getPath(parent, tree);
            result.add(path);
        }
        return result;
    }

    public static List<TreePathHandle> fromElements(Iterable<? extends Element> elements, CompilationInfo info) {
        ArrayList<TreePathHandle> result = new ArrayList<>(elements instanceof Collection ? ((Collection)elements).size() : 11);
        for (Element e : elements) {
            TreePathHandle handle = TreePathHandle.create((Element)e, (CompilationInfo)info);
            result.add(handle);
        }
        return result;
    }

    public static List<TreePath> toPaths(Iterable<? extends Element> elements, Trees trees) {
        ArrayList<TreePath> result = new ArrayList<>(elements instanceof Collection ? ((Collection)elements).size() : 11);
        for (Element e : elements) {
            TreePath path = trees.getPath(e);
            result.add(path);
        }
        return result;
    }

    public static List<Element> toElements(Iterable<? extends TreePathHandle> handles, CompilationInfo info) {
        ArrayList<Element> result = new ArrayList<>(handles instanceof Collection ? ((Collection)handles).size() : 11);
        for (TreePathHandle handle : handles) {
            Element element = handle.resolveElement(info);
            result.add(element);
        }
        return result;
    }

    public static boolean isStatic(Element el, Scope scope, CompilationController cc) {
        return cc.getTreeUtilities().isStaticContext(scope) || el.getModifiers().contains(Modifier.STATIC);
    }

    public static BlockTree findBlockTree(Tree tree) {
        BlockTree result = null;
        if (tree instanceof MethodTree) {
            MethodTree mt = (MethodTree)tree;
            result = mt.getBody();
        } else if (tree instanceof BlockTree) {
            result = (BlockTree)tree;
        }
        return result;
    }

    @SuppressWarnings("null")
    public static Tree resolveTreePathHandle(TreePathHandle handle) throws IOException {
        FileObject fob = handle.getFileObject();
        JavaSource src = JavaSource.forFileObject((FileObject)fob);
        TreeFinder finder = new TreeFinder(handle);
        src.runUserActionTask(finder, true);
        return finder.tree;
    }

    public static List<Tree> resolveTreePathHandles(Iterable<TreePathHandle> handles) throws IOException {
        ArrayList<Tree> result = new ArrayList<>(handles instanceof Collection ? ((Collection)handles).size() : 10);
        for (TreePathHandle handle : handles) {
            Tree tree = Utils.resolveTreePathHandle(handle);
            result.add(tree);
        }
        return result;
    }

    @SuppressWarnings("null")
    public static Collection<TreePathHandle> getOverridingMethodHandles(ExecutableElement e, CompilationController cc) throws IOException {
        Collection<ElementHandle<ExecutableElement>> mtds = Utils.getOverridingMethods(e, (CompilationInfo)cc);
        HashSet<TreePathHandle> result = new HashSet<>();
        ElementHandle toFind = ElementHandle.create((Element)e);
        for (ElementHandle<ExecutableElement> element : mtds) {
            FileObject fob = SourceUtils.getFile(element, (ClasspathInfo)cc.getClasspathInfo());
            JavaSource src = JavaSource.forFileObject((FileObject)fob);
            assert (src.getFileObjects().contains(fob));
            TreeFromElementFinder finder = new TreeFromElementFinder(element);
            src.runUserActionTask((Task)finder, false);
            if (finder.handle == null) {
                continue;
            }
            result.add(finder.handle);
        }
        return result;
    }

    @SuppressWarnings("null")
    public static Collection<ElementHandle<ExecutableElement>> getOverridingMethods(ExecutableElement e, CompilationInfo info) {

        Collection<ElementHandle<ExecutableElement>> result = new ArrayList<>();
        TypeElement parentType = (TypeElement)e.getEnclosingElement();
        Set<ElementHandle<TypeElement>> subTypes = info.getClasspathInfo().getClassIndex().getElements(
                ElementHandle.create(parentType),
                EnumSet.of(org.netbeans.api.java.source.ClassIndex.SearchKind.IMPLEMENTORS),
                EnumSet.of(org.netbeans.api.java.source.ClassIndex.SearchScope.SOURCE)
        );
        for (ElementHandle subTypeHandle : subTypes) {
            TypeElement type = (TypeElement)subTypeHandle.resolve(info);
            for (ExecutableElement method : ElementFilter.methodsIn(type.getEnclosedElements())) {
                if (info.getElements().overrides(method, e, type)) {
                    result.add(ElementHandle.create(method));
                }
            }
        }
        return result;
    }

    public static Collection<TreePathHandle> getInvocationsOf(ElementHandle e, CompilationController wc) throws IOException {
        assert (e != null);
        assert (wc != null);
        wc.toPhase(JavaSource.Phase.RESOLVED);
        Element element = e.resolve((CompilationInfo)wc);
        TypeElement type = wc.getElementUtilities().enclosingTypeElement(element);
        ElementHandle<TypeElement> elh = ElementHandle.create(type);
        assert (elh != null);
        Set<ElementHandle<TypeElement>> classes = wc.getClasspathInfo().getClassIndex().getElements(
                elh,
                EnumSet.of(ClassIndex.SearchKind.METHOD_REFERENCES),
                EnumSet.of(ClassIndex.SearchScope.SOURCE)
        );
        ArrayList<TreePathHandle> result = new ArrayList<>();
        for (ElementHandle h : classes) {
            result.addAll(Utils.getReferencesToMember((ElementHandle<TypeElement>)h, wc.getClasspathInfo(), e));
        }
        return result;
    }

    @SuppressWarnings("null")
    public static Collection<TreePathHandle> getReferencesToMember(ElementHandle<TypeElement> on, ClasspathInfo info, ElementHandle toFind) throws IOException {
        FileObject ob = SourceUtils.getFile(on, (ClasspathInfo)info);
        assert (ob != null);
        JavaSource src = JavaSource.forFileObject((FileObject)ob);
        InvocationScanner scanner = new InvocationScanner(toFind);
        src.runUserActionTask((Task)scanner, true);
        return scanner.usages;
    }

    @SuppressWarnings("null")
    public static <R, D> Map<TreePathHandle, Map<TreeVisitor<R, D>, R>> runAgainstSources(Iterable<TreePathHandle> handles, D arg, TreeVisitor<R, D>... visitors) throws IOException {
        HashMap<TreePathHandle, Map<TreeVisitor<R, D>, R>> results = new HashMap<>();
        for (TreePathHandle handle : handles) {
            FileObject fob = handle.getFileObject();
            JavaSource src = JavaSource.forFileObject((FileObject)fob);
            MultiVisitorRunner<R, D> runner = new MultiVisitorRunner<>(handle, arg, visitors);
            src.runUserActionTask(runner, true);
            results.put(handle, runner.results);
        }
        return results;
    }

    @SuppressWarnings("null")
    public static void runAgainstSources(Iterable<TreePathHandle> handles, CancellableTask<CompilationController> c) throws IOException {
        for (TreePathHandle handle : handles) {
            FileObject fob = handle.getFileObject();
            JavaSource src = JavaSource.forFileObject((FileObject)fob);
            src.runUserActionTask(c, true);
        }
    }

    @SuppressWarnings("null")
    public static <T> void runAgainstSources(Iterable<TreePathHandle> handles, TreePathHandleTask<T> t, T arg) throws IOException {
        for (TreePathHandle handle : handles) {
            FileObject file = handle.getFileObject();
            t.handle = handle;
            t.arg = arg;
            t.file = file;
            JavaSource src = JavaSource.forFileObject((FileObject)file);
            src.runUserActionTask(t, true);
        }
        t.handle = null;
        t.arg = null;
        t.file = null;
    }

    public static TreePath findEnclosingClass(CompilationInfo javac, TreePath path, boolean isClass, boolean isInterface, boolean isEnum, boolean isAnnotation, boolean isAnonymous) {
        Tree selectedTree = path.getLeaf();
        TreeUtilities utils = javac.getTreeUtilities();
        do {
            if (Tree.Kind.CLASS == selectedTree.getKind()) {
                ClassTree classTree = (ClassTree)selectedTree;
                if (isEnum && utils.isEnum(classTree) || isInterface && utils.isInterface(classTree) || isAnnotation && utils.isAnnotation(classTree) || isClass && !utils.isInterface(classTree) && !utils.isEnum(classTree) && !utils.isAnnotation(classTree)) {
                    Tree.Kind parentKind = path.getParentPath().getLeaf().getKind();
                    if (isAnonymous || Tree.Kind.NEW_CLASS != parentKind) {
                        break;
                    }
                }
            }
            if ((path = path.getParentPath()) == null) {
                selectedTree = javac.getCompilationUnit().getTypeDecls().get(0);
                path = javac.getTrees().getPath(javac.getCompilationUnit(), selectedTree);
                break;
            }
            selectedTree = path.getLeaf();
        } while (true);
        return path;
    }

    public static boolean isParentPath(TreePath targetParent, TreePath test) {
        assert (test != null);
        assert (targetParent != null);
        assert (test.getLeaf() != null);
        assert (targetParent.getLeaf() != null);
        if (test.getLeaf().equals(targetParent.getLeaf())) {
            return true;
        }
        while ((test = test.getParentPath()) != null && !test.getLeaf().equals(targetParent)) {
        }
        boolean result = test != null;
        System.err.println("ipp " + result);
        return result;
    }

    private static boolean isEnclosedBy(TypeElement el, TypeElement maybeParent, CompilationInfo compiler) {
        ElementUtilities utils = compiler.getElementUtilities();
        boolean result = false;
        Element current = el;
        ElementHandle b = ElementHandle.create((Element)maybeParent);
        while (current.getKind() != ElementKind.PACKAGE && !(result = Utils.elementsEqual(current, maybeParent, compiler))) {
            current = current.getEnclosingElement();
        }
        return result;
    }

    public static String getQualification(TreePath pathToMemberSelect, TypeElement ownerOfMemberSelect, Element selected, CompilationInfo compiler) {
        Trees trees = compiler.getTrees();
        ElementUtilities utils = compiler.getElementUtilities();
        TreePath pathToOwnerOfSelectedMember = Utils.findEnclosingClass(compiler, pathToMemberSelect, true, true, false, false, false);
        TypeElement ownerType = (TypeElement)trees.getElement(pathToOwnerOfSelectedMember);
        TreePath pathToOwnerOfMemberSelect = Utils.findEnclosingClass(compiler, pathToOwnerOfSelectedMember, true, false, true, false, true);
        String result = null;
        boolean enclosed = Utils.isParentPath(pathToOwnerOfSelectedMember, pathToOwnerOfMemberSelect);
        boolean isSuperType = Utils.isSupertype(ownerOfMemberSelect, ownerType, compiler);
        boolean same = pathToOwnerOfSelectedMember.getLeaf().equals(pathToOwnerOfMemberSelect.getLeaf());
        if (same || enclosed || isSuperType) {
            boolean isElementStatic = selected.getModifiers().contains(Modifier.STATIC);
            if (isElementStatic) {
                System.err.println("   a");
                PackageElement ownerPackage = compiler.getElements().getPackageOf(ownerType);
                PackageElement selectPackage = compiler.getElements().getPackageOf(ownerOfMemberSelect);
                boolean fqn = ownerPackage != null && !ownerPackage.equals(selectPackage);
                result = fqn ? ownerOfMemberSelect.getQualifiedName().toString() : ownerOfMemberSelect.getSimpleName().toString();
            } else if (same) {
                result = "this";
            } else {
                TypeElement last;
                Element el;
                Element enc = ownerType.getEnclosingElement();
                System.err.println("   d");
                TypeElement owner = utils.enclosingTypeElement(enc);
                ArrayList<String> strings = new ArrayList<>();
                strings.add("this");
                do {
                    strings.add(0, owner.getSimpleName().toString());
                    last = owner;
                    el = owner.getEnclosingElement();
                } while (el != null && el.getKind() != ElementKind.PACKAGE && last != (owner = utils.enclosingTypeElement(el)) && ownerType != null && ownerType.getNestingKind() != NestingKind.TOP_LEVEL);
                StringBuilder sb = new StringBuilder();
                Iterator it = strings.iterator();
                while (it.hasNext()) {
                    sb.append((String)it.next());
                    if (!it.hasNext()) {
                        continue;
                    }
                    sb.append('.');
                }
                result = sb.toString();
            }
        }
        return result;
    }

    public static boolean isSupertype(TypeElement ownerOfMemberSelect, TypeElement memberOwner, CompilationInfo compiler) {
        boolean result = false;
        TypeElement supertype = ownerOfMemberSelect;
        while (supertype != null) {
            TypeMirror type;
            result = Utils.elementsEqual(supertype, memberOwner, compiler);
            List<? extends TypeMirror> ifaces = supertype.getInterfaces();
            for (TypeMirror t : ifaces) {
                Element e = compiler.getTypes().asElement(t);
                result = Utils.elementsEqual(supertype, e, compiler);
            }
            if (result || (type = supertype.getSuperclass()).getKind() == TypeKind.NONE) {
                break;
            }
            Element e = compiler.getTypes().asElement(supertype.getSuperclass());
            if (e instanceof TypeElement) {
                supertype = (TypeElement)e;
                continue;
            }
            supertype = null;
        }
        return result;
    }

    public static boolean elementsEqual(Element a, Element b, CompilationInfo info) {
        boolean result;
        if (a == b) {
            result = true;
        } else if (a.getKind() == b.getKind()) {
            ElementHandle e1 = ElementHandle.create((Element)a);
            ElementHandle e2 = ElementHandle.create((Element)b);
            result = e1.equals((Object)e2);
        } else {
            result = false;
        }
        return result;
    }

    public static boolean isWebMarkupContainer(Trees trees, Types types, CompilationUnitTree unit, Tree t) {
        TypeMirror mirror = trees.getTypeMirror(TreePath.getPath(unit, t));
        return Utils.isWebMarkupContainer(mirror, types);
    }

    public static boolean isWebMarkupContainer(TypeMirror mirror, Types types) {
        boolean result;
        block1:
        {
            result = false;
            if (mirror == null || "java.lang.Object".equals(mirror.toString()) || (result |= "org.apache.wicket.Component".equals(mirror.toString()))) {
                break block1;
            }
            List<? extends TypeMirror> l = types.directSupertypes(mirror);
            for (TypeMirror tm : l) {
                if (result |= Utils.isWebMarkupContainer(tm, types)) {
                    break;
                }
            }
        }
        return result;
    }

    static boolean isRepeater(Trees trees, Types types, CompilationUnitTree unit, Tree t) {
        TypeMirror mirror = trees.getTypeMirror(TreePath.getPath(unit, t));
        return Utils.isWebMarkupContainer(mirror, types);
    }

    static boolean isRepeater(TypeMirror mirror, Types types) {
        boolean result;
        block1:
        {
            result = false;
            if (mirror == null || "java.lang.Object".equals(mirror.toString()) || (result |= "org.apache.wicket.markup.repeater.RepeatingView".equals(mirror.toString()))) {
                break block1;
            }
            List<? extends TypeMirror> l = types.directSupertypes(mirror);
            for (TypeMirror tm : l) {
                if (result |= Utils.isWebMarkupContainer(tm, types)) {
                    break;
                }
            }
        }
        return result;
    }

    private static final class InvocationScanner extends TreePathScanner<Tree, ElementHandle> implements CancellableTask<CompilationController> {

        private CompilationController cc;
        private final ElementHandle toFind;
        Set<TreePathHandle> usages = new HashSet<>();
        boolean cancelled;

        InvocationScanner(ElementHandle toFind) {
            this.toFind = toFind;
        }

        @Override
        public Tree visitMemberSelect(MemberSelectTree node, ElementHandle p) {
            assert (this.cc != null);
            Element e = p.resolve((CompilationInfo)this.cc);
            this.addIfMatch(this.getCurrentPath(), node, e);
            return (Tree)super.visitMemberSelect(node, p);
        }

        private void addIfMatch(TreePath path, Tree tree, Element elementToFind) {
            if (this.cc.getTreeUtilities().isSynthetic(path)) {
                return;
            }
            Element el = this.cc.getTrees().getElement(path);
            if (el == null) {
                return;
            }
            if (elementToFind.getKind() == ElementKind.METHOD && el.getKind() == ElementKind.METHOD) {
                if (el.equals(elementToFind) || this.cc.getElements().overrides((ExecutableElement)el, (ExecutableElement)elementToFind, (TypeElement)elementToFind.getEnclosingElement())) {
                    this.addUsage(this.getCurrentPath());
                }
            } else if (el.equals(elementToFind)) {
                this.addUsage(this.getCurrentPath());
            }
        }

        void addUsage(TreePath path) {
            this.usages.add(TreePathHandle.create((TreePath)path, (CompilationInfo)this.cc));
        }

        @Override
        public void cancel() {
            this.cancelled = true;
        }

        @Override
        public void run(CompilationController cc) throws Exception {
            if (this.cancelled) {
                return;
            }
            cc.toPhase(JavaSource.Phase.RESOLVED);
            if (this.cancelled) {
                return;
            }
            this.cc = cc;
            try {
                TreePath path = new TreePath(cc.getCompilationUnit());
                this.scan(path, this.toFind);
            } finally {
                this.cc = null;
            }
        }
    }

    private static class MultiVisitorRunner<R, D> implements CancellableTask<CompilationController> {

        private final Map<TreeVisitor<R, D>, R> results = new HashMap<>();
        private final TreeVisitor<R, D>[] visitors;
        private final D arg;
        private final TreePathHandle handle;
        private volatile boolean cancelled;

        /* varargs */ MultiVisitorRunner(TreePathHandle handle, D arg, TreeVisitor<R, D>... visitors) {
            this.visitors = visitors;
            this.handle = handle;
            this.arg = arg;
        }

        R getResult(TreeVisitor visitor) {
            return this.results.get(visitor);
        }

        @Override
        public void cancel() {
            this.cancelled = true;
        }

        @Override
        public void run(CompilationController cc) throws Exception {
            if (this.cancelled) {
                return;
            }
            TreePath path = this.handle.resolve((CompilationInfo)cc);
            TreeScanner<R, D> scanner;
            R result;
            for (TreeVisitor<R, D> v : this.visitors) {
                if (this.cancelled) {
                    return;
                }
                if (v instanceof TreePathScanner) {
                    scanner = (TreePathScanner)v;
                    result = scanner.scan(path, this.arg);
                } else if (v instanceof TreeScanner) {
                    scanner = (TreeScanner)v;
                    result = scanner.scan(path.getLeaf(), this.arg);
                } else {
                    result = path.getLeaf().accept(v, this.arg);
                }
                this.results.put(v, result);
            }
        }
    }

    private static class TreeFinder<T extends Tree> implements CancellableTask<CompilationController> {

        Tree tree;
        private final TreePathHandle handle;
        boolean cancelled;

        TreeFinder(TreePathHandle handle) {
            this.handle = handle;
        }

        @Override
        public void cancel() {
            this.cancelled = true;
        }

        @Override
        public void run(CompilationController cc) throws Exception {
            cc.toPhase(JavaSource.Phase.RESOLVED);
            TreePath path = this.handle.resolve((CompilationInfo)cc);
            assert (path != null);
            this.tree = path.getLeaf();
        }
    }

    private static class TreeFromElementFinder implements CancellableTask<CompilationController> {

        private volatile boolean cancelled;
        Tree tree;
        TreePathHandle handle;
        private final ElementHandle element;

        TreeFromElementFinder(ElementHandle element) {
            this.element = element;
        }

        @Override
        public void cancel() {
            this.cancelled = true;
        }

        @Override
        public void run(CompilationController cc) throws Exception {
            if (this.cancelled) {
                return;
            }
            cc.toPhase(JavaSource.Phase.RESOLVED);
            Element e = this.element.resolve((CompilationInfo)cc);
            this.tree = cc.getTrees().getTree(e);
            assert (this.tree != null);
            if (this.cancelled) {
                return;
            }
            CompilationUnitTree unit = cc.getCompilationUnit();
            TreePath path = TreePath.getPath(unit, this.tree);
            assert (path != null);
            this.handle = TreePathHandle.create((TreePath)path, (CompilationInfo)cc);
        }
    }

    public static abstract class TreePathHandleTask<T> implements CancellableTask<CompilationController> {

        protected boolean cancelled;
        private TreePathHandle handle;
        private T arg;
        private FileObject file;

        @Override
        public void cancel() {
            this.cancelled = true;
        }

        @Override
        public final void run(CompilationController cc) throws Exception {
            cc.toPhase(JavaSource.Phase.RESOLVED);
            this.run(cc, this.handle, this.file, this.arg);
        }

        public abstract void run(CompilationController var1, TreePathHandle var2, FileObject var3, T var4);
    }

    private static final class TreePathResolver implements CancellableTask<CompilationController> {

        private volatile boolean cancelled = false;
        TreePath path;
        private final TreePathHandle handle;

        TreePathResolver(TreePathHandle handle) {
            this.handle = handle;
        }

        @Override
        public void cancel() {
            this.cancelled = true;
        }

        @Override
        public void run(CompilationController cc) throws Exception {
            if (this.cancelled) {
                return;
            }
            this.path = this.handle.resolve((CompilationInfo)cc);
        }
    }

    private Utils() {
    }
}
