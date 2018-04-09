package sample.lambda.basic;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.HashSet;

public class RoutesTest {
  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testNullOrEmptyInput() {
    Assert.assertEquals(new HashSet<>(), new Routes(null).getNodes());
    Assert.assertEquals(new HashMap<>(), new Routes(null).getEdges());
    Assert.assertEquals(new HashSet<>(), new Routes("").getNodes());
    Assert.assertEquals(new HashMap<>(), new Routes("").getEdges());
  }

  @Test
  public void testInvalidInput() {
    Assert.assertEquals(new HashSet<>(), new Routes("a").getNodes());
    Assert.assertEquals(new HashMap<>(), new Routes("a").getEdges());
    Assert.assertEquals(new HashSet<>(), new Routes("ab").getNodes());
    Assert.assertEquals(new HashMap<>(), new Routes("ab").getEdges());
    Assert.assertEquals(new HashSet<>(), new Routes("ab1").getNodes());
    Assert.assertEquals(new HashMap<>(), new Routes("ab1").getEdges());
    Assert.assertEquals(new HashSet<>(), new Routes("abc").getNodes());
    Assert.assertEquals(new HashMap<>(), new Routes("abc").getEdges());
    Assert.assertEquals(new HashSet<>(), new Routes("AB").getNodes());
    Assert.assertEquals(new HashMap<>(), new Routes("AB").getEdges());
    Assert.assertEquals(new HashSet<>(), new Routes("ABC").getNodes());
    Assert.assertEquals(new HashMap<>(), new Routes("ABC").getEdges());
    Assert.assertEquals(new HashSet<>(), new Routes("AB1,BC1.,Bc3").getNodes());
    Assert.assertEquals(new HashMap<>(), new Routes("AB1,BC1.,Bc3").getEdges());
  }

  @Test
  public void testDuplicatedRoutes() {
    Assert.assertEquals(new HashSet<>(), new Routes("AB1,AB1").getNodes());
    Assert.assertEquals(new HashMap<>(), new Routes("AB1,AB1").getEdges());
  }

  @Test
  public void testStopWithSameStartEnd() {
    Assert.assertEquals(new HashSet<>(), new Routes("AA1,AB1").getNodes());
    Assert.assertEquals(new HashMap<>(), new Routes("AA1,AB1").getEdges());
  }

  @Test
  public void testExistRoute() {
    Assert.assertFalse(new Routes("").existsRoute("ABC"));
    Assert.assertTrue(new Routes("AB1,BC2,CD3").existsRoute("ABC"));
  }

  @Test
  public void testRouteDistance() {
    Assert.assertEquals(3, new Routes("AB1,BC2,CD3").routeDistance("ABC"));
    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage("no such route");
    Assert.assertEquals(0, new Routes("").routeDistance("ABC"));
  }

  @Test
  public void testPositive() {
    Routes routes = new Routes("AB5,BC4,CD8,DC8,DE6,AD5,CE2,EB3,AE7");
    Assert.assertEquals(9, routes.routeDistance("ABC"));
    Assert.assertEquals(5, routes.routeDistance("AD"));
    Assert.assertEquals(13, routes.routeDistance("ADC"));
    Assert.assertEquals(22, routes.routeDistance("AEBCD"));
    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage("no such route");
    Assert.assertEquals(0, routes.routeDistance("AED"));
  }

  @Ignore
  @Test
  public void testThrow() {
    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage("");
    new Routes("AB5,BC4,CD8,DC8,DE6,AD5,CE2,EB3,AE7").routeDistance("AEBCD");
  }

  @Test
  public void testTraverse() {
    Routes routes = new Routes("AB5,BC4,CD8,DC8,DE6,AD5,CE2,EB3,AE7");
    System.out.println(routes.traverse("A"));
  }

  @Test
  public void testFindRoutes() {
    Routes routes = new Routes("AB5,BC4,CD8,DC8,DE6,AD5,CE2,EB3,AE7");
    System.out.println(routes.traverse("A"));
    System.out.println(routes.findRoutes("C", r -> r.endsWith("C") && r.length() <= 3));
    System.out.println(routes.findRoutes("A", r -> r.endsWith("C") && routes.convertToStops(r).size() == 4));
  }

  @Test
  public void testShortestRoutes() {
    Routes routes = new Routes("AB5,BC4,CD8,DC8,DE6,AD5,CE2,EB3,AE7");
    Assert.assertEquals(9, routes.shortestDistance("A", "C"));
    Assert.assertEquals(9, routes.shortestDistance("B", "B"));
  }
}
