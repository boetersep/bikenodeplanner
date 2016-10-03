package cc.boeters.bikeplanner.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import cc.boeters.bikeplanner.util.QueryUtil;

public class BikeNodeService {

	/**
	 * No route found exception message for astar_bbox.
	 */
	private static final String ERROR_VERTEX_WAS_NOT_FOUND = "vertex was not found.";

	private static final Logger LOG = LoggerFactory.getLogger(BikeNodeService.class);

	private final String nodesQuery = QueryUtil.getQuery("bikenodebbox");
	private final String routeQuery = QueryUtil.getQuery("astar_bbox");
	private final Double[] bboxRadiusAttempts = { 0.1, 1.0, 2.0, 5.0 };

	private ComboPooledDataSource datasource;

	public BikeNodeService(ComboPooledDataSource cpds) {
		this.datasource = cpds;
	}

	public String getNodes(Double left, Double bottom, Double right, Double top) {
		try (Connection connection = datasource.getConnection()) {
			PreparedStatement prepareStatement = connection.prepareStatement(nodesQuery);
			prepareStatement.setDouble(1, left);
			prepareStatement.setDouble(2, bottom);
			prepareStatement.setDouble(3, right);
			prepareStatement.setDouble(4, top);
			long start = System.currentTimeMillis();
			ResultSet executeQuery = prepareStatement.executeQuery();
			if (executeQuery.next()) {
				String result = executeQuery.getString(1);
				executeQuery.close();
				prepareStatement.close();
				connection.close();
				long stop = System.currentTimeMillis();
				LOG.info("Nodes query took {} ms.", stop - start);
				return result;
			}
		} catch (SQLException e) {
			LOG.error("SQL error", e);
		}
		return null;
	}

	public String getRoute(Long from, Long to) {
		try (Connection connection = datasource.getConnection()) {
			for (Double bboxRadius : bboxRadiusAttempts) {
				LOG.info("Going to execute route (from {}, to {}) query with bbox radius value {}.", from, to,
						bboxRadius);
				String route = attemptRoute(from, to, bboxRadius, connection);
				if (route != null) {
					LOG.info("Found route for from {}, to {}.", from, to);
					connection.close();
					return route;
				}
			}
			LOG.error("No route found: from {}, to {}.", from, to);
			connection.close();
		} catch (SQLException e) {
			LOG.error("SQL error", e);
		}
		return null;
	}

	private String attemptRoute(Long from, Long to, Double bboxRadius, Connection connection) throws SQLException {
		try {
			PreparedStatement prepareStatement = connection.prepareStatement(routeQuery);
			prepareStatement.setDouble(1, bboxRadius);
			prepareStatement.setLong(2, from);
			prepareStatement.setLong(3, to);
			prepareStatement.setLong(4, from);
			prepareStatement.setLong(5, to);
			long start = System.currentTimeMillis();
			ResultSet executeQuery = prepareStatement.executeQuery();
			if (executeQuery.next()) {
				String result = executeQuery.getString(1);
				executeQuery.close();
				prepareStatement.close();
				long stop = System.currentTimeMillis();
				LOG.info("Route query took {} ms.", stop - start);
				return result;
			}
		} catch (SQLException e) {
			if (e.getMessage().endsWith(ERROR_VERTEX_WAS_NOT_FOUND)) {
				// Lets try with a larger bbox.
				return null;
			}
			throw e;
		}
		return null;
	}

}
