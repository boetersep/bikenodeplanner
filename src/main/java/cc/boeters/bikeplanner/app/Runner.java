package cc.boeters.bikeplanner.app;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.glassfish.jersey.jetty.JettyHttpContainer;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class Runner {

	private static final Logger LOG = LoggerFactory.getLogger(Runner.class);

	private ComboPooledDataSource cpds;

	public static void main(String[] args) throws Exception {
		Options options = createCommandLineOptions();
		try {
			CommandLineParser parser = new DefaultParser();
			CommandLine line = parser.parse(options, args);
			new Runner(line);
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("bikeplanner", "Start bikeplanner", options, "", true);
		}

	}

	private static Options createCommandLineOptions() {
		Options options = new Options();
		options.addOption(Option.builder().required().longOpt("psql-host").hasArg().desc("psql host").build());
		options.addOption(Option.builder().required().longOpt("psql-db").hasArg().desc("psql database").build());
		options.addOption(Option.builder().required().longOpt("psql-user").hasArg().desc("psql user").build());
		options.addOption(Option.builder().required().longOpt("psql-pass").hasArg().desc("psql password").build());
		options.addOption(Option.builder("l").required().longOpt("listen").hasArg().desc("listen at port").build());
		return options;
	}

	public Runner(CommandLine line) throws Exception {
		Runtime.getRuntime().addShutdownHook(new Thread(new Destroyer()));
		cpds = new ComboPooledDataSource();
		cpds.setDriverClass("org.postgresql.Driver"); // loads the jdbc driver
		cpds.setJdbcUrl("jdbc:postgresql://" + line.getOptionValue("psql-host") + "/" + line.getOptionValue("psql-db"));
		cpds.setUser(line.getOptionValue("psql-user"));
		cpds.setPassword(line.getOptionValue("psql-pass"));
		cpds.setAcquireIncrement(5);
		cpds.setMaxPoolSize(20);

		ResourceConfig config = new AppResourceConfig(cpds);
		JettyHttpContainer restHandler = ContainerFactory.createContainer(JettyHttpContainer.class, config);

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { restHandler });
		Server server = new Server(Integer.valueOf(line.getOptionValue("listen")));
		server.setHandler(handlers);
		server.start();
		LOG.info("Bikeplanner app started!");

	}

	class Destroyer implements Runnable {
		public void run() {
			cpds.close();
		}
	}

}