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
package org.netbeans.modules.web.wicket.palette;

import java.io.IOException;
import javax.swing.Action;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.netbeans.spi.palette.PaletteFilter;
import org.openide.loaders.DataFolder;
import org.openide.nodes.FilterNode;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author Peter Nabbefeld
 */
public class WicketHtmlPaletteFactory {

    private static PaletteController palette = null;

    @MimeRegistration(mimeType = "text/html", service = PaletteController.class)
    public static PaletteController createPalette() {
        try {
            if (palette == null) {
                palette = PaletteFactory.createPalette(
                        // Palette folder:
                        "HTMLPalette",
                        // Palette actions:
                        createPaletteActions(),
                        // Palette filter, may be null:
                        createPaletteFilter(),
                        //Drag and Drop handler:
                        createDragAndDropHandler()
                );
            }
            return palette;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private static PaletteActions createPaletteActions() {
        return new PaletteActions() {
            @Override
            public Action[] getImportActions() {
                return null;
            }

            @Override
            public Action[] getCustomPaletteActions() {
                return null;
            }

            @Override
            public Action[] getCustomCategoryActions(Lookup lkp) {
                return null;
            }

            @Override
            public Action[] getCustomItemActions(Lookup lkp) {
                return null;
            }

            @Override
            public Action getPreferredAction(Lookup lkp) {
                return null;
            }
        };
    }

    // Currently only used for debugging
    private static PaletteFilter createPaletteFilter() {
        return new PaletteFilter() {
            @Override
            public boolean isValidCategory(Lookup lkp) {
                DataFolder.FolderNode folderNode = lkp.lookup(DataFolder.FolderNode.class);
                if (folderNode != null) {
//                    System.out.println("FolderNode: " + folderNode.getDisplayName());
                }
                return true;
            }

            @Override
            public boolean isValidItem(Lookup lkp) {
                FilterNode itemNode = lkp.lookup(FilterNode.class);
                if (itemNode != null) {
//                    System.out.println("FolderNode: " + itemNode.getDisplayName());
                }
                return true;
            }
        };
    }

    private static DragAndDropHandler createDragAndDropHandler() {
        return new DragAndDropHandler(true) {
            @Override
            public void customize(ExTransferable et, Lookup lkp) {
            }
        };
    }
}
