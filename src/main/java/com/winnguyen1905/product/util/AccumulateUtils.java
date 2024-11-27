package com.winnguyen1905.product.util;

import java.lang.reflect.Field;
import java.util.Arrays;

public class AccumulateUtils {
    public static <T> T accumulate(T object1, T targetObject) {
        Field[] fields = object1.getClass().getDeclaredFields();
        Arrays.asList(fields).forEach(field -> {
                try {
                    String fieldName = field.getName();
                    Field targetField = targetObject.getClass().getDeclaredField(fieldName);
                    targetField.setAccessible(true);
                    field.setAccessible(true);
                    Object value1 = field.get(object1);
                    Object value2 = targetField.get(targetObject);
                    
                    if(value1 == null || !(value1 instanceof Number)) return;

                    if(value2 == null) targetField.set(targetObject, value1);
                    else targetField.set(targetObject, Double.parseDouble(value2.toString()) + Double.parseDouble(value1.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        );
        return targetObject;
    }
}
