package cc.boeters.bikeplanner.util;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QueryUtil {

	private static final Logger LOG = LoggerFactory.getLogger(QueryUtil.class);

	public static String getQuery(String name) {
		InputStream stream = QueryUtil.class.getResourceAsStream(String.format("/query/%s.sql", name));
		StringBuffer buffer = new StringBuffer();
		int c;
		try {
			while ((c = stream.read()) != -1) {
				buffer.append((char) c);
			}
		} catch (IOException e) {
			LOG.error("Error gettting query.", e);
		}
		return buffer.toString();
	}
}