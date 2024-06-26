/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.impl.console;

import java.util.Collection;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.console.DevConsole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DefaultDevConsolesLoaderTest extends ContextTestSupport {

    @Test
    public void testLoader() {
        DefaultDevConsolesLoader loader = new DefaultDevConsolesLoader(context);
        Collection<DevConsole> col = loader.loadDevConsoles();
        Assertions.assertTrue(col.size() > 3);
    }

    @Test
    public void testLoaderForce() {
        DefaultDevConsolesLoader loader = new DefaultDevConsolesLoader(context);
        Collection<DevConsole> col = loader.loadDevConsoles(true);
        Assertions.assertTrue(col.size() > 3);
    }

}
