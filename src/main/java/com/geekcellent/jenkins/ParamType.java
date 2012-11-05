package com.limebrokerage.jenkins;

/**
 * @author Douglas Chimento
 */
public enum ParamType {
	STRING("hudson.model.StringParameterDefinition"),
	BOOL( "hudson.model.BooleanParameterDefinition");

	public final String type;

	ParamType(String type) {
		this.type = type;
	}
}
