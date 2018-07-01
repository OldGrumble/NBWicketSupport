/*
 * Not ready for public use, so <b>don't use it</b>, yet.
 */
package org.netbeans.spi.wicket;

/**
 *
 * @author Tim Boudreau
 */
import org.netbeans.api.project.Project;

public interface WicketProjectQueryImplementation {

    public abstract boolean isWicket(Project project);
}
