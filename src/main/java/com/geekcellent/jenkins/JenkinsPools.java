package com.geekcellent.jenkins;

/**
 * @author Douglas Chimento
 */
public enum JenkinsPools {
	REPORT("ReportPool"),
	TRANSFER("TransferPool"),
	OATS("OatsPool");

	public final String pool;

	private JenkinsPools(String pool) {
		this.pool = pool;
	}

}
