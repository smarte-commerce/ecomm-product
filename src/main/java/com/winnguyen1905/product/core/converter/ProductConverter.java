package com.winnguyen1905.product.core.converter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.winnguyen1905.product.core.model.Product;
import com.winnguyen1905.product.core.model.request.AddProductRequest;
import com.winnguyen1905.product.exception.ResourceNotFoundException;
import com.winnguyen1905.product.persistance.entity.EProduct;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductConverter {

  private final ModelMapper mapper;

  public EProduct map(AddProductRequest product) {
    return null;
  }

  public EProduct map(Product product) {
    return null;
  }

  public Product map(EProduct product) {
    return null;
  }

  // static {
    // productRegistry = new HashMap<String, Class<?>>();
    // productRegistry.put("electronic_", Electronic.class);
    // productRegistry.put("eelectronic", EElectronic.class);

    // productRegistry.put("smartwatch_", SmartPhone.class);
    // productRegistry.put("smartwatch_entity", SmartPhoneEntity.class);
  // }

  // public <D> D toProductEntity(Product product) {
  //   try {
  //     Class<?> dClass = this.productRegistry.get(product.getProductType() + "_entity");
  //     if (dClass == null)
  //       throw new ResourceNotFoundException("Not found product type " + product.getProductType());
  //     D instanceOfDClass = (D) dClass.getDeclaredConstructor().newInstance();
  //     this.modelMapper.map(product, instanceOfDClass);
  //     return instanceOfDClass;
  //   } catch (Exception e) {
  //     e.printStackTrace();
  //   }
  //   return (D) this.modelMapper.map(product, EProduct.class);
  // }

  // public <D> D toProduct(EProduct product) {
  //   try {
  //     Class<?> dClass = this.productRegistry.get(product.getProductType() + "_");
  //     if (dClass == null)
  //       throw new ResourceNotFoundException("Not found product type " + product.getProductType());
  //     D instanceOfDClass = (D) dClass.getDeclaredConstructor().newInstance();
  //     this.modelMapper.map(product, instanceOfDClass);
  //     return instanceOfDClass;
  //   } catch (Exception e) {
  //     e.printStackTrace();
  //   }
  //   return (D) this.modelMapper.map(product, Product.class);
  // }

  // public List<Field> getAllField(Class<?> dClass) {
  //   if (dClass == Object.class)
  //     return new ArrayList<>();
  //   List<Field> parentFields = getAllField(dClass.getSuperclass());
  //   parentFields.addAll(Arrays.asList(dClass.getDeclaredFields()));
  //   return parentFields;
  // }

  // public <D> D taget(D object, D target) {
  // List<Field> fieldList = getAllField(object.getClass());
  // fieldList.stream().forEach(item -> {

  // });
  // field.setAccessible(true);
  // String fieldName = field.getName();
  // Object value = field.get(object);
  // if(value instanceof Collection) {
  // for(Object val : )
  // }
  // }

  // public <D> D mergeNestedProductModel(D object, D target) {
  // List<Field> fields = getAllField(object.getClass());
  // fields.stream().forEach(item -> {
  // item.setAccessible(true);
  // Object value = item.get(object);
  // if(value instanceof Collection) {

  // }
  // })
  // Field[] fields = object.getClass().getDeclaredFields();
  // while(object.getClass().getSuperclass() != Object.class) {
  // fieldList.addAll(Arrays.asList(object.getClass().getDeclaredFields()));
  // object = (D) object.getClass().getSuperclass();
  // }
  // modelMapper.addMappings(new PropertyMap<Product, Product>() {
  // @Override
  // protected void configure() {
  // map().setInventory(source.getInventory()); // Map nested Inventory
  // }
  // });
  // return object;
  // }
}
