package com.nordea.dompap.workflow.content;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.nordea.next.dompap.domain.type.NationalPersonalId;
import com.nordea.next.dompap.domain.type.dk.NationalPersonalId_DK;
import com.nordea.next.dompap.domain.type.factory.NationalPersonalIdFactory;
import com.nordea.next.dompap.domain.type.fi.NationalPersonalId_FI;
import com.nordea.next.dompap.domain.type.no.NationalPersonalId_NO;
import com.nordea.next.dompap.domain.type.se.NationalPersonalId_SE;

public class GsonNationalPersonIdSerializer implements JsonSerializer<NationalPersonalId>, JsonDeserializer<NationalPersonalId> {

    @Override
    public NationalPersonalId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jo = json.getAsJsonObject();
        String country = jo.get("country").getAsString();
        String number = jo.get("number").getAsString();
        return new NationalPersonalIdFactory(country).create(number);
    }

    @Override
    public JsonElement serialize(NationalPersonalId src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        Class<? extends NationalPersonalId> clazz = src.getClass();
        String className = clazz.getName();
        if (className.equals(NationalPersonalId_DK.class.getName())) {
            json.addProperty("country", "DK");
        } else if (className.equals(NationalPersonalId_FI.class.getName())) {
            json.addProperty("country", "FI");
        } else if (className.equals(NationalPersonalId_NO.class.getName())) {
            json.addProperty("country", "NO");
        } else if (className.equals(NationalPersonalId_SE.class.getName())) {
            json.addProperty("country", "SE");
        }
        json.addProperty("number", src.getNationalPersonalId());
        return json;
    }
    
}
