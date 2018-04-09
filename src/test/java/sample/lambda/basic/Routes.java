package sample.lambda.basic;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Routes {
  private final Set<String> nodes;
  private final Map<String, Integer> edges;

  public Routes(String input) {
    List<String> list = Optional.ofNullable(input)
      .map(a -> Arrays.asList(a.split(",")))
      .filter(l -> l.stream().allMatch(s -> s.matches("[A-Z]{2}\\d")))
      .filter(l -> l.size() == l.stream().map(s -> s.substring(0, 2)).distinct().count())
      .filter(l -> l.stream().noneMatch(s -> s.substring(0, 1).equals(s.substring(1, 2))))
      .orElse(new ArrayList<>());
    nodes = list.stream().flatMap(s -> s.substring(0, 2).chars().mapToObj(c -> String.valueOf((char) c))).collect(Collectors.toSet());
    edges = list.stream().collect(Collectors.toMap(s -> s.substring(0, 2), s -> Integer.parseInt(s.substring(2))));
  }


  public Set<String> getNodes() {
    return nodes;
  }

  public Map<String, Integer> getEdges() {
    return edges;
  }

  public List<String> convertToStops(String route) {
    return Optional.ofNullable(route).filter(s -> s.length() > 1)
      .map(r -> IntStream.range(0, r.length() - 1).mapToObj(i -> r.substring(i, i + 2)).collect(Collectors.toList()))
      .orElse(new ArrayList<>());
  }

  public boolean existsRoute(String route) {
    return convertToStops(route).stream().allMatch(edges::containsKey);
  }

  public int routeDistance(String route) {
    return Optional.ofNullable(route)
      .filter(this::existsRoute)
      .map(r -> convertToStops(r).stream().map(edges::get).mapToInt(Integer::intValue).sum())
      .orElseThrow(() -> new RuntimeException("no such route"));
  }

  private List<String> traverse(Set<String> routes) {
    Set<String> tmpRoutes = new HashSet<>(routes);
    for (String route : routes) {
      for (String stop : edges.keySet().stream().filter(a -> a.startsWith(route.substring(route.length() - 1))).collect(Collectors.toSet())) {
        if (!route.contains(stop)) {
          tmpRoutes.add(route.concat(stop.substring(1)));
        }
      }
    }
    if (tmpRoutes.size() > routes.size()) {
      return traverse(tmpRoutes);
    } else {
      return tmpRoutes.stream().sorted().collect(Collectors.toList());
    }
  }

  public List<String> traverse(String start) {
    return Optional.ofNullable(start)
      .filter(nodes::contains)
      .map(b -> traverse(edges.keySet().stream().filter(c -> c.startsWith(b)).collect(Collectors.toSet())))
      .orElse(new ArrayList<>());
  }

  public List<String> findRoutes(String start, Predicate<String> routePredicate) {
    return traverse(start).stream().filter(routePredicate).collect(Collectors.toList());
  }

  public int shortestDistance(String start, String end) {
    return findRoutes(start, r -> r.endsWith(end))
      .stream()
      .min(Comparator.comparingInt(r -> convertToStops(r).stream().map(a -> getEdges().get(a)).mapToInt(Integer::intValue).sum()))
      .map(b -> convertToStops(b).stream().map(c -> getEdges().get(c)).mapToInt(Integer::intValue).sum())
      .orElseThrow(() -> new RuntimeException("no such route"));
  }


}
