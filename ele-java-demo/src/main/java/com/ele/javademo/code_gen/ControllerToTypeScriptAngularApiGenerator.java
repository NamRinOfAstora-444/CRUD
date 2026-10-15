package com.ele.javademo.code_gen;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.web.bind.annotation.*;

public class ControllerToTypeScriptAngularApiGenerator {

  private static final String OUTPUT_DIR = "src/main/resources/api-angular";
  private static final String CONTROLLER_PACKAGE = "com.ele.javademo.controllers";
  private static final String DTO_PACKAGE = "com.ele.javademo.dto";
  private static final String API_BASE_URL = resolveBaseUrl();

  private static String resolveBaseUrl() {
    try (InputStream is =
        ControllerToTypeScriptAngularApiGenerator.class
            .getClassLoader()
            .getResourceAsStream("application.properties")) {
      if (is == null) return "http://localhost:8080";

      Properties props = new Properties();
      props.load(is);

      String raw = props.getProperty("server.port", "8080");
      String port = resolvePropertyValue(raw);
      return "http://localhost:" + port;
    } catch (Exception e) {
      return "http://localhost:8080";
    }
  }

  private static String resolvePropertyValue(String value) {
    // Resolve ${ENV_VAR:default} pattern
    Pattern pattern = Pattern.compile("\\$\\{([^:}]+)(?::([^}]*))?}");
    Matcher matcher = pattern.matcher(value);
    if (matcher.matches()) {
      String envVar = matcher.group(1);
      String defaultVal = matcher.group(2) != null ? matcher.group(2) : "";
      String envValue = System.getenv(envVar);
      return (envValue != null && !envValue.isEmpty()) ? envValue : defaultVal;
    }
    return value;
  }

  public static void main(String[] args) {
    try {
      System.out.println("🔍 Scanning for controller classes in package: " + CONTROLLER_PACKAGE);

      // Tạo thư mục output nếu chưa có
      Files.createDirectories(Paths.get(OUTPUT_DIR));

      // Scan tất cả controller trong package
      List<Class<?>> controllerClasses = findControllerClasses(CONTROLLER_PACKAGE);

      if (controllerClasses.isEmpty()) {
        System.out.println("⚠️  No controller classes found in package: " + CONTROLLER_PACKAGE);
        return;
      }

      System.out.println("✓ Found " + controllerClasses.size() + " controller class(es):");
      for (Class<?> controllerClass : controllerClasses) {
        System.out.println("  - " + controllerClass.getSimpleName());
      }

      // Generate TypeScript service cho từng controller
      for (Class<?> controllerClass : controllerClasses) {
        generateTypeScriptService(controllerClass);
      }

      System.out.println("✅ TypeScript API services generated successfully at: " + OUTPUT_DIR);

    } catch (Exception e) {
      System.err.println("❌ Error generating TypeScript API services: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static List<Class<?>> findControllerClasses(String packageName) throws Exception {
    List<Class<?>> controllerClasses = new ArrayList<>();
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    String path = packageName.replace('.', '/');
    Enumeration<URL> resources = classLoader.getResources(path);

    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      File directory = new File(resource.getFile());

      if (directory.exists()) {
        File[] files = directory.listFiles();
        if (files != null) {
          for (File file : files) {
            if (file.getName().endsWith(".class")) {
              String className =
                  packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
              try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(RestController.class)) {
                  controllerClasses.add(clazz);
                }
              } catch (ClassNotFoundException e) {
                System.err.println("Could not load class: " + className);
              }
            }
          }
        }
      }
    }

    return controllerClasses;
  }

  private static void generateTypeScriptService(Class<?> controllerClass) throws Exception {
    String controllerName = controllerClass.getSimpleName().replace("Controller", "");
    String kebabName = camelToKebab(controllerName);
    String dtoFolderName = controllerName.toLowerCase(); // UserController -> user

    System.out.println("\n  📁 Processing controller: " + controllerClass.getSimpleName());
    System.out.println("  🔍 Looking for DTOs in folder: dto/" + dtoFolderName);

    // Find all DTO classes in the corresponding folder
    Map<String, Class<?>> dtoClasses = findDtoClassesInFolder(dtoFolderName);

    if (dtoClasses.isEmpty()) {
      System.out.println("  ⚠️  No DTO classes found in dto/" + dtoFolderName);
    } else {
      System.out.println("  ✓ Found " + dtoClasses.size() + " DTO class(es):");
      for (String dtoName : dtoClasses.keySet()) {
        System.out.println("    - " + dtoName);
      }
    }

    // Generate interface file
    generateInterfaceFile(controllerName, kebabName, dtoClasses);

    // Generate API service file
    Method[] methods = controllerClass.getDeclaredMethods();
    generateApiFile(controllerClass, controllerName, kebabName, methods);
  }

  private static Map<String, Class<?>> findDtoClassesInFolder(String folderName) throws Exception {
    Map<String, Class<?>> dtoClasses = new HashMap<>();
    String dtoPackagePath = DTO_PACKAGE + "." + folderName;

    try {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      String path = dtoPackagePath.replace('.', '/');
      Enumeration<URL> resources = classLoader.getResources(path);

      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        File directory = new File(resource.getFile());

        if (directory.exists() && directory.isDirectory()) {
          File[] files = directory.listFiles();
          if (files != null) {
            for (File file : files) {
              if (file.getName().endsWith(".class")) {
                String className =
                    dtoPackagePath + '.' + file.getName().substring(0, file.getName().length() - 6);

                // Skip inner classes (containing $) - these are Lombok Builder classes, etc.
                if (className.contains("$")) {
                  continue;
                }

                try {
                  Class<?> clazz = Class.forName(className);
                  // Only include non-abstract, non-interface classes
                  if (!clazz.isInterface()
                      && !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                    dtoClasses.put(clazz.getSimpleName(), clazz);
                  }
                } catch (ClassNotFoundException e) {
                  System.err.println("    ⚠️  Could not load class: " + className);
                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      // Folder doesn't exist or can't be accessed
      System.out.println("  ℹ️  DTO folder '" + folderName + "' not found");
    }

    return dtoClasses;
  }

  private static boolean isPrimitiveType(String typeName) {
    return typeName.equals("String")
        || typeName.equals("Integer")
        || typeName.equals("Long")
        || typeName.equals("Double")
        || typeName.equals("Boolean")
        || typeName.equals("Void");
  }

  private static void generateInterfaceFile(
      String controllerName, String kebabName, Map<String, Class<?>> usedTypes) throws Exception {
    // Create folder for this controller
    String folderPath = OUTPUT_DIR + "/" + kebabName;
    Files.createDirectories(Paths.get(folderPath));

    String fileName = kebabName + ".interface.ts";
    String outputFile = folderPath + "/" + fileName;

    StringBuilder tsContent = new StringBuilder();

    // TODO: xoá đi nếu ảnh hưởng tới việc build bên FE
    tsContent.append("// @ts-nocheck\n");

    // Generate interfaces for each type
    for (Map.Entry<String, Class<?>> entry : usedTypes.entrySet()) {
      String typeName = entry.getKey();
      Class<?> typeClass = entry.getValue();

      tsContent.append("export interface I").append(typeName).append(" {\n");

      // Get all declared fields from the class
      Field[] fields = typeClass.getDeclaredFields();
      for (Field field : fields) {
        // Skip static and synthetic fields
        if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
          continue;
        }

        String fieldName = field.getName();
        String tsType = javaTypeToTypeScript(field.getType());

        // Handle optional fields (can be enhanced based on validation annotations)
        tsContent.append("  ").append(fieldName).append(": ").append(tsType).append(";\n");
      }

      tsContent.append("}\n\n");
    }

    // Write to file
    try (FileWriter writer = new FileWriter(outputFile)) {
      writer.write(tsContent.toString());
    }

    System.out.println("  ✓ Generated: " + kebabName + "/" + fileName);
  }

  private static void generateApiFile(
      Class<?> controllerClass, String controllerName, String kebabName, Method[] methods)
      throws Exception {
    // Create folder for this controller
    String folderPath = OUTPUT_DIR + "/" + kebabName;
    Files.createDirectories(Paths.get(folderPath));

    String serviceName = controllerName + "ApiService";
    String fileName = kebabName + ".api.ts";
    String outputFile = folderPath + "/" + fileName;

    StringBuilder tsContent = new StringBuilder();

    // TODO: xoá đi nếu ảnh hưởng tới việc build bên FE
    tsContent.append("// @ts-nocheck\n");

    // Import statements
    tsContent.append("import { inject, Injectable } from '@angular/core';\n");
    tsContent.append(
        "import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';\n");
    tsContent.append("import { Observable } from 'rxjs';\n");
    tsContent.append("\n");
    tsContent.append(
        "import { IBaseResponse, IBasePagination } from '../../shared/models/interfaces';\n");
    tsContent.append("import { SKIP_BUSINESS_ERROR_TOAST } from '../http-context';\n");
    tsContent.append("import * as Interfaces from './").append(kebabName).append(".interface';\n");
    tsContent.append("\n");

    // Service class
    tsContent.append("@Injectable({\n");
    tsContent.append("  providedIn: 'root',\n");
    tsContent.append("})\n");
    tsContent.append("export class ").append(serviceName).append(" {\n");

    // inject pattern
    tsContent.append("  http = inject(HttpClient);\n\n");

    // Base URL
    String baseMapping = getRequestMapping(controllerClass);
    tsContent
        .append("  private readonly API_BASE_URL = '")
        .append(API_BASE_URL)
        .append(baseMapping)
        .append("';\n\n");

    // buildContext helper
    tsContent.append("  private buildContext(skipToast: boolean): HttpContext {\n");
    tsContent.append("    return new HttpContext().set(SKIP_BUSINESS_ERROR_TOAST, skipToast);\n");
    tsContent.append("  }\n\n");

    // Generate methods for each endpoint
    for (Method method : methods) {
      String methodContent = generateMethodTypeScript(method);
      if (methodContent != null) {
        tsContent.append(methodContent).append("\n");
      }
    }

    tsContent.append("}\n");

    // Write to file
    try (FileWriter writer = new FileWriter(outputFile)) {
      writer.write(tsContent.toString());
    }

    System.out.println("  ✓ Generated: " + kebabName + "/" + fileName);
  }

  private static String getRequestMapping(Class<?> controllerClass) {
    RequestMapping requestMapping = controllerClass.getAnnotation(RequestMapping.class);
    if (requestMapping != null && requestMapping.value().length > 0) {
      return requestMapping.value()[0];
    }
    return "";
  }

  private static String generateMethodTypeScript(Method method) {
    String httpMethod = null;
    String path = "";

    // Determine HTTP method and path
    if (method.isAnnotationPresent(GetMapping.class)) {
      httpMethod = "GET";
      GetMapping mapping = method.getAnnotation(GetMapping.class);
      path = mapping.value().length > 0 ? mapping.value()[0] : "";
    } else if (method.isAnnotationPresent(PostMapping.class)) {
      httpMethod = "POST";
      PostMapping mapping = method.getAnnotation(PostMapping.class);
      path = mapping.value().length > 0 ? mapping.value()[0] : "";
    } else if (method.isAnnotationPresent(PutMapping.class)) {
      httpMethod = "PUT";
      PutMapping mapping = method.getAnnotation(PutMapping.class);
      path = mapping.value().length > 0 ? mapping.value()[0] : "";
    } else if (method.isAnnotationPresent(DeleteMapping.class)) {
      httpMethod = "DELETE";
      DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);
      path = mapping.value().length > 0 ? mapping.value()[0] : "";
    } else if (method.isAnnotationPresent(PatchMapping.class)) {
      httpMethod = "PATCH";
      PatchMapping mapping = method.getAnnotation(PatchMapping.class);
      path = mapping.value().length > 0 ? mapping.value()[0] : "";
    }

    if (httpMethod == null) {
      return null;
    }

    StringBuilder sb = new StringBuilder();
    String methodName = method.getName();

    // Extract parameters
    Parameter[] parameters = method.getParameters();
    List<String> tsParams = new ArrayList<>();
    List<String> queryParams = new ArrayList<>();
    List<String> pathVariables = new ArrayList<>();
    String bodyParam = null;

    for (Parameter param : parameters) {
      if (param.isAnnotationPresent(RequestParam.class)) {
        RequestParam requestParam = param.getAnnotation(RequestParam.class);
        String paramName =
            requestParam.value().isEmpty() ? requestParam.name() : requestParam.value();
        if (paramName.isEmpty()) {
          paramName = param.getName();
        }
        String defaultValue = requestParam.defaultValue();
        boolean hasDefault = !defaultValue.contains("\ue000");
        if (hasDefault) {
          tsParams.add(paramName + " = " + toTsDefaultValue(param.getType(), defaultValue));
        } else {
          tsParams.add(paramName + ": " + javaTypeToTypeScript(param.getType()));
        }
        queryParams.add(paramName);
      } else if (param.isAnnotationPresent(PathVariable.class)) {
        PathVariable pathVariable = param.getAnnotation(PathVariable.class);
        String paramName =
            pathVariable.value().isEmpty() ? pathVariable.name() : pathVariable.value();
        if (paramName.isEmpty()) {
          paramName = param.getName();
        }
        tsParams.add(paramName + ": " + javaTypeToTypeScript(param.getType()));
        pathVariables.add(paramName);
      } else if (param.isAnnotationPresent(RequestBody.class)) {
        String paramName = param.getName();
        tsParams.add(paramName + ": Interfaces.I" + param.getType().getSimpleName());
        bodyParam = paramName;
      }
    }

    // skipToast always last
    tsParams.add("skipToast = false");

    // Determine return type
    Type returnType = method.getGenericReturnType();
    String tsReturnType = extractResponseType(returnType);
    String wrappedReturnType = "IBaseResponse<" + tsReturnType + ">";

    // Method signature - multi-line
    sb.append("  ").append(methodName).append("(\n");
    for (String p : tsParams) {
      sb.append("    ").append(p).append(",\n");
    }
    sb.append("  ): Observable<").append(wrappedReturnType).append("> {\n");

    // Build URL
    String url;
    if (path.isEmpty()) {
      url = "this.API_BASE_URL";
    } else {
      String template = "`${this.API_BASE_URL}" + path + "`";
      for (String pathVar : pathVariables) {
        template = template.replace("{" + pathVar + "}", "${" + pathVar + "}");
      }
      url = template;
    }

    // HttpParams - chained style
    if (!queryParams.isEmpty()) {
      sb.append("    const params = new HttpParams()");
      for (String qp : queryParams) {
        sb.append(".set('").append(qp).append("', ").append(qp).append(".toString())");
      }
      sb.append(";\n\n");
    }

    // HTTP call
    if (httpMethod.equals("GET")) {
      if (!queryParams.isEmpty()) {
        sb.append("    return this.http.get<").append(wrappedReturnType).append(">(\n");
        sb.append("      ").append(url).append(",\n");
        sb.append("      { params, context: this.buildContext(skipToast) },\n");
        sb.append("    );\n");
      } else {
        sb.append("    return this.http.get<")
            .append(wrappedReturnType)
            .append(">(")
            .append(url)
            .append(", { context: this.buildContext(skipToast) });\n");
      }
    } else if (bodyParam != null) {
      sb.append("    return this.http.")
          .append(httpMethod.toLowerCase())
          .append("<")
          .append(wrappedReturnType)
          .append(">(")
          .append(url)
          .append(", ")
          .append(bodyParam)
          .append(", {\n");
      sb.append("      context: this.buildContext(skipToast),\n");
      sb.append("    });\n");
    } else {
      if (!queryParams.isEmpty()) {
        sb.append("    return this.http.")
            .append(httpMethod.toLowerCase())
            .append("<")
            .append(wrappedReturnType)
            .append(">(\n");
        sb.append("      ").append(url).append(",\n");
        sb.append("      { params, context: this.buildContext(skipToast) },\n");
        sb.append("    );\n");
      } else {
        sb.append("    return this.http.")
            .append(httpMethod.toLowerCase())
            .append("<")
            .append(wrappedReturnType)
            .append(">(")
            .append(url)
            .append(", { context: this.buildContext(skipToast) });\n");
      }
    }

    sb.append("  }\n");
    return sb.toString();
  }

  private static String toTsDefaultValue(Class<?> javaType, String defaultValue) {
    if (javaType == String.class) {
      return "'" + defaultValue + "'";
    }
    return defaultValue;
  }

  private static String extractResponseType(Type returnType) {
    if (returnType instanceof ParameterizedType) {
      ParameterizedType paramType = (ParameterizedType) returnType;
      Type[] typeArgs = paramType.getActualTypeArguments();

      if (typeArgs.length > 0) {
        Type innerType = typeArgs[0];
        if (innerType instanceof ParameterizedType) {
          ParameterizedType innerParamType = (ParameterizedType) innerType;
          String rawTypeName = ((Class<?>) innerParamType.getRawType()).getSimpleName();

          if (rawTypeName.equals("PagedResponse")) {
            Type[] innerTypeArgs = innerParamType.getActualTypeArguments();
            if (innerTypeArgs.length > 0) {
              String contentType = getSimpleTypeName(innerTypeArgs[0]);
              return "IBasePagination<Interfaces.I" + contentType + ">";
            }
          }

          return "Interfaces.I" + rawTypeName;
        } else if (innerType instanceof Class) {
          String typeName = ((Class<?>) innerType).getSimpleName();
          if (isPrimitiveType(typeName)) {
            return typeName.toLowerCase();
          }
          return "Interfaces.I" + typeName;
        }
      }
    }
    return "any";
  }

  private static String getSimpleTypeName(Type type) {
    if (type instanceof Class) {
      return ((Class<?>) type).getSimpleName();
    } else if (type instanceof ParameterizedType) {
      return ((Class<?>) ((ParameterizedType) type).getRawType()).getSimpleName();
    }
    return "any";
  }

  private static String javaTypeToTypeScript(Class<?> javaType) {
    // Primitive types and wrappers
    if (javaType == int.class
        || javaType == Integer.class
        || javaType == long.class
        || javaType == Long.class
        || javaType == double.class
        || javaType == Double.class
        || javaType == float.class
        || javaType == Float.class
        || javaType == short.class
        || javaType == Short.class
        || javaType == byte.class
        || javaType == Byte.class) {
      return "number";
    } else if (javaType == boolean.class || javaType == Boolean.class) {
      return "boolean";
    } else if (javaType == String.class) {
      return "string";
    } else if (javaType == LocalDateTime.class
        || javaType == LocalDate.class
        || javaType.getName().equals("java.util.Date")
        || javaType.getName().equals("java.sql.Timestamp")) {
      return "string"; // ISO date string
    } else if (javaType.isEnum()) {
      return "string"; // Enums as string
    } else if (javaType == Object.class) {
      return "any";
    }

    // Complex types - reference by name
    return "any";
  }

  private static String camelToKebab(String camelCase) {
    return camelCase.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
  }
}
