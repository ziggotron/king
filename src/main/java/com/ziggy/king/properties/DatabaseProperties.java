package com.ziggy.king.properties;

import java.util.Properties;

import org.apache.log4j.Logger;

public class DatabaseProperties extends PropertiesManager {
	private static DatabaseProperties instance;

	private Logger logger = Logger.getLogger(this.getClass());

	private String databaseDriver;
	private String databaseUrl;
	private String databaseUsername;
	private String databasePassword;
	private String showSQL;

	protected DatabaseProperties() {
		Properties properties = readFile();
		if (properties != null) {
			try {
				databaseDriver = readValue(properties, "DBDriver", null, true);
				databaseUrl = readValue(properties, "DBUrl", null, true);
				databaseUsername = readValue(properties, "DBUsername", null, true);
				databasePassword = readValue(properties, "DBPassword", null, true);
				showSQL = readValue(properties, "DBShowSQL", "false", true);
			} catch (AficionadoConfigurationException e) {
				logger.error(String.format("Error reading properties: '%s'", e.getMessage()));
				System.exit(1);
			}
		} else {
			logger.error(String.format("Couldn't read properties (file missing)"));
			System.exit(1);
		}
	}

	public static synchronized DatabaseProperties getProperties() {
		if (instance == null) {
			instance = new DatabaseProperties();
		}
		return instance;
	}

	public String getDatabaseDriver() {
		return databaseDriver;
	}

	public String getDatabaseUrl() {
		return databaseUrl;
	}

	public String getDatabaseUsername() {
		return databaseUsername;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public String getShowSQL() {
		return showSQL;
	}

}

