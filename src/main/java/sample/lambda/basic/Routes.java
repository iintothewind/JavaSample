package sample.lambda.basic;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The local commuter railroad services a number of towns in Kiwiland.  Because of monetary concerns, all of the tracks are 'one-way.'  That is, a route from Kaitaia to Invercargill does not imply the existence of a route from Invercargill to Kaitaia.  In fact, even if both of these routes do happen to exist, they are distinct and are not necessarily the same distance!
 * <p>
 * The purpose of this problem is to help the railroad provide its customers with information about the routes.  In particular, you will compute the distance along a certain route, the number of different routes between two towns, and the shortest route between two towns.
 * <p>
 * Input:  A directed graph where a node represents a town and an edge represents a route between two towns.  The weighting of the edge represents the distance between the two towns.  A given route will never appear more than once, and for a given route, the starting and ending town will not be the same town.
 * <p>
 * Output: For test input 1 through 5, if no such route exists, output 'NO SUCH ROUTE'.  Otherwise, follow the route as given; do not make any extra stops!  For example, the first problem means to start at city A, then travel directly to city B (a distance of 5), then directly to city C (a distance of 4).
 * The distance of the route A-B-C.
 * The distance of the route A-D.
 * The distance of the route A-D-C.
 * The distance of the route A-E-B-C-D.
 * The distance of the route A-E-D.
 * The number of trips starting at C and ending at C with a maximum of 3 stops.  In the sample data below, there are two such trips: C-D-C (2 stops). and C-E-B-C (3 stops).
 * The number of trips starting at A and ending at C with exactly 4 stops.  In the sample data below, there are three such trips: A to C (via B,C,D); A to C (via D,C,D); and A to C (via D,E,B).
 * The length of the shortest route (in terms of distance to travel) from A to C.
 * The length of the shortest route (in terms of distance to travel) from B to B.
 * The number of different routes from C to C with a distance of less than 30.  In the sample data, the trips are: CDC, CEBC, CEBCDC, CDCEBC, CDEBC, CEBCEBC, CEBCEBCEBC.
 * <p>
 * Test Input:
 * For the test input, the towns are named using the first few letters of the alphabet from A to D.  A route between two towns (A to B) with a distance of 5 is represented as AB5.
 * Graph: AB5, BC4, CD8, DC8, DE6, AD5, CE2, EB3, AE7
 * Expected Output:
 * Output #1: 9
 * Output #2: 5
 * Output #3: 13
 * Output #4: 22
 * Output #5: NO SUCH ROUTE
 * Output #6: 2
 * Output #7: 3
 * Output #8: 9
 * Output #9: 9
 * Output #10: 7
 */
public class Routes {
  /**
   * the towns from given input
   */
  private final Set<String> nodes;
  /**
   * the stops and its distances from given input
   */
  private final Map<String, Integer> edges;

  /**
   * Given a string that contains some stops with its distance to construct Routes instance
   *
   * @param input a string match format as: "AB5,BC4,CD8,DC8,DE6,AD5,CE2,EB3,AE7"
   *              the input string must contain a list of 2 letters from A-Z, come with a number, separated by ","
   *              each stop cannot appear more than once.
   *              The nodes and edges will be empty if input string is not valid.
   */
  public Routes(String input) {
    List<String> list = Optional.ofNullable(input)
      .map(a -> Arrays.asList(a.split(",")))
      .filter(l -> l.stream().allMatch(s -> s.matches("[A-Z]{2}\\d")))
      .filter(l ->
        l.size() == l.stream()
          .map(s -> s.substring(0, 2))
          .distinct()
          .count())
      .filter(l ->
        l.stream()
          .noneMatch(s -> s.substring(0, 1).equals(s.substring(1, 2))))
      .orElse(new ArrayList<>());
    nodes = list.stream()
      .flatMap(s ->
        s.substring(0, 2)
          .chars()
          .mapToObj(c ->
            String.valueOf((char) c)))
      .collect(Collectors.toSet());
    edges = list.stream()
      .collect(Collectors.toMap(
        s -> s.substring(0, 2),
        s -> Integer.parseInt(s.substring(2))));
  }


  public Set<String> getNodes() {
    return nodes;
  }

  public Map<String, Integer> getEdges() {
    return edges;
  }

  /**
   * Given a string of some nodes as route, return a list of edges that cover all the nodes
   * Return empty list if given string is null or empty
   *
   * @param route a string of some nodes as route
   * @return a list of edges that cover all the nodes or empty list if given string is null or empty
   */
  public List<String> convertToStops(String route) {
    return Optional
      .ofNullable(route)
      .filter(s -> s.length() > 1)
      .map(r ->
        IntStream
          .range(0, r.length() - 1)
          .mapToObj(i -> r.substring(i, i + 2))
          .collect(Collectors.toList()))
      .orElse(new ArrayList<>());
  }

  /**
   * Given a string of some nodes as route, return true if route exists, or false if not
   *
   * @param route a string of some nodes as route
   * @return true if route exists, or false if not
   */
  public boolean existsRoute(String route) {
    return convertToStops(route).stream().allMatch(edges::containsKey);
  }

  /**
   * @param route a string of some nodes as route
   * @return the distance of the input route
   * @throws RuntimeException if given route not exists
   */
  public int routeDistance(String route) {
    return Optional
      .ofNullable(route)
      .filter(this::existsRoute)
      .map(r ->
        convertToStops(r).stream()
          .map(edges::get)
          .mapToInt(Integer::intValue)
          .sum())
      .orElseThrow(() -> new RuntimeException("no such route"));
  }

  /**
   * traverse from given start edges, always try to traverse as many edges as possible,
   * till none of the new route can match the predicate
   *
   * @param routes a string of some nodes as route
   * @return a list of routes that starts from given start edges
   */
  private List<String> traverse(Set<String> routes, BiPredicate<String, String> bp) {
    Set<String> tmpRoutes = new HashSet<>(routes);
    for (String route : routes) {
      for (String stop : edges.keySet().stream()
        .filter(a -> a.startsWith(route.substring(route.length() - 1)))
        .collect(Collectors.toSet())) {
        if (bp.test(route, stop)) {
          tmpRoutes.add(route.concat(stop.substring(1)));
        }
      }
    }
    if (tmpRoutes.size() > routes.size()) {
      return traverse(tmpRoutes, bp);
    } else {
      return tmpRoutes.stream()
        .sorted()
        .collect(Collectors.toList());
    }
  }

  /**
   * Given a start node, always try to traverse as many edges as possible, but make sure that each edge has only been traversed once in each route
   *
   * @param start the start node
   * @return a list of routes that starts from given start node, or empty list if input start node is null or empty, or none existing
   */
  public List<String> traverse(String start) {
    return Optional
      .ofNullable(start)
      .filter(nodes::contains)
      .map(b ->
        traverse(
          edges.keySet().stream()
            .filter(c -> c.startsWith(b))
            .collect(Collectors.toSet()),
          (route, stop) -> !route.contains(stop)))
      .orElse(new ArrayList<>());
  }

  /**
   * Given a start node, return all the routes with stops no more than given maxStops
   *
   * @param start    start node
   * @param maxStops the return routes should have stops less than or equal to max stops
   * @return a list of routes that starts from given start node, or empty list if input start node is null or empty, or none existing
   */
  public List<String> traverseWithMaxStops(String start, int maxStops) {
    return Optional.ofNullable(start)
      .filter(nodes::contains)
      .map(b -> traverse(
        edges.keySet().stream()
          .filter(c -> c.startsWith(b))
          .collect(Collectors.toSet()),
        (route, stop) ->
          convertToStops(route.concat(stop.substring(1)))
            .size() <= Optional.of(maxStops).filter(n -> n > 0).orElse(1)))
      .orElse(new ArrayList<>());
  }

  /**
   * Given a start node, return all the routes with stops no more than given maxDistance
   *
   * @param start       start node
   * @param maxDistance the return routes should have distance less than or equal to max distance
   * @return a list of routes that starts from given start node, or empty list if input start node is null or empty, or none existing
   */
  public List<String> traverseWithMaxDistance(String start, int maxDistance) {
    return Optional
      .ofNullable(start)
      .filter(nodes::contains)
      .map(b -> traverse(
        edges.keySet().stream()
          .filter(c -> c.startsWith(b))
          .collect(Collectors.toSet()),
        (route, stop) ->
          routeDistance(route.concat(stop.substring(1))) <= Optional.of(maxDistance).filter(n -> n > 0).orElse(1)))
      .orElse(new ArrayList<>());
  }

  /**
   * Given a list of routes, return the filtered routes according to given route predicate
   *
   * @param routes         input routes
   * @param routePredicate the route predicate
   * @return a list of routes that starts from given routes, or empty list if input routes is null or empty
   */
  public List<String> findRoutes(List<String> routes, Predicate<String> routePredicate) {
    return Optional
      .ofNullable(routes)
      .orElse(new ArrayList<>())
      .stream()
      .filter(routePredicate)
      .collect(Collectors.toList());
  }

  /**
   * Given a start node and an end node, return the distance of its shortest route
   *
   * @param start start node
   * @param end   end node
   * @return shortest distance
   * @throws RuntimeException if no route exists between given start node and end node
   */
  public int shortestDistance(String start, String end) {
    return findRoutes(traverse(start), r -> r.endsWith(end))
      .stream()
      .min(
        Comparator
          .comparingInt(r ->
            convertToStops(r)
              .stream()
              .map(a -> getEdges().get(a))
              .mapToInt(Integer::intValue)
              .sum()))
      .map(b -> convertToStops(b).stream()
        .map(c -> getEdges().get(c))
        .mapToInt(Integer::intValue)
        .sum())
      .orElseThrow(() -> new RuntimeException("no such route"));
  }

  public static void main(String[] argc) {
    Routes routes = new Routes("AB5,BC4,CD8,DC8,DE6,AD5,CE2,EB3,AE7");
    System.out.println("1. The distance of the route A-B-C.");
    System.out.println(routes.routeDistance("ABC"));
    System.out.println("2. The distance of the route A-D.");
    System.out.println(routes.routeDistance("AD"));
    System.out.println("3. The distance of the route A-D-C.");
    System.out.println(routes.routeDistance("ADC"));
    System.out.println("4. The distance of the route A-E-B-C-D.");
    System.out.println(routes.routeDistance("AEBCD"));
    System.out.println("5. The distance of the route A-E-D.");
    try {
      System.out.println(routes.routeDistance("AED"));
    } catch (RuntimeException e) {
      System.out.println(e.getMessage());
    }
    System.out.println("6. The number of trips starting at C and ending at C with a maximum of 3 stops.");
    System.out.println(routes.findRoutes(
      routes.traverseWithMaxStops("C", 3),
      r -> r.endsWith("C"))
      .stream()
      .map(s -> String.format("%s: %s stops", s, routes.convertToStops(s).size()))
      .collect(Collectors.toList())
      .size());
    System.out.println("7. The number of trips starting at A and ending at C with exactly 4 stops.");
    System.out.println(routes.findRoutes(
      routes.traverseWithMaxStops("A", 4),
      s -> s.endsWith("C") && routes.convertToStops(s).size() == 4)
      .stream()
      .map(s -> String.format("%s: %s stops", s, routes.convertToStops(s).size())).
        collect(Collectors.toList()).size());
    System.out.println("8. The length of the shortest route (in terms of distance to travel) from A to C.");
    System.out.println(routes.shortestDistance("A", "C"));
    System.out.println("9. The length of the shortest route (in terms of distance to travel) from B to B.");
    System.out.println(routes.shortestDistance("B", "B"));
    System.out.println("10.The number of different routes from C to C with a distance of less than 30.");
    System.out.println(routes.findRoutes(
      routes.traverseWithMaxDistance("C", 30),
      s -> s.endsWith("C") && routes.routeDistance(s) < 30).size());
  }
}
