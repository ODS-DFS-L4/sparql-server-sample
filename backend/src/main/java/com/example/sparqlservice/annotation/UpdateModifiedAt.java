package com.example.sparqlservice.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks repository methods to update the modified timestamp in the RDF store.
 * Handled by {@link com.example.sparqlservice.aop.UpdateModifiedAspect#updateModifiedAt}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UpdateModifiedAt {
}
