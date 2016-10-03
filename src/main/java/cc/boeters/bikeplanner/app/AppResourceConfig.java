package cc.boeters.bikeplanner.app;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import cc.boeters.bikeplanner.service.BikeNodeService;

public class AppResourceConfig extends ResourceConfig {

	public AppResourceConfig(ComboPooledDataSource cpds) {
		register(new AppBinder(cpds));
		register(JacksonFeature.class);
		packages(true, "cc.boeters");
	}

	public class AppBinder extends AbstractBinder {

		private final ComboPooledDataSource cpds;
		private final BikeNodeService bikeNodeService;

		public AppBinder(ComboPooledDataSource cpds) {
			this.cpds = cpds;
			this.bikeNodeService = new BikeNodeService(cpds);
		}

		@Override
		protected void configure() {
			bind(bikeNodeService).to(BikeNodeService.class);
		}
	}
}