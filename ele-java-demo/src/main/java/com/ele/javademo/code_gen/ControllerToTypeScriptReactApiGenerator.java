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

/**
 * Generator for React TypeScript API services.
 *
 * <p>Output folder: {@code src/main/resources/api-react}
 *
 * <p>Conventions:
 *
 * <ul>
 *   <li>Interface file: {@code kebab-name.interface.ts} with {@code I} prefix
 *   <li>API file: {@code kebab-name.api.ts} — plain functions returning axios promise directly
 *   <li>HTTP client: axios via {@code httpContext}
 *   <li>Return type: {@code IBasePaginationResponse<T>} or {@code IBaseResponse<T>}
 * </ul>
 *
 * <p>Reference implementations:
 *
 * <ul>
 *   <li>Angular: {@link ControllerToTypeScriptAngularApiGenerator}
 *   <li>Vue: {@link ControllerToTypeScriptVueApiGenerator}
 * </ul>
 */
public class ControllerToTypeScriptReactApiGenerator {

  private static final String OUTPUT_DIR = "src/main/resources/api-react";
  private static final String CONTROLLER_PACKAGE = "com.ele.javademo.controllers";
  private static final String DTO_PACKAGE = "com.ele.javademo.dto";

  private static String resolveBaseUrl() {
    try (InputStream is =
        ControllerToTypeScriptReactApiGenerator.class
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

      Files.createDirectories(Paths.get(OUTPUT_DIR));

      List<Class<?>> controllerClasses = findControllerClasses(CONTROLLER_PACKAGE);

      if (controllerClasses.isEmpty()) {
        System.out.println("⚠️  No controller classes found in package: " + CONTROLLER_PACKAGE);
        return;
      }

      System.out.println("✓ Found " + controllerClasses.size() + " controller class(es):");
      for (Class<?> controllerClass : controllerClasses) {
        System.out.println("  - " + controllerClass.getSimpleName());
      }

      for (Class<?> controllerClass : controllerClasses) {
        generateTypeScriptService(controllerClass);
      }

      System.out.println(
          "✅ React TypeScript API services generated successfully at: " + OUTPUT_DIR);

    } catch (Exception e) {
      System.err.println("❌ Error generating React TypeScript API services: " + e.getMessage());
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
    String dtoFolderName = controllerName.toLowerCase();

    System.out.println("\n  📁 Processing controller: " + controllerClass.getSimpleName());
    System.out.println("  🔍 Looking for DTOs in folder: dto/" + dtoFolderName);

    Map<String, Class<?>> dtoClasses = findDtoClassesInFolder(dtoFolderName);

    if (dtoClasses.isEmpty()) {
      System.out.println("  ⚠️  No DTO classes found in dto/" + dtoFolderName);
    } else {
      System.out.println("  ✓ Found " + dtoClasses.size() + " DTO class(es):");
      for (String dtoName : dtoClasses.keySet()) {
        System.out.println("    - " + dtoName);
      }
    }

    generateInterfaceFile(controllerName, kebabName, dtoClasses);

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

                if (className.contains("$")) {
                  continue;
                }

                try {
                  Class<?> clazz = Class.forName(className);
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

  // ── Interface file: kebab-name.interface.ts ──────────────────────────────────

  private static void generateInterfaceFile(
      String controllerName, String kebabName, Map<String, Class<?>> usedTypes) throws Exception {
    String folderPath = OUTPUT_DIR + "/" + kebabName;
    Files.createDirectories(Paths.get(folderPath));

    String fileName = kebabName + ".interface.ts";
    String outputFile = folderPath + "/" + fileName;

    StringBuilder tsContent = new StringBuilder();

    // TODO: xoá đi nếu ảnh hưởng tới việc build bên FE
    tsContent.append("// @ts-nocheck\n");

    for (Map.Entry<String, Class<?>> entry : usedTypes.entrySet()) {
      String typeName = entry.getKey();
      Class<?> typeClass = entry.getValue();

      tsContent.append("export interface I").append(typeName).append(" {\n");

      Field[] fields = typeClass.getDeclaredFields();
      for (Field field : fields) {
        if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
          continue;
        }

        String fieldName = field.getName();
        String tsType = javaTypeToTypeScript(field.getType());

        tsContent.append("  ").append(fieldName).append(": ").append(tsType).append(";\n");
      }

      tsContent.append("}\n\n");
    }

    try (FileWriter writer = new FileWriter(outputFile)) {
      writer.write(tsContent.toString());
    }

    System.out.println("  ✓ Generated: " + kebabName + "/" + fileName);
  }

  // ── API file: kebab-name.api.ts (axios / httpContext, plain functions) ───────

  private static void generateApiFile(
      Class<?> controllerClass, String controllerName, String kebabName, Method[] methods)
      throws Exception {
    String folderPath = OUTPUT_DIR + "/" + kebabName;
    Files.createDirectories(Paths.get(folderPath));

    String fileName = kebabName + ".api.ts";
    String outputFile = folderPath + "/" + fileName;

    String basePath = getRequestMapping(controllerClass);

    // First pass: collect used types and rendered method bodies
    Set<String> usedDtoTypes = new LinkedHashSet<>();
    List<String> methodContents = new ArrayList<>();
    boolean needsPagination = false;
    boolean needsBaseResponse = false;

    for (Method method : methods) {
      String content = generateMethodTypeScript(method, basePath, usedDtoTypes);
      if (content != null) {
        methodContents.add(content);
        if (content.contains("IBasePaginationResponse")) needsPagination = true;
        if (content.contains("IBaseResponse")) needsBaseResponse = true;
      }
    }

    StringBuilder tsContent = new StringBuilder();

    // TODO: xoá đi nếu ảnh hưởng tới việc build bên FE
    tsContent.append("// @ts-nocheck\n");

    // Side-effect import for error interceptor
    tsContent.append("import \"../http-error.interceptor\";\n");
    tsContent.append("import { httpContext } from \"../http-context\";\n");

    // Shared model imports — path relative (no alias), no .ts extension
    List<String> sharedImports = new ArrayList<>();
    if (needsPagination) sharedImports.add("IBasePaginationResponse");
    if (needsBaseResponse) sharedImports.add("IBaseResponse");

    if (!sharedImports.isEmpty()) {
      tsContent.append("import type {\n");
      for (String imp : sharedImports) {
        tsContent.append("  ").append(imp).append(",\n");
      }
      tsContent.append("} from \"../../shared/models/interfaces\";\n");
    }
    tsContent.append("\n");

    // DTO type imports
    if (!usedDtoTypes.isEmpty()) {
      List<String> prefixedTypes = new ArrayList<>();
      for (String t : usedDtoTypes) prefixedTypes.add("I" + t);
      tsContent
          .append("import type { ")
          .append(String.join(", ", prefixedTypes))
          .append(" } from './" + kebabName + ".interface'\n");
    }
    tsContent.append("\n");

    // Method contents
    for (String content : methodContents) {
      tsContent.append(content).append("\n");
    }

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

  private static String generateMethodTypeScript(
      Method method, String basePath, Set<String> usedDtoTypes) {
    String httpMethod = null;
    String methodPath = "";

    if (method.isAnnotationPresent(GetMapping.class)) {
      httpMethod = "get";
      GetMapping mapping = method.getAnnotation(GetMapping.class);
      methodPath = mapping.value().length > 0 ? mapping.value()[0] : "";
    } else if (method.isAnnotationPresent(PostMapping.class)) {
      httpMethod = "post";
      PostMapping mapping = method.getAnnotation(PostMapping.class);
      methodPath = mapping.value().length > 0 ? mapping.value()[0] : "";
    } else if (method.isAnnotationPresent(PutMapping.class)) {
      httpMethod = "put";
      PutMapping mapping = method.getAnnotation(PutMapping.class);
      methodPath = mapping.value().length > 0 ? mapping.value()[0] : "";
    } else if (method.isAnnotationPresent(DeleteMapping.class)) {
      httpMethod = "delete";
      DeleteMapping mapping = method.getAnnotation(DeleteMapping.class);
      methodPath = mapping.value().length > 0 ? mapping.value()[0] : "";
    } else if (method.isAnnotationPresent(PatchMapping.class)) {
      httpMethod = "patch";
      PatchMapping mapping = method.getAnnotation(PatchMapping.class);
      methodPath = mapping.value().length > 0 ? mapping.value()[0] : "";
    }

    if (httpMethod == null) return null;

    StringBuilder sb = new StringBuilder();
    String methodName = method.getName();

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
        if (paramName.isEmpty()) paramName = param.getName();

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
        if (paramName.isEmpty()) paramName = param.getName();
        tsParams.add(paramName + ": " + javaTypeToTypeScript(param.getType()));
        pathVariables.add(paramName);
      } else if (param.isAnnotationPresent(RequestBody.class)) {
        bodyParam = param.getName();
        String dtoType = param.getType().getSimpleName();
        usedDtoTypes.add(dtoType);
        tsParams.add(bodyParam + ": I" + dtoType);
      }
    }

    // Return type
    Type returnType = method.getGenericReturnType();
    String tsReturnType = extractReactResponseType(returnType, usedDtoTypes);

    // Function signature — plain function (not async)
    sb.append("export const ").append(methodName).append(" = (");
    if (!tsParams.isEmpty()) {
      sb.append("\n");
      for (String p : tsParams) {
        sb.append("  ").append(p).append(",\n");
      }
      sb.append(")");
    } else {
      sb.append(")");
    }
    sb.append(" => {\n");

    // Build URL
    String fullPath = basePath + methodPath;
    String url;
    if (fullPath.isEmpty()) {
      url = "''";
    } else if (!pathVariables.isEmpty()) {
      String template = fullPath;
      for (String pathVar : pathVariables) {
        template = template.replace("{" + pathVar + "}", "${" + pathVar + "}");
      }
      url = "`" + template + "`";
    } else {
      url = "\"" + fullPath + "\"";
    }

    // Axios call — return directly (no async/await)
    boolean isGetOrDelete = httpMethod.equals("get") || httpMethod.equals("delete");
    if (isGetOrDelete) {
      if (!queryParams.isEmpty()) {
        sb.append("  return httpContext.")
            .append(httpMethod)
            .append("<")
            .append(tsReturnType)
            .append(">(")
            .append(url)
            .append(", {\n");
        sb.append("    params: { ").append(String.join(", ", queryParams)).append(" },\n");
        sb.append("  });\n");
      } else {
        sb.append("  return httpContext.")
            .append(httpMethod)
            .append("<")
            .append(tsReturnType)
            .append(">(")
            .append(url)
            .append(");\n");
      }
    } else {
      if (bodyParam != null && !queryParams.isEmpty()) {
        sb.append("  return httpContext.")
            .append(httpMethod)
            .append("<")
            .append(tsReturnType)
            .append(">(")
            .append(url)
            .append(", ")
            .append(bodyParam)
            .append(", {\n");
        sb.append("    params: { ").append(String.join(", ", queryParams)).append(" },\n");
        sb.append("  });\n");
      } else if (bodyParam != null) {
        sb.append("  return httpContext.")
            .append(httpMethod)
            .append("<")
            .append(tsReturnType)
            .append(">(")
            .append(url)
            .append(", ")
            .append(bodyParam)
            .append(");\n");
      } else if (!queryParams.isEmpty()) {
        sb.append("  return httpContext.")
            .append(httpMethod)
            .append("<")
            .append(tsReturnType)
            .append(">(")
            .append(url)
            .append(", undefined, {\n");
        sb.append("    params: { ").append(String.join(", ", queryParams)).append(" },\n");
        sb.append("  });\n");
      } else {
        sb.append("  return httpContext.")
            .append(httpMethod)
            .append("<")
            .append(tsReturnType)
            .append(">(")
            .append(url)
            .append(");\n");
      }
    }

    sb.append("};\n");

    return sb.toString();
  }

  /**
   * Maps Java return type to React response type.
   *
   * <ul>
   *   <li>{@code ResponseEntity<PagedResponse<Foo>>} → {@code IBasePaginationResponse<IFoo>}
   *   <li>{@code ResponseEntity<Foo>} → {@code IBaseResponse<IFoo>}
   * </ul>
   */
  private static String extractReactResponseType(Type returnType, Set<String> usedDtoTypes) {
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
              if (!isPrimitiveType(contentType)) usedDtoTypes.add(contentType);
              return "IBasePaginationResponse<I" + contentType + ">";
            }
          }

          if (!isPrimitiveType(rawTypeName)) usedDtoTypes.add(rawTypeName);
          return "IBaseResponse<I" + rawTypeName + ">";

        } else if (innerType instanceof Class) {
          String typeName = ((Class<?>) innerType).getSimpleName();
          if (isPrimitiveType(typeName)) {
            return "IBaseResponse<" + typeName.toLowerCase() + ">";
          }
          usedDtoTypes.add(typeName);
          return "IBaseResponse<I" + typeName + ">";
        }
      }
    }
    return "IBaseResponse<any>";
  }

  private static String getSimpleTypeName(Type type) {
    if (type instanceof Class) {
      return ((Class<?>) type).getSimpleName();
    } else if (type instanceof ParameterizedType) {
      return ((Class<?>) ((ParameterizedType) type).getRawType()).getSimpleName();
    }
    return "any";
  }

  private static String toTsDefaultValue(Class<?> javaType, String defaultValue) {
    if (javaType == String.class) {
      return "'" + defaultValue + "'";
    }
    return defaultValue;
  }

  private static String javaTypeToTypeScript(Class<?> javaType) {
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
      return "string";
    } else if (javaType == Object.class) {
      return "any";
    }
    return "any";
  }

  private static String camelToKebab(String camelCase) {
    return camelCase.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
  }
}
