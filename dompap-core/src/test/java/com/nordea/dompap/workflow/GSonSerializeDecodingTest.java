package com.nordea.dompap.workflow;

import com.nordea.dompap.workflow.content.GsonWorkFlowContentSerializer;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GSonSerializeDecodingTest {

	private static class MySerializer extends GsonWorkFlowContentSerializer {
		@Override
		public String decodeBytes(byte[] bytes) {
			return super.decodeBytes(bytes);
		}
	}
	
	@Test
	public void testUnknownToUTF8() {
		MySerializer ms = new MySerializer();
		String sourceString = "The string ÆØÅæøåöÖäÄ £$";
		String decodedString = ms.decodeBytes(sourceString.getBytes(StandardCharsets.ISO_8859_1));
		assertEquals(sourceString, decodedString, "Not equals");
	}
	
	@Test
	public void testUTF8ToUTF8() {
		MySerializer ms = new MySerializer();
		String sourceString = "The string ÆØÅæøåöÖäÄ £$€";
		String decodedString = ms.decodeBytes(sourceString.getBytes(StandardCharsets.UTF_8));
		assertEquals(sourceString, decodedString, "Not equals");
	}
	
	@Test
	public void testCP1252ToUTF8() {
		MySerializer ms = new MySerializer();
		try {
			String sourceString = "The string ÆØÅæøåöÖäÄ £$€";
			String decodedString = ms.decodeBytes(sourceString.getBytes("CP1252"));
			assertEquals(sourceString, decodedString, "Not equals");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testWindows1252ToUTF8() {
		MySerializer ms = new MySerializer();
		try {
			String sourceString = "The string ÆØÅæøåöÖäÄ £$€";
			String decodedString = ms.decodeBytes(sourceString.getBytes("windows-1252"));
			assertEquals(sourceString, decodedString, "Not equals");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDBBytesToUTF8() {
		MySerializer ms = new MySerializer();
		byte[] bytes = new byte[]{(byte)82,(byte)229,(byte)100,(byte)104,(byte)117};
		String decodedString = ms.decodeBytes(bytes);
		assertEquals("Rådhu", decodedString, "Not equals");
	}
}
