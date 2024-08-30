package sample.reflect;


import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClassDeclarationSpy {
  private final Class<?> clazz;

  public ClassDeclarationSpy(Class<?> clazz) {
    this.clazz = clazz;
  }

  public ClassDeclarationSpy(String name) {
    try {
      this.clazz = Class.forName(name);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public void spyGenericString() {
    log.info("Generic String: {}", clazz.toGenericString());
  }

  public void spyCanonicalName() {
    log.info("Name: {}", clazz.getCanonicalName());
  }

  public void spyModifiers() {
    log.info("Modifiers: {}", Modifier.toString(clazz.getModifiers()));
  }

  public void spyTypeParameters() {
    TypeVariable[] tvs = clazz.getTypeParameters();
    if (tvs.length > 0) {
      String desc = "Type Parameters: <";
      for (TypeVariable tv : tvs) {
        desc = desc.concat(tv.getName().concat(","));
      }
      desc = desc.substring(0, desc.length() - 1).concat(">");
      log.info(desc);
    } else {
      log.info("  -- No Type Parameters --");
    }
  }

  public void spyImplementedInterfaces() {
    Type[] interfaces = clazz.getGenericInterfaces();
    if (interfaces.length > 0) {
      for (Type intfce : interfaces) {
        log.info("Implemented Interfaces: {}", intfce.toString());
      }
    } else {
      log.info("  -- No Implemented Interfaces --");
    }
  }

  private void printClassInheritance(Class<?> clazz) {
    Class<?> parent = clazz.getSuperclass();
    if (parent != null) {
      log.info("Class Hierarchy: {} <: {}", clazz.getCanonicalName(), parent.getCanonicalName());
      printClassInheritance(parent);
    }
  }

  public void spyInheritance() {
    printClassInheritance(clazz);
  }

  public void spyAnnotations() {
    Annotation[] annotations = clazz.getAnnotations();
    if (annotations.length > 0) {
      for (Annotation annotation : annotations) {
        log.info("Annotation : {}", annotation.toString());
      }
    } else {
      log.info("  -- No Annotations --");
    }
  }

}
