package com.geekcellent.jenkins;

import com.lexicalscope.jewel.cli.Option;

import java.lang.reflect.Method;
import java.util.Arrays;

import static com.geekcellent.jenkins.DefaultProject.escape;
import static com.google.common.base.CaseFormat.*;

/**
 * @author Douglas Chimento
 */
public class JobParams implements Comparable<JobParams> {

	public final Option o;
	public final JenkinsParameter j;
	public final Method m;
	public final ProjectType type;

	JobParams(Method m, ProjectType type) {
		this.m = m;
		this.o = m.getAnnotation(Option.class);
		this.j = m.getAnnotation(JenkinsParameter.class);
		this.type = type;
	}

	/**
	 * Converts an {@link Method} name to an UPPER_UNDERSCORE string
	 * <code>
	 *   getDatabase() -> DATABASE
	 *   getMaxThreads -> MAX_THREADS
	 *   isDebug -> DEBUG
	 * </code>
	 * @param name
	 * @return
	 */
	public static String methodToJenkinsParam(String name) {
		 return LOWER_CAMEL.to(UPPER_UNDERSCORE,
		                      name.replaceFirst("^is", "").
				                      replaceFirst("^get",""));
	}

	/**
	 * Converts an {@link Method} name to an UPPER_UNDERSCORE string
	 * <code>
	 *   getDatabase() -> --database
	 *   getMaxThreads -> --maxthreads
	 *   isDebug -> --debug
	 * </code>
	 * @param name
	 * @return
	 */
	public static String methodToCommandLine(String name) {
		//getDatebase() -> --database
		//isDebug() -> --debug
		String n =  name.replaceFirst("^is","").
				replaceFirst("^get","");
		return n.substring(0,1).toLowerCase() + n.substring(1);
	}

	public String getJenkinsName() {
		return methodToJenkinsParam(m.getName());
	}

	public String getCliName() {
		return methodToCommandLine(m.getName());
	}

	public boolean isHidden() {
		return o.hidden() ||  ( j != null ? j.hide() : false);
	}

	public String getDefaultValue() {
		
		if (j != null) {
			String jenkinsValue;
			if (type == ProjectType.QA) {
				jenkinsValue = !j.qaValue().isEmpty() ? j.qaValue() : j.value();
			} else {
				jenkinsValue = j.value();
			}
			if (jenkinsValue == null || jenkinsValue.isEmpty())
				jenkinsValue = "";
			
			return escape(jenkinsValue);
		}

		if (getType() == ParamType.BOOL && o.defaultToNull()) {
			return "false";
		}

		if (o.defaultToNull())
			return "";

		String retVal = "";
		for (int i = 0; i < o.defaultValue().length; i++) {
			String s = o.defaultValue()[i];
			if (i == o.defaultValue().length -1)
				retVal += s;
			else
				retVal += s + ",";
		}
		return escape(retVal);
	}

	public ParamType getType() {
		return  (m.getReturnType().toString().equals("boolean") ? ParamType.BOOL : ParamType.STRING);
	}

	@Override
	public int compareTo(JobParams o) {
		return getJenkinsName().compareTo(o.getJenkinsName());
	}
}
