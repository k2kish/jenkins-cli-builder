package com.geekcellent.jenkins;

import com.geekcellent.reporting.framework.BaseApplication;
import com.geekcellent.reporting.framework.options.BaseOptions;
import hudson.cli.CLI;
import hudson.cli.CLIConnectionFactory;
import org.apache.log4j.Logger;
import org.reflections.Reflections;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;

/**
 * @author Douglas Chimento
 */
public class JenkinsManager extends BaseApplication<JenkinsManagerOptions> {

	private static Logger log = Logger.getLogger(JenkinsManager.class);
	private final CLI cli;

	public JenkinsManager(String[] args, Class<? extends BaseOptions> optionsClazz) {
		super(args, optionsClazz, "JenkinsManager");

		CLIConnectionFactory connection = new CLIConnectionFactory();
		CLI tmp = null;
		try {
			log.info("Connecting to " + options.getJenkinsServer());
			connection.basicAuth("brokuser","t0Ps3krit");
			connection.url(options.getJenkinsServer());
			tmp = connection.connect();
		} catch (MalformedURLException e) {
			log.error(e.getMessage(),e);
			System.exit(-1);
		} catch (InterruptedException e) {
			log.error(e.getMessage(),e);
			System.exit(-1);
		} catch (IOException e) {
			log.error(e.getMessage(),e);
			System.exit(-1);
		}
		if (tmp == null) {
			log.error("Cannot create connection to " + options.getJenkinsServer());
			System.exit(-1);
		}

		cli = tmp;

	}

	private void loadJob(Class<? extends BaseOptions> jobClass) {
		File jobConfig;
		OutputStreamWriter writer;
		InputStream in;
		String xmlTemplate = "";
		ProjectType type = jobClass.getAnnotation(Jenkins.class).projectType();

		switch (type) {
		case PRODUCTION:
			if (options.isProduction()) {
				xmlTemplate = JenkinsTemplate.REPORT_PROD.template;
			} else {
				xmlTemplate = JenkinsTemplate.REPORT_QA.template;
			}
			break;
		case QA:
			if (!options.isProduction()) {
				xmlTemplate = JenkinsTemplate.REPORT_QA.template;
			}
			break;
		}

			//Overwrite the configured template
		if (options.getTemplate() != null)
			xmlTemplate = options.getTemplate();

		if (xmlTemplate.isEmpty()) {
			log.info("Not loading " + jobClass.getSimpleName() +
					         ". It is configured for " + type + " but we want jobs" +
					         (options.isProduction() ? "" : " not " )
					         + " in production mode");
			return;
		}

		try {
			jobConfig = File.createTempFile("job", "xml");
			jobConfig.deleteOnExit();

		    LimeJenkinsProject projectBuilder =  new LimeJenkinsProject(jobClass,xmlTemplate,options.isProduction());
		    projectBuilder.setProduction(options.isProduction());

			log.info("Creating job " + projectBuilder.name());
			writer = new OutputStreamWriter(new FileOutputStream(jobConfig));
			writer.write(projectBuilder.getBuildConfig());
			writer.flush();
			writer.close(); //Paranoid

			if (options.isDebug())
				log.info(projectBuilder.getBuildConfig());

			ArrayList<String> commands = new ArrayList<String>();
			in = new BufferedInputStream(new FileInputStream(jobConfig));
			String operation = "create-job";
			commands.add("get-job");
			commands.add(projectBuilder.name());

			//Ignore output from the jenkins channel...hence the /dev/null
			//TODO What if on a windows system?
			if (cli.execute(commands,System.in,new FileOutputStream("/dev/null"),System.err) == 0) {
				//Job Exists so it is an update;
				operation = "update-job";
			}
			commands.clear();
			commands.add(operation);
			commands.add(projectBuilder.name());

			if (cli.execute(commands,in, new FileOutputStream("/dev/null"),System.err) != 0) {
				log.error("Error loading " + projectBuilder.name());
				log.info(projectBuilder.getBuildConfig());
				System.exit(-1);
				return;
			}
			log.info("Successfully updated " + projectBuilder.name());
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			System.exit(-1);
		}
	}

	private List<Class> getClassesToLoad() {

		ArrayList<Class> classes = new ArrayList<Class>();
		try {
			if (options.getJobs() != null) {
				for (String s : options.getJobs()) {
					Class jobClass = null;
					jobClass = Class.forName(s);
					classes.add(jobClass);
				}
				return classes;
			}
		} catch (ClassNotFoundException e) {
			log.error(e.getMessage(),e);
			System.exit(-1);
		}

		for (String path : options.getPackages()) {
			Reflections reflections = new Reflections(path);
			Set<Class<?>> annotated;
			annotated = reflections.getTypesAnnotatedWith(Jenkins.class);
			classes.addAll(annotated);
		}

		return classes;
	}

	public void start() {
		//Load the classes
		List<Class> classes = getClassesToLoad();
		if (classes.isEmpty()) {
			log.error("There are no project to process" );
			System.exit(-1);
		}
		for (Class aClass : classes) {
			loadJob(aClass);
		}

		finish();
	}

	private void finish() {
		try {
			if (cli != null)
				cli.close();
		} catch (IOException e) {

		} catch (InterruptedException e) {

		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		new JenkinsManager(args,JenkinsManagerOptions.class).start();
	}
}
