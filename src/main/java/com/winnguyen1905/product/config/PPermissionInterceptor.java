package com.winnguyen1905.product.config;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import com.winnguyen1905.product.exception.BaseException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class PPermissionInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String path = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    String requestURI = request.getRequestURI();
    String httpMethod = request.getMethod();

    if (SecurityUtils.getCurrentUsersPermissions().isPresent()) {
      List<Permission> permissionDTOs = SecurityUtils.getCurrentUsersPermissions().get().stream()
          .map(stringPermission -> SecurityUtils.jsonToPermission(stringPermission)).toList();
      Boolean isAllow = permissionDTOs.stream()
          .anyMatch(item -> (item.apiPath().equals(path) && item.method().equals(httpMethod))
              || item.apiPath().equals("/api/v1/"));
      if (isAllow == false)
        throw new BaseException("Cannot use endpoint " + path, 403, "Forbidden");
    }
    return true;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
      ModelAndView modelAndView) throws Exception {
    // Post-request logic
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
      throws Exception {
    // Cleanup logic after request completion
  }

}
