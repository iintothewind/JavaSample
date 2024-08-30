package sample.reflect;


import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class ReflectionTest {

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
