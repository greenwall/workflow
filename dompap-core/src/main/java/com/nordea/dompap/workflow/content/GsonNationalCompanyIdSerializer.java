package com.nordea.dompap.workflow.content;

import java.lang.reflect.Type;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.nordea.next.dompap.domain.type.NationalCompanyId;
import com.nordea.next.dompap.domain.type.dk.NationalCompanyId_DK;
import com.nordea.next.dompap.domain.type.factory.NationalCompanyIdFactory;
import com.nordea.next.dompap.domain.type.fi.NationalCompanyId_FI;
import com.nordea.next.dompap.domain.type.no.NationalCompanyId_NO;
import com.nordea.next.dompap.domain.type.se.NationalCompanyId_SE;

public class GsonNationalCompanyIdSerializer implements JsonSerializer<NationalCompanyId>, JsonDeserializer<NationalCompanyId> {

    @Override
    public NationalCompanyId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jo = json.getAsJsonObject();
        String country = jo.get("country").getAsString();
        String number = jo.get("number").getAsString();
        return new NationalCompanyIdFactory(country).create(number);
    }

    @Override
    public JsonElement serialize(NationalCompanyId src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        Class<? extends NationalCompanyId> clazz = src.getClass();
        String className = clazz.getName();
        if (className.equals(NationalCompanyId_DK.class.getName())) {
            json.addProperty("country", "DK");
        } else if (className.equals(NationalCompanyId_FI.class.getName())) {
            json.addProperty("country", "FI");
        } else if (className.equals(NationalCompanyId_NO.class.getName())) {
            json.addProperty("country", "NO");
        } else if (className.equals(NationalCompanyId_SE.class.getName())) {
            json.addProperty("country", "SE");
        }
        json.addProperty("number", src.getNationalCompanyId());
        return json;
    }
}
