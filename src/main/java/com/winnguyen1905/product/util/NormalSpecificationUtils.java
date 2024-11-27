package com.winnguyen1905.product.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.winnguyen1905.product.core.model.BaseObject;
import com.winnguyen1905.product.persistance.repository.custom.QuerySpecification; 

public class NormalSpecificationUtils<D> {
    public static <T, D> List<Specification<D>> toNormalSpec(T tSearchRequest) {
        Field[] fields = tSearchRequest.getClass().getDeclaredFields();
        List<Specification<D>> specList = new ArrayList<>();
        Arrays.asList(fields).forEach(field -> {
            try {
                String fieldName = field.getName(); // Field name of Entity class
                field.setAccessible(true);
                Object value = field.get(tSearchRequest);
                if(value == null || fieldName.indexOf("Id") != -1) return;
                
                if(value instanceof Boolean bl) {
                    if(bl == true) specList.add(QuerySpecification.isTrue((Boolean) value, fieldName, null));
                    else specList.add(QuerySpecification.isFalse((Boolean) value, fieldName, null));
                    return;
                }
                if(value instanceof String str) {
                    specList.add(QuerySpecification.isValueLike((String) str, fieldName, null));
                    return;
                }
                if(value instanceof Number num) {
                    if(fieldName.endsWith("From"))  
                        specList.add(QuerySpecification.isGreaterThanOrEqual((Double) num, fieldName.substring(0, fieldName.length() - 4), null));
                    else if(fieldName.endsWith("To"))
                        specList.add(QuerySpecification.isLessThanOrEqual((Double) num, fieldName.substring(0, fieldName.length() - 2), null));
                    else specList.add(QuerySpecification.isEqualValue((Integer) num, fieldName, null));
                    return;
                }
            } catch (Exception e) {e.printStackTrace();}
        });
        if(tSearchRequest instanceof BaseObject abstractDTO) {
            if(abstractDTO.getCreatedBy() != null) specList.add(QuerySpecification.isEqualValue(abstractDTO.getCreatedBy(), "createdBy", null));
            if(abstractDTO.getUpdatedBy() != null) specList.add(QuerySpecification.isEqualValue(abstractDTO.getUpdatedBy(), "updateBy", null));
        }
        return specList;
    }
}
