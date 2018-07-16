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
package org.netbeans.modules.web.wicket.palette.util;

import java.util.Set;

/**
 *
 * @author Peter Nabbefeld
 */
public class NewMethodProperties {

    private final String methodName;
    private final String returnType;
    private String bodyContent;
    private Set<String> requiredImports;

    public NewMethodProperties(String methodName, String returnType) {
        this.methodName = methodName;
        this.returnType = returnType;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setBodyContent(String bodyContent) {
        this.bodyContent = bodyContent;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public void setRequiredImports(Set<String> requiredImports) {
        this.requiredImports = requiredImports;
    }

    public Set<String> getRequiredImports() {
        return requiredImports;
    }

    String getBody() {
        return "{" + bodyContent + "}";
    }

    boolean hasRequiredImports() {
        return requiredImports != null && !requiredImports.isEmpty();
    }
}
