package com.nordea.dompap.workflow.content;

import com.google.gson.*;
import com.nordea.next.dompap.domain.type.CustomerNumber;
import com.nordea.next.dompap.domain.type.dk.CorporateCustomerNumber_DK;
import com.nordea.next.dompap.domain.type.dk.CustomerNumber_DK;
import com.nordea.next.dompap.domain.type.factory.CorporateCustomerNumberFactory;
import com.nordea.next.dompap.domain.type.factory.CustomerNumberFactory;
import com.nordea.next.dompap.domain.type.fi.CorporateCustomerNumber_FI;
import com.nordea.next.dompap.domain.type.fi.CustomerNumber_FI;
import com.nordea.next.dompap.domain.type.no.CorporateCustomerNumber_NO;
import com.nordea.next.dompap.domain.type.no.CustomerNumber_NO;
import com.nordea.next.dompap.domain.type.se.CorporateCustomerNumber_SE;
import com.nordea.next.dompap.domain.type.se.CustomerNumber_SE;

import java.lang.reflect.Type;

public class GsonCustomerNumberSerializer implements JsonSerializer<CustomerNumber>, JsonDeserializer<CustomerNumber> {
    private static final String COUNTRY_NAME = "country";
    private static final String CUSTOMER_TYPE = "customerType";
    private static final String CUSTOMER_NUMBER = "number";
    private static final String CORPORATE_CUSTOMER = "Corporate";

    private enum COUNTY_NAME {
        DK, FI, SE, NO
    }

    @Override
    public JsonElement serialize(CustomerNumber src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        if (src == null) {
            throw new IllegalArgumentException("Unable to serialize, CustomerNumber is null");
        }
        Class<? extends CustomerNumber> clazz = src.getClass();
        String className = clazz.getName();
        if (CustomerNumber_DK.class.getName().equals(className)) {
            json.addProperty(COUNTRY_NAME, COUNTY_NAME.DK.name());
        } else if (CustomerNumber_FI.class.getName().equals(className)) {
            json.addProperty(COUNTRY_NAME, COUNTY_NAME.FI.name());
        } else if (CustomerNumber_NO.class.getName().equals(className)) {
            json.addProperty(COUNTRY_NAME, COUNTY_NAME.NO.name());
        } else if (CustomerNumber_SE.class.getName().equals(className)) {
            json.addProperty(COUNTRY_NAME, COUNTY_NAME.SE.name());
        } else if (CorporateCustomerNumber_DK.class.getName().equals(className)) {
            json.addProperty(COUNTRY_NAME, COUNTY_NAME.DK.name());
            json.addProperty(CUSTOMER_TYPE, CORPORATE_CUSTOMER);
        } else if (CorporateCustomerNumber_NO.class.getName().equals(className)) {
            json.addProperty(COUNTRY_NAME, COUNTY_NAME.NO.name());
            json.addProperty(CUSTOMER_TYPE, CORPORATE_CUSTOMER);
        } else if (CorporateCustomerNumber_SE.class.getName().equals(className)) {
            json.addProperty(COUNTRY_NAME, COUNTY_NAME.SE.name());
            json.addProperty(CUSTOMER_TYPE, CORPORATE_CUSTOMER);
        } else if (CorporateCustomerNumber_FI.class.getName().equals(className)) {
            json.addProperty(COUNTRY_NAME, COUNTY_NAME.FI.name());
            json.addProperty(CUSTOMER_TYPE, CORPORATE_CUSTOMER);
        }
        json.addProperty(CUSTOMER_NUMBER, src.getCustomerNumber());
        return json;
    }

    @Override
    public CustomerNumber deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        JsonElement country = jsonObject.get(COUNTRY_NAME);
        JsonElement number = jsonObject.get(CUSTOMER_NUMBER);
        JsonElement custType = jsonObject.get(CUSTOMER_TYPE);
        if (country == null || number == null) {
            throw new IllegalArgumentException(
                    String.format("Unable to deserialize, values of country:%s and customer number:%s",
                            country, number));
        }
        if (custType != null && CORPORATE_CUSTOMER.equals(custType.getAsString())) {
            return new CorporateCustomerNumberFactory(country.getAsString()).create(number.getAsString());
        } else {
            return new CustomerNumberFactory(country.getAsString()).create(number.getAsString());
        }

    }
}
