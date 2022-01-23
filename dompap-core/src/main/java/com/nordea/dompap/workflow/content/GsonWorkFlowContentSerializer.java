package com.nordea.dompap.workflow.content;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.nordea.next.dompap.domain.type.CustomerNumber;
import com.nordea.next.dompap.domain.type.NationalCompanyId;
import com.nordea.next.dompap.domain.type.NationalPersonalId;
import com.nordea.next.dompap.domain.type.NetBankCustomerId;
import com.nordea.next.dompap.workflow.GsonContent;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 * Serializes to and from JSON.
 * Loading and storing of serialized byte array is from table WFLW_WORKFLOW_CONTENT (ID, CONTENT).  
 */
@Slf4j
public class GsonWorkFlowContentSerializer extends DefaultWorkFlowContentSerializer {
//	private static final String FALLBACK_CODEPAGE_CONFIG_KEY = "dompap.json.deserialize.fallback.codepage";
	private static final String CODEPAGE = "UTF-8";
	private static final String FALLBACK_CODEPAGE = "windows-1252";

	@Override
	public <T> byte[] serialize(T workflowContent) throws IOException {
    	Gson gson = getGson();
    	String json = gson.toJson(workflowContent);
    	return json.getBytes(CODEPAGE);
    }

	public <T> T deserialize(byte[] bytes, Class<T> contentType) throws IOException, ClassNotFoundException {
    	String json = decodeBytes(bytes);
    	if (contentType == GsonContent.class) {
        	Gson gson = new GsonBuilder().setPrettyPrinting().create();
    		T obj = gson.fromJson(json, contentType);
    		((GsonContent)obj).setContent(gson.toJson(new JsonParser().parse(json)));
    		return obj;
    	}
    	Gson gson = getGson();
    	return gson.fromJson(json, contentType);
    }    
	
	protected String decodeBytes(byte[] bytes) {
		log.info("Default charset: " + Charset.defaultCharset());
		String charSet = FALLBACK_CODEPAGE;

		// TODO Need for configurability ?
/*
		try {
			charSet = NextConfigurationV2.getCurrent().getStringProperty(FALLBACK_CODEPAGE_CONFIG_KEY, FALLBACK_CODEPAGE);
		} catch (NoActiveModuleException e2) {
			log.error(e2.getMessage(), e2);
		}
 */
		CharsetDecoder decoder = Charset.forName(CODEPAGE).newDecoder();
		try {
			CharBuffer decoded = decoder.decode(ByteBuffer.wrap(bytes));
			return decoded.toString();
		} catch (CharacterCodingException e) {
			decoder = Charset.forName(charSet).newDecoder();
			try {
				CharBuffer decoded = decoder.decode(ByteBuffer.wrap(bytes));
				return decoded.toString();
			} catch (CharacterCodingException e1) {
				log.error(e1.getMessage(), e);
			}
		}
		return new String(bytes);
	}
	
	/**
	 * Creates the gson with custom adapters.
	 */
	private Gson getGson() {
	    GsonBuilder gb = new GsonBuilder();
	    gb.registerTypeAdapter(CustomerNumber.class, new GsonCustomerNumberSerializer());
	    gb.registerTypeAdapter(NationalPersonalId.class, new GsonNationalPersonIdSerializer());
	    gb.registerTypeAdapter(NationalCompanyId.class, new GsonNationalCompanyIdSerializer());
	    gb.registerTypeAdapter(NetBankCustomerId.class, new GsonNetBankCustomerIdSerializer());
	    return gb.create();
	}
}
