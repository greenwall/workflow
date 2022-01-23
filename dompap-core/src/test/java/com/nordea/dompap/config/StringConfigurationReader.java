package com.nordea.dompap.config;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

public class StringConfigurationReader implements ConfigurationReader {
	
	private final Properties props;
	
	public StringConfigurationReader(String props) throws IOException {
		this.props = new Properties();
		this.props.load(new StringReader(props));
	}
	@Override
	public String getStringProperty(String key) {
		return props.getProperty(key);
	}

}
