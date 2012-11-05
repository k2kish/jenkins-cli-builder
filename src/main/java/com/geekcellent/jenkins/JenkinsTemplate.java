package com.limebrokerage.jenkins;

/**
 * @author Douglas Chimento
 */
public enum JenkinsTemplate {
	REPORT_PROD("bo-report-template.xml"),
	REPORT_QA(  "bo-report-template-qa.xml");

	public final String template;

	private JenkinsTemplate(String template) {
		this.template = template;
	}
}
