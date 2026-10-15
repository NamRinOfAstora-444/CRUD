package com.ele.javademo.code_gen;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class EnumToTypeScriptGenerator {

  private static final String OUTPUT_DIR = "src/main/resources/businessCode";
  private static final String OUTPUT_FILE = OUTPUT_DIR + "/business-codes.ts";
  private static final String PACKAGE_NAME = "com.ele.javademo.business_code";

  public static void main(String[] args) {
    try {
      System.out.println("🔍 Scanning for enum classes in package: " + PACKAGE_NAME);

      // Tạo thư mục output nếu chưa có
      Files.createDirectories(Paths.get(OUTPUT_DIR));

      // Tự động scan tất cả enum trong package
      List<Class<?>> enumClasses = findEnumClasses(PACKAGE_NAME);

      if (enumClasses.isEmpty()) {
        System.out.println("⚠️  No enum classes found in package: " + PACKAGE_NAME);
        return;
      }

      System.out.println("✓ Found " + enumClasses.size() + " enum class(es):");
      for (Class<?> enumClass : enumClasses) {
        System.out.println("  - " + enumClass.getSimpleName());
      }

      StringBuilder tsContent = new StringBuilder();
      tsContent.append("// @ts-nocheck\n");
      List<String> enumNames = new ArrayList<>();
      List<String> silentCodes = new ArrayList<>();

      // Generate TypeScript cho từng enum
      for (Class<?> enumClass : enumClasses) {
        tsContent.append(generateEnumTypeScript(enumClass));
        tsContent.append("\n\n");
        enumNames.add(enumClass.getSimpleName());
        collectSilentCodes(enumClass, silentCodes);
      }

      // Generate combined enum
      tsContent.append(generateCombinedEnum(enumNames));
      tsContent.append("\n\n");
      tsContent.append(generateSilentCodes(silentCodes));

      // Ghi file
      try (FileWriter writer = new FileWriter(OUTPUT_FILE)) {
        writer.write(tsContent.toString());
      }

      System.out.println("✅ TypeScript enums generated successfully at: " + OUTPUT_FILE);

    } catch (Exception e) {
      System.err.println("❌ Error generating TypeScript enums: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  private static List<Class<?>> findEnumClasses(String packageName) throws Exception {
    List<Class<?>> enumClasses = new ArrayList<>();
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
                if (clazz.isEnum()) {
                  enumClasses.add(clazz);
                }
              } catch (ClassNotFoundException e) {
                System.err.println("Could not load class: " + className);
              }
            }
          }
        }
      }
    }

    return enumClasses;
  }

  private static String generateEnumTypeScript(Class<?> enumClass) throws Exception {
    StringBuilder sb = new StringBuilder();
    String enumName = enumClass.getSimpleName();

    // Comment header
    sb.append("// ").append(enumName).append("\n");
    sb.append("export enum ").append(enumName).append(" {\n");

    // Get enum constants
    Object[] enumConstants = enumClass.getEnumConstants();
    Method getCodeMethod = enumClass.getMethod("getCode");
    Method getMessageMethod = enumClass.getMethod("getMessage");

    // Generate enum values
    for (int i = 0; i < enumConstants.length; i++) {
      Object enumConstant = enumConstants[i];
      String name = ((Enum<?>) enumConstant).name();
      String code = (String) getCodeMethod.invoke(enumConstant);

      sb.append("  ").append(name).append(" = \"").append(code).append("\"");
      if (i < enumConstants.length - 1) {
        sb.append(",");
      }
      sb.append("\n");
    }

    sb.append("}\n\n");

    // Generate messages object
    sb.append("export const ")
        .append(enumName)
        .append("Messages: Record<")
        .append(enumName)
        .append(", string> = {\n");
    for (int i = 0; i < enumConstants.length; i++) {
      Object enumConstant = enumConstants[i];
      String name = ((Enum<?>) enumConstant).name();
      String message = (String) getMessageMethod.invoke(enumConstant);

      sb.append("  [")
          .append(enumName)
          .append(".")
          .append(name)
          .append("]: \"")
          .append(message)
          .append("\"");
      if (i < enumConstants.length - 1) {
        sb.append(",");
      }
      sb.append("\n");
    }
    sb.append("};");

    return sb.toString();
  }

  private static void collectSilentCodes(Class<?> enumClass, List<String> silentCodes)
      throws Exception {
    Method isSilentMethod;
    try {
      isSilentMethod = enumClass.getMethod("isSilent");
    } catch (NoSuchMethodException e) {
      return;
    }

    String enumName = enumClass.getSimpleName();
    for (Object enumConstant : enumClass.getEnumConstants()) {
      boolean silent = (boolean) isSilentMethod.invoke(enumConstant);
      if (silent) {
        String name = ((Enum<?>) enumConstant).name();
        silentCodes.add(enumName + "." + name);
      }
    }
  }

  private static String generateSilentCodes(List<String> silentCodes) {
    StringBuilder sb = new StringBuilder();
    sb.append("/**\n");
    sb.append(" * Các business code KHÔNG hiển thị toast tự động.\n");
    sb.append(" * Caller tự chịu trách nhiệm xử lý (vd: hiển thị inline error trên form).\n");
    sb.append(" */\n");
    sb.append("export const SILENT_BUSINESS_CODES = new Set<string>([\n");
    for (int i = 0; i < silentCodes.size(); i++) {
      sb.append("  ").append(silentCodes.get(i));
      if (i < silentCodes.size() - 1) {
        sb.append(",");
      }
      sb.append("\n");
    }
    sb.append("]);");
    return sb.toString();
  }

  private static String generateCombinedEnum(List<String> enumNames) {
    StringBuilder sb = new StringBuilder();

    sb.append("// Combined Business Code (All codes)\n");
    sb.append("export const BusinessCode = {\n");
    for (int i = 0; i < enumNames.size(); i++) {
      sb.append("  ...").append(enumNames.get(i));
      if (i < enumNames.size() - 1) {
        sb.append(",");
      }
      sb.append("\n");
    }
    sb.append("} as const;\n\n");

    // Type union
    sb.append("export type BusinessCode = ");
    for (int i = 0; i < enumNames.size(); i++) {
      sb.append(enumNames.get(i));
      if (i < enumNames.size() - 1) {
        sb.append(" | ");
      }
    }
    sb.append(";\n\n");

    // Combined messages
    sb.append("// Combined Messages\n");
    sb.append("export const BusinessCodeMessages = {\n");
    for (int i = 0; i < enumNames.size(); i++) {
      sb.append("  ...").append(enumNames.get(i)).append("Messages");
      if (i < enumNames.size() - 1) {
        sb.append(",");
      }
      sb.append("\n");
    }
    sb.append("};");

    return sb.toString();
  }
}
