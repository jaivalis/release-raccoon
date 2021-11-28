package com.raccoon.common;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

/**
 * According to JaCoCo documentation an annotation with `Generated` in its name and the following
 * inherited annotations will mark a class or method as ignored from coverage reports.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({TYPE, METHOD})
public @interface ExcludeFromJacocoGeneratedReport {}
