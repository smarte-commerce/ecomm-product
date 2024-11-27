package com.winnguyen1905.product.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1")
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface APIPrefix {
  String discription() default "";
}
