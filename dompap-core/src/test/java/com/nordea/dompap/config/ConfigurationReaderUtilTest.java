package com.nordea.dompap.config;

import org.junit.Test;

import javax.resource.ResourceException;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ConfigurationReaderUtilTest {

	@Test
	public void getPropertyForClassNameTest() throws IOException, ResourceException {
		{
			String props = 
				"prefix.com.nordea.dompap.config.ConfigurationReaderUtilTest=1\n"
				+"prefix.com.nordea.dompap.dpap.config=2\n"
				+"prefix.com.nordea.dompap.dpap=3\n"
				+"prefix.com.nordea.dompap=4\n"
				+"prefix.com.nordea=5\n"
				+"prefix.com=6\n"
				+"prefix=7\n";
		
			assertEquals("1", ConfigurationReaderUtil.getPropertyForClassName(createConfig(props), "prefix", this.getClass().getName()));
		}

		{
			String props = 
				"prefix.com.nordea.dompap.config=2\n"
				+"prefix.com.nordea.dompap=3\n"
				+"prefix.com.nordea.dompap=4\n"
				+"prefix.com.nordea=5\n"
				+"prefix.com=6\n"
				+"prefix=7\n";
		
			assertEquals("2", ConfigurationReaderUtil.getPropertyForClassName(createConfig(props), "prefix", this.getClass().getName()));
		}

		{
			String props = 
				"prefix.com.nordea.dompap=3\n"
				+"prefix.com.nordea=5\n"
				+"prefix.com=6\n"
				+"prefix=7\n";
		
			assertEquals("3", ConfigurationReaderUtil.getPropertyForClassName(createConfig(props), "prefix", this.getClass().getName()));
		}
		
		{
			String props = 
				"prefix.com.nordea.dompap=4\n"
				+"prefix.com.nordea=5\n"
				+"prefix.com=6\n"
				+"prefix=7\n";
		
			assertEquals("4", ConfigurationReaderUtil.getPropertyForClassName(createConfig(props), "prefix", this.getClass().getName()));
		}

		{
			String props = 
				"prefix.com.nordea=5\n"
				+"prefix.com=6\n"
				+"prefix=7\n";
		
			assertEquals("5", ConfigurationReaderUtil.getPropertyForClassName(createConfig(props), "prefix", this.getClass().getName()));
		}

		{
			String props = 
				"prefix.com=6\n"
				+"prefix=7\n";
		
			assertEquals("6", ConfigurationReaderUtil.getPropertyForClassName(createConfig(props), "prefix", this.getClass().getName()));
		}

		{
			String props = 
				"prefix=7\n";
		
			assertEquals("7", ConfigurationReaderUtil.getPropertyForClassName(createConfig(props), "prefix", this.getClass().getName()));
		}
		
		{
			String props = 
				"";
		
			assertEquals(null, ConfigurationReaderUtil.getPropertyForClassName(createConfig(props), "prefix", this.getClass().getName()));
		}
		
	}

	@Test
	public void getListTest() throws IOException, ResourceException {
		{
			{
				String props = "";
			
				List<String> list = ConfigurationReaderUtil.getListProperty(createConfig(props), "list", "|");
				assertEquals(0, list.size());
			}
			{
				String props = 
					"list=|a.b.C|a.b.D||a.b.E|";
			
				List<String> list = ConfigurationReaderUtil.getListProperty(createConfig(props), "list", "|");
				assertEquals(3, list.size());
				assertEquals("a.b.C", list.get(0));
				assertEquals("a.b.D", list.get(1));
				assertEquals("a.b.E", list.get(2));
			}
		
			{
				String props = 
					"list=|$0|$1||$2|\n"
					+"list.0=a.b.C\n"
					+"list.1=a.b.D\n"
					+"list.2=a.b.E\n";
			
				List<String> list = ConfigurationReaderUtil.getListProperty(createConfig(props), "list", "|");
				assertEquals(3, list.size());
				assertEquals("a.b.C", list.get(0));
				assertEquals("a.b.D", list.get(1));
				assertEquals("a.b.E", list.get(2));
			}

			{
				String props = 
					"list=|$0|$1|$5|$6||a.b.E|\n"
					+"list.0=a.b.C\n"
					+"list.1=a.b.D\n"
					+"list.5=  \n";
			
				List<String> list = ConfigurationReaderUtil.getListProperty(createConfig(props), "list", "|");
				assertEquals(3, list.size());
				assertEquals("a.b.C", list.get(0));
				assertEquals("a.b.D", list.get(1));
				assertEquals("a.b.E", list.get(2));
			}
			
			{
				String props = 
					"list=..15\n"
					+"list.0=a.b.C\n"
					+"list.12=a.b.D\n"
					+"list.15=a.b.E\n"
					+"list.16=a.b.F\n";
			
				List<String> list = ConfigurationReaderUtil.getListProperty(createConfig(props), "list", "|");
				assertEquals(3, list.size());
				assertEquals("a.b.C", list.get(0));
				assertEquals("a.b.D", list.get(1));
				assertEquals("a.b.E", list.get(2));
			}

			{
				String props = 
					"list=..15\n"
					+"list.0=a.b.C\n"
					+"list.5=\n"
					+"list.12=a.b.D\n"
					+"list.13=  \n"
					+"list.15=a.b.E\n"
					+"list.16=a.b.F\n";
			
				List<String> list = ConfigurationReaderUtil.getListProperty(createConfig(props), "list", "|");
				assertEquals(3, list.size());
				assertEquals("a.b.C", list.get(0));
				assertEquals("a.b.D", list.get(1));
				assertEquals("a.b.E", list.get(2));
			}
			
		}
	}

	@Test
	public void getListOneByOneTest() throws IOException, ResourceException {
		String props = "list.0=a.b.C\n"
				+ "list.1=a.b.D\n"
				+ "list.2=a.b.E\n"
				+ "list.4=a.b.G\n";

		List<String> list = ConfigurationReaderUtil.getListProperty(createConfig(props), "list", null);
		assertEquals(3, list.size());
		assertEquals("a.b.C", list.get(0));
		assertEquals("a.b.D", list.get(1));
		assertEquals("a.b.E", list.get(2));
	}
	
	private ConfigurationReader createConfig(String props) throws IOException {
		return new StringConfigurationReader(props);
	}
	
}
