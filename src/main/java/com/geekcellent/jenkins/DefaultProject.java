package com.limebrokerage.jenkins;


import com.limebrokerage.reporting.framework.BaseApplication;
import com.limebrokerage.reporting.framework.options.BaseOptions;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.ClassLoader.getSystemResourceAsStream;


/**
 * @author Douglas Chimento
 */
public class DefaultProject implements Builder {
	final Jenkins jenkinsProperties;
	private List<JobParams> jobParamList = new ArrayList<JobParams>();
	private boolean disabled;
	private String template;
	private boolean production = false;
	public final Class<? extends BaseOptions> options;

	/**
	 *
	 * @param appOptions
	 */
	public DefaultProject(Class<? extends BaseOptions> appOptions,String template, boolean production) throws IOException {
		this.jenkinsProperties = appOptions.getAnnotation(Jenkins.class);
		this.options = appOptions;
		readTemplate(template);

	    for (Method method : appOptions.getMethods()) {
			jobParamList.add(new JobParams(method, production ? ProjectType.PRODUCTION : ProjectType.QA));
		}
		Collections.sort(jobParamList);
		disabled = disableQa();
	}

	private void readTemplate(String template) throws IOException {
		InputStream in = new DataInputStream(getSystemResourceAsStream(template));
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		String tmp = "";
		while((line = reader.readLine()) != null) {
			tmp += line;
		}
		this.template = tmp;
	}

	/**
	 * Remove any chars that conflict with the xml standard of Jenkins
	 * @param value
	 * @return
	 */
	public static String escape(String value) {
		return value.replaceAll("\\&","&amp;");
	}

	@Override
	public String getBuildConfig() {
		return template.replace("{{DESCRIPTION}}",getDescription()).
				replace("{{PARAMETERS}}",getParameters()).
				replace("{{POOL}}",getPool()).
				replace("{{DISABLED}}",Boolean.toString(disabled)).
				replace("{{CONCURRENT}}",Boolean.toString(concurrent())).
				replace("{{CRONTAB}}",getCrontab()).
				replace("{{COMMAND}}",getBuildCommand()).
				replace("{{EMAIL}}",getEmails()).
				replace("{{IRC}}",getIrc());
	}

	@Override
	public String getDescription() {
		return description();
	}

	@Override
	public String getPool() {
		return pool().pool;
	}

	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public boolean isConcurrent() {
		return concurrent();
	}

	@Override
	public String getIrc() {
		String irc = "";
		for (String channel : ircChannel()) {
			irc += 	"<hudson.plugins.im.GroupChatIMMessageTarget>\n" +
					"<name>" + channel + "</name>\n" +
					"<notificationOnly>false</notificationOnly>\n" +
					"</hudson.plugins.im.GroupChatIMMessageTarget>\n\n";
		}
		return irc;
	}

	@Override
	public String getCrontab() {
		return schedule();
	}

	@Override
	public String getBuildCommand() {
		String script = "";
		for (JobParams jobOption : jobParamList) {
			if (jobOption.isHidden())
				continue;

			String param = jobOption.getJenkinsName();
			String cliName = jobOption.getCliName();

			if (jobOption.getType() == ParamType.BOOL) {
				script += "if [ &quot;$" + param + "&quot; == &quot;true&quot; ]; then\n";
				script += param + "=&quot;--" + cliName + "&quot;\n";
				script += "else\n" + param + "=&quot;&quot;";
				script += "\nfi\n\n";
			} else {
				script += "if [ -n &quot;$" + param + "&quot; ]; then\n";
				script += param + "=&quot;--" + cliName + " $" + param + "&quot;\n";
				script += "else\n" + param + "=&quot;&quot;";
				script += "\nfi\n\n";
			}
		}

		script += "TMPDIR=`mktemp -d`\n";
		//To prevent the jar from being overwritten
		script += "cp $WORKSPACE/reporting.jar $TMPDIR\n";
		String args = "";
		for (JobParams jobOption : jobParamList) {
			if (jobOption.isHidden())
				continue;
			args += "$" +  jobOption.getJenkinsName() + " " ;
		}
		script += "java -cp $TMPDIR/reporting.jar " + mainClass().getName() +
				" " + args + " &amp;&amp;  \n";
		script += "rm -rf $TMPDIR\n";
		return script;
	}

	@Override
	public String getEmails() {
		return email();
	}

	@Override
	public String getParameters() {
		String params =  "";

		for (JobParams jobOption : jobParamList) {
			if (jobOption.isHidden())
				continue;

			params += "<" + jobOption.getType().type + ">";
			params +=  "<name>" + jobOption.getJenkinsName() + "</name>";
			params += "<description>" + jobOption.o.description() + "</description>";
			params += "<defaultValue>" + jobOption.getDefaultValue() + "</defaultValue>";
			params += "</" + jobOption.getType().type + ">\n";
		}
		return params;
	}


	@Override
	public Class<? extends Annotation> annotationType() {
		return null;
	}

	@Override
	public ProjectType projectType() {
		return jenkinsProperties.projectType();
	}

	@Override
	public boolean disableQa() {
		return jenkinsProperties.disableQa();
	}

	@Override
	public JenkinsPools pool() {
		return jenkinsProperties.pool();
	}

	@Override
	public String schedule() {
		return jenkinsProperties.schedule();
	}

	@Override
	public String javaOpts() {
		return jenkinsProperties.javaOpts();
	}

	@Override
	public String email() {
		return jenkinsProperties.email();
	}

	@Override
	public String[] ircChannel() {
		return jenkinsProperties.ircChannel();
	}


	@Override
	public String description() {
		return jenkinsProperties.description();
	}

	@Override
	public Class<? extends BaseApplication> mainClass() {
		return jenkinsProperties.mainClass();
	}

	@Override
	public String name() {
		return jenkinsProperties.name();
	}

	@Override
	public boolean concurrent() {
		return jenkinsProperties.concurrent();
	}

	@Override
	public String toString() {
		return "DefaultProject{" +
				"jenkinsProperties=" + jenkinsProperties +
				'}';
	}

	public void setProduction(boolean prod) {
		production = prod;
	}
}
