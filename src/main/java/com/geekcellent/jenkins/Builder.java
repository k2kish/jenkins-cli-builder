package com.limebrokerage.jenkins;

/**
 * This is the interface which drives project creation
 * It is broken up in components similar to web layout
 * @author Douglas Chimento
 */
public interface Builder extends Jenkins {

	String getDescription();

	String getParameters();

	String getPool();

	boolean isDisabled();

	boolean isConcurrent();

	String getIrc();

	String getCrontab();

	String getBuildCommand();

	String getEmails();

	String getBuildConfig();
	
}
