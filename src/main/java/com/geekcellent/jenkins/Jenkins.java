package com.geekcellent.jenkins;

import com.geekcellent.reporting.framework.BaseApplication;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is an annotation to build Jenkins jobs/projects
 *
 * @author Douglas Chimento
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Jenkins {

	/**
	 *
	 * @return String
	 */
	String name();

	/**
	 *
	 * @return
	 */
	Class<? extends BaseApplication> mainClass();

	/**
	 *
	 * @return
	 */
	String description();

	boolean concurrent() default true;

	/**
	 *
	 * @return {@link boolean}
	 */
	boolean disableQa() default true;

	/**
	 *
	 * @return
	 */
	String[] ircChannel() default "#bo";

	/**
	 *
	 * @return
	 */
	String email() default "prodeng@limebrokerage.com";

	String javaOpts() default "";

	String schedule() default "";

	JenkinsPools pool() default JenkinsPools.REPORT;

	ProjectType projectType() default ProjectType.QA;

}
