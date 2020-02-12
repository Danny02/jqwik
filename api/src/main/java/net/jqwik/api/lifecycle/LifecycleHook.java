package net.jqwik.api.lifecycle;

import java.lang.reflect.*;
import java.util.*;

import org.apiguardian.api.*;

import static org.apiguardian.api.API.Status.*;

/**
 * Experimental feature. Not ready for public usage yet.
 */
@API(status = EXPERIMENTAL, since = "1.0")
public interface LifecycleHook {

	/**
	 * This method is called once per hook and potential element during lifecycle hooks registration.
	 *
	 * @param element The Optional instance contains element for container classes
	 *                or method but is empty for the engine
	 * @return true if a hook shall be applied to this element
	 */
	@API(status = EXPERIMENTAL, since = "1.2.4")
	default boolean appliesTo(Optional<AnnotatedElement> element) {
		return true;
	}

	/**
	 * This method is called when an element, container class or property method,
	 * is found with this hook directly attached or applied to through a parent element.
	 *
	 *  @param context The context can be a {@linkplain ContainerLifecycleContext}
	 *                 or a {@linkplain PropertyLifecycleContext}
	 */
	@API(status = EXPERIMENTAL, since = "1.2.4")
	default void prepareFor(LifecycleContext context) {
	}

	/**
	 * Marker interface. Hook class must implement if you want that all children 
	 * of the annotated element - container, property or engine - should also
	 * have this hook.
	 * 
	 * Application of hooks can further be refined with {@linkplain #appliesTo(Optional)}.
	 *
	 * Experimental feature. Not ready for public usage yet.
	 */
	@API(status = EXPERIMENTAL, since = "1.2.4")
	interface ApplyToChildren {
	}

}
