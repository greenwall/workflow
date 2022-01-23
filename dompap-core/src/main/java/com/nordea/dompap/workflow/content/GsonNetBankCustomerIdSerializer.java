package com.nordea.dompap.workflow.content;

import java.lang.reflect.Type;
import java.util.Optional;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.nordea.next.dompap.domain.type.NetBankCustomerId;
import com.nordea.next.dompap.domain.type.factory.NetBankCustomerIdFactory;
import com.nordea.next.dompap.domain.type.fi.NetBankCustomerId_FI;
/**
 * 
 * @author G90511(Hitesh Karel)
 *
 */
public class GsonNetBankCustomerIdSerializer
        implements JsonSerializer<NetBankCustomerId>, JsonDeserializer<NetBankCustomerId> {

    @Override
    public NetBankCustomerId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        Optional<JsonElement> country = Optional.of(jsonObject.get("country"));
        Optional<JsonElement> number = Optional.of(jsonObject.get("number"));
        if (!country.isPresent() || !number.isPresent()) {
            throw new IllegalArgumentException("Unable to deserialize, either country or netbankId is null");
        }
        return new NetBankCustomerIdFactory(country.get().getAsString()).create(number.get().getAsString());
    }

    @Override
    public JsonElement serialize(NetBankCustomerId src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        if (src == null) {
            throw new IllegalArgumentException("Unable to serialize, NetBankCustomerId is null");
        }
        Class<? extends NetBankCustomerId> clazz = src.getClass();
        String clazzName = clazz.getName();
        if (NetBankCustomerId_FI.class.getName().equals(clazzName)) {
            json.addProperty("country", "FI");
        }
        json.addProperty("number", src.getCustomerNetBankId());
        return json;
    }

}
