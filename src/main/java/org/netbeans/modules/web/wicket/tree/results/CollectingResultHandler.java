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
package org.netbeans.modules.web.wicket.tree.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

/**
 *
 * @author Peter Nabbefeld
 */
public class CollectingResultHandler<T, C extends Collection<T>> implements TreeScanResultHandler<T> {

    private final Class<C> clazz;
    private final C resultCollection;

    public CollectingResultHandler(Class<C> clazz) {
        this.clazz = clazz;
        resultCollection = createCollectionImpl(clazz);
    }

    public CollectingResultHandler(Collection<T> collection) {
        this.clazz = (Class<C>)collection.getClass();
        resultCollection = (C)collection;
    }

    /**
     * Add some intermediary result to the collection.
     *
     * @param result The intermediary result.
     */
    @Override
    public void handleResult(T result) {
        resultCollection.add(result);
    }

    /**
     * Get the result collection.
     *
     * @return The result collection.
     */
    public C getResult() {
        return clazz.cast(Collections.unmodifiableCollection(resultCollection));
    }

    private C createCollectionImpl(Class<C> clazz) {
        switch (clazz.getName()) {
            case "java.util.List":
                return clazz.cast(new ArrayList<>());
            case "java.util.Set":
                return clazz.cast(new TreeSet<>());
            default:
                throw new IllegalArgumentException("The collection type " + clazz.getName() + " is not supported");
        }
    }
}
