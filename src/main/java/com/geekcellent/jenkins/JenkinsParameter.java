package com.geekcellent.jenkins;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to control run time params in Jenkins
 * @author Douglas Chimento
 */
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
public @interface JenkinsParameter  {
	String value() default "";
	String qaValue() default "";
	boolean hide() default false;
}
