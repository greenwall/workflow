package com.nordea.dompap.workflow;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark the final state of a workflow and optionally defining after how many days it should be archived (default 30). 
 * @author G93283
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FinalState {
    int archiveAfterDays() default 30;
}
