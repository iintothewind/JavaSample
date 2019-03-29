package sample.reflect;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class ReflectionTest {
  private final Logger log = LogManager.getLogger();
  private ClassDeclarationSpy spy = null;

  @Before
  public void setUp() {
    spy = new ClassDeclarationSpy(Class.class);
  }

  @Test
  public void testDeclaration() {
    spy.spyCanonicalName();
    spy.spyModifiers();
    spy.spyTypeParameters();
    spy.spyGenericString();
  }

  @Test
  public void testImplementedInterfaces() {
    spy.spyImplementedInterfaces();
  }

  @Test
  public void testSpyInheritance() {
    spy.spyInheritance();
  }

  @Test
  public void testSpyAnnotations() {
    spy.spyAnnotations();
  }
}
