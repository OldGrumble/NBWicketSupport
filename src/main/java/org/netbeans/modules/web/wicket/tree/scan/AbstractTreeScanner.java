/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.web.wicket.tree.scan;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.util.TreeScanner;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.wicket.tree.results.TreeScanResultHandler;

/**
 *
 * @author peter
 */
public class AbstractTreeScanner<T> extends TreeScanner<Void, TreeScanResultHandler<T>> {

    protected final CompilationInfo info;

    public AbstractTreeScanner(CompilationInfo info) {
        this.info = info;
    }
}
