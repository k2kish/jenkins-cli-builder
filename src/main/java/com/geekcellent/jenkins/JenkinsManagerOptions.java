package com.limebrokerage.jenkins;

import com.lexicalscope.jewel.cli.Option;
import com.limebrokerage.reporting.framework.options.BaseOptions;

import java.util.List;

/**
 * @author Douglas Chimento
 */
public interface JenkinsManagerOptions extends BaseOptions {

	@Option(description = "The list of classes that will become jenkins jobs",
	        defaultToNull = true, longName = {"jobs", "job"})
	List<String> getJobs();

	@Option(description = "The url of the jenkins server")
	String getJenkinsServer();

	@Option(defaultToNull = true,
	        description = "The file name of the template to use. Note this file should be in src/main/resources. Possible values: bo-report-template-qa.xml or bo-report-template-qa.xml")
	String getTemplate();

	@Option(description = "This is a production deployment. If set to false it would cause jobs to be disabled")
	boolean isProduction();

	@Option(description = "A package path to search for classes that have been annotated with @Jenkins", defaultValue = "com.limebrokerage")
	List<String> getPackages();

  }
