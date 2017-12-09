package com.ziggy.king.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public abstract class PropertiesManager {

	private static final String PROPERTIES_FILE = "king.properties";

	private Logger logger = Logger.getLogger(this.getClass());

	protected Properties readFile() {
		return readFile(PROPERTIES_FILE);
	}
	
	protected Properties readFile(String file) {
		Properties properties = new Properties();
		try {
			File propFS = new File(file);
			InputStream is = null;
			if (propFS.exists()) {
				// try filesystem properties first (for deployed)
				is = new FileInputStream(propFS);
			} else {
				// if filesystem doesn't have the file, look for default (src/main/resources)
				is = getClass().getClassLoader().getResourceAsStream(file);
			}
			properties.load(is);
			return properties;
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	protected Integer parseInt(String key, String value, String defaultValue, boolean mandatory) throws AficionadoConfigurationException {
		try {
			Integer res = Integer.parseInt(value);
			return res;
		} catch (NumberFormatException nfe) {
			if (mandatory) {
				if (defaultValue != null) {
					try {
						Integer res = Integer.parseInt(defaultValue);
						return res;
					} catch (NumberFormatException nfeIn) {
						throw new AficionadoConfigurationException("Couldn't parse integer for mandatory value '%s' - tried from '%s' (read) and '%s' (default)", key,
								value, defaultValue);
					}
				} else {
					throw new AficionadoConfigurationException("Couldn't parse integer for mandatory value '%s' - tried from '%s' (read). No default value provided.",
							key, value);
				}
			} else {
				try {
					Integer res = Integer.parseInt(defaultValue);
					return res;
				} catch (NumberFormatException nfeIn) {
					return null;
				}
			}
		}
	}

	protected Boolean parseBoolean(String key, String value, String defaultValue, boolean mandatory) throws AficionadoConfigurationException {
		try {
			Boolean res = Boolean.parseBoolean(value);
			return res;
		} catch (NumberFormatException nfe) {
			if (mandatory) {
				if (defaultValue != null) {
					try {
						Boolean res = Boolean.parseBoolean(defaultValue);
						return res;
					} catch (NumberFormatException nfeIn) {
						throw new AficionadoConfigurationException("Couldn't parse boolean for mandatory value '%s' - tried from '%s' (read) and '%s' (default)", key,
								value, defaultValue);
					}
				} else {
					throw new AficionadoConfigurationException("Couldn't parse boolean for mandatory value '%s' - tried from '%s' (read). No default value provided.",
							key, value);
				}
			} else {
				try {
					Boolean res = Boolean.parseBoolean(defaultValue);
					return res;
				} catch (NumberFormatException nfeIn) {
					return null;
				}
			}
		}
	}

	protected Boolean readBoolean(Properties properties, String key, String defaultValue, boolean mandatory) throws AficionadoConfigurationException {
		Boolean value = parseBoolean(key, readValue(properties, key, defaultValue, mandatory), defaultValue, mandatory);
		logger.info(String.format("Successfully parsed boolean value for property '%s': %s", key, value.toString()));
		return value;
	}

	protected Integer readInt(Properties properties, String key, String defaultValue, boolean mandatory) throws AficionadoConfigurationException {
		Integer value = parseInt(key, readValue(properties, key, defaultValue, mandatory), defaultValue, mandatory);
		logger.info(String.format("Successfully parsed integer value for property '%s': %d", key, value));
		return value;
	}

	protected String readValue(Properties properties, String key, String defaultValue, boolean mandatory) throws AficionadoConfigurationException {
		String value = properties.getProperty(key);
		if (value != null) {
			logger.info(String.format("Successfully read value for property '%s': %s", key, value));
			return value;
		} else {
			if (mandatory) {
				if (defaultValue != null) {
					logger.info(String.format("Couldn't read value for property '%s'. Returning default: %s", key, defaultValue));
					return defaultValue;
				} else {
					throw new AficionadoConfigurationException("Couldn't read mandatory property '%s' and no default value was given.", key);
				}
			} else {
				logger.info(String.format("Couldn't read value for property '%s'. Returning default: %s", key, defaultValue));
				return defaultValue;
			}
		}
	}

	@SuppressWarnings("serial")
	public class AficionadoConfigurationException extends Exception {
		public AficionadoConfigurationException(String message, Object... params) {
			super(String.format(message, params));
		}
	}

}
