package com.example;

import java.io.IOException;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONArray;
import org.json.JSONObject;

public class RouteFinder {

    private static String mapboxApiKey = "pk.eyJ1IjoidG9ybWFsb2siLCJhIjoiY2x6bDh2YzUyMDA2bDJrcXd1OWtoZGduNiJ9.PC_dfeirD6xIzI_TOy4LtQ";

    // List of coordinates of landmarks (latitude, longitude) and their names
    private static final List<Landmark> landmarks = new ArrayList<>();

    static {
        landmarks.add(new Landmark(5.65188, -0.18683, "Balme Library"));
        landmarks.add(new Landmark(5.65142, -0.18549, "Department of Physics"));
        landmarks.add(new Landmark(5.65450, -0.18368, "Department of Computer Science"));
        landmarks.add(new Landmark(5.64050, -0.16750, "Great Hall"));
        landmarks.add(new Landmark(5.63900, -0.16650, "University Square"));
        landmarks.add(new Landmark(5.64000, -0.16700, "Commonwealth Hall"));
        landmarks.add(new Landmark(5.64020, -0.16680, "Legon Hall"));
        landmarks.add(new Landmark(5.64080, -0.16730, "Mensah Sarbah Hall"));
        landmarks.add(new Landmark(5.64050, -0.16800, "Nkrumah Hall"));
        landmarks.add(new Landmark(5.63850, -0.16500, "University Hospital"));
        landmarks.add(new Landmark(5.63990, -0.16600, "Institute of African Studies"));
        landmarks.add(new Landmark(5.63800, -0.16400, "Noguchi Memorial Institute for Medical Research"));
        landmarks.add(new Landmark(5.63850, -0.16550, "School of Public Health"));
        landmarks.add(new Landmark(5.63930, -0.16640, "Department of Geography and Resource Development"));
        landmarks.add(new Landmark(5.63970, -0.16710, "Department of Psychology"));
        landmarks.add(new Landmark(5.63950, -0.16690, "Department of Sociology"));
        landmarks.add(new Landmark(5.63980, -0.16720, "Department of Political Science"));
        landmarks.add(new Landmark(5.63850, -0.16800, "Sports Fields"));
        landmarks.add(new Landmark(5.63920, -0.16780, "University Guest Centre"));
        landmarks.add(new Landmark(5.63800, -0.16850, "UG Sports Stadium"));
        landmarks.add(new Landmark(5.64000, -0.16800, "Botanical Gardens"));
        landmarks.add(new Landmark(5.63930, -0.16850, "Athletic Oval"));
        landmarks.add(new Landmark(5.63940, -0.16780, "Legon Pool Side"));
        landmarks.add(new Landmark(5.64960, -0.18720, "University of Ghana Business School"));
    }

    // Extract coordinates and names
    private static final List<double[]> coordinates = landmarks.stream()
            .map(landmark -> new double[]{landmark.getLatitude(), landmark.getLongitude()})
            .collect(Collectors.toList());

    private static final List<String> names = landmarks.stream()
            .map(Landmark::getName)
            .collect(Collectors.toList());

    // Convert coordinates to the required format for Mapbox
    private static final String locString = coordinates.stream()
            .map(coord -> String.format("%.6f,%.6f", coord[1], coord[0]))
            .collect(Collectors.joining(";"));

    // Function to make Mapbox API call for the matrix
    private static JSONObject getMapboxMatrix(String locations, String profile) throws IOException {
        String url = String.format("https://api.mapbox.com/directions-matrix/v1/mapbox/%s/%s?annotations=duration,distance&access_token=%s",
                profile, locations, mapboxApiKey);
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            return new JSONObject(content.toString());
        } else {
            throw new IOException("Error: " + responseCode + ", " + con.getResponseMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        // Get matrices for driving-car and walking
        JSONObject matrixCar = getMapboxMatrix(locString, "driving");
        JSONObject matrixWalking = getMapboxMatrix(locString, "walking");

        JSONArray distancesCar = matrixCar.getJSONArray("distances");
        JSONArray durationsCar = matrixCar.getJSONArray("durations");
        JSONArray distancesWalking = matrixWalking.getJSONArray("distances");
        JSONArray durationsWalking = matrixWalking.getJSONArray("durations");

        Graph G = new Graph();

        // Add nodes with positions and names
        for (int i = 0; i < coordinates.size(); i++) {
            G.addNode(i, coordinates.get(i), names.get(i)); // Use names directly
        }

        // Add edges with distances and durations as weights
        for (int i = 0; i < distancesCar.length(); i++) {
            for (int j = 0; j < distancesCar.getJSONArray(i).length(); j++) {
                if (i != j && !distancesCar.getJSONArray(i).isNull(j)) {
                    G.addEdge(i, j,
                            distancesCar.getJSONArray(i).getDouble(j) / 1000, // Convert meters to kilometers
                            durationsCar.getJSONArray(i).getDouble(j) / 60,  // Convert seconds to minutes
                            distancesWalking.getJSONArray(i).getDouble(j) / 1000,
                            durationsWalking.getJSONArray(i).getDouble(j) / 60);
                }
            }
        }

        // Interactive part for user input
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nWelcome to the Route Finder!");
            System.out.println("1. Find shortest path automatically");
            System.out.println("2. Find shortest path with specific waypoints");
            System.out.println("3. Quit");

            String choice = scanner.nextLine();
            if (choice.equals("1") || choice.equals("2")) {
                // Display all landmarks with their associated numbers
                System.out.println("\nLandmarks:");
                for (int i = 0; i < names.size(); i++) {
                    System.out.printf("Node %d: %s%n", i, names.get(i));
                }

                // Get user input for start and end locations
                try {
                    System.out.print("\nEnter the number for the start location: ");
                    int start = Integer.parseInt(scanner.nextLine());
                    System.out.print("Enter the number for the end location: ");
                    int end = Integer.parseInt(scanner.nextLine());
                    System.out.println("Select the mode of travel:");
                    System.out.println("1: Driving");
                    System.out.println("2: Walking");
                    int mode = Integer.parseInt(scanner.nextLine());

                    List<Integer> waypoints = new ArrayList<>();
                    if (choice.equals("2")) {
                        System.out.print("Enter the numbers of waypoints to include, separated by commas (e.g., 1,3,5): ");
                        String waypointsInput = scanner.nextLine();
                        if (!waypointsInput.isEmpty()) {
                            waypoints = Arrays.stream(waypointsInput.split(","))
                                    .map(Integer::parseInt)
                                    .collect(Collectors.toList());
                        }
                    }

                    // Find the shortest path
                    List<Integer> path = G.findShortestPathWithWaypoints(start, end, mode, waypoints);
                    double pathLength = G.getPathWeight(path, mode == 1 ? "durationCar" : "durationWalking");
                    double pathDistance = G.getPathDistance(path, mode == 1 ? "distanceCar" : "distanceWalking");

                    // Display the shortest path and its distance
                    System.out.printf("\nThe shortest path from Node %d to Node %d is:%n", start, end);
                    for (int node : path) {
                        System.out.printf("Node %d: %s%n", node, G.getNodeLabel(node));
                    }

                    String travelMode = mode == 1 ? "driving" : "walking";

                    int pathLengthMinutes = (int) Math.round(pathLength);
                    String timeStr;
                    if (pathLengthMinutes >= 60) {
                        int hours = pathLengthMinutes / 60;
                        int minutes = pathLengthMinutes % 60;
                        timeStr = String.format("%d hour(s) %d min(s)", hours, minutes);
                    } else {
                        timeStr = String.format("%d min(s)", pathLengthMinutes);
                    }

                    System.out.printf("Total distance: %.2f km%n", pathDistance);
                    System.out.printf("Total travel time: %s%n", timeStr);

                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                }
            } else if (choice.equals("3")) {
                System.out.println("Quitting the program. Have a great day!");
                break;
            } else {
                System.out.println("Invalid choice. Please choose 1, 2, or 3.");
            }
        }
    }
}

class Landmark {
    private final double latitude;
    private final double longitude;
    private final String name;

    public Landmark(double latitude, double longitude, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }
}

class Graph {

    private final Map<Integer, Node> nodes = new HashMap<>();
    private final Map<Integer, Map<Integer, Edge>> edges = new HashMap<>();

    public void addNode(int id, double[] coord, String label) {
        nodes.put(id, new Node(id, coord, label));
    }

    public void addEdge(int from, int to, double distanceCar, double durationCar, double distanceWalking, double durationWalking) {
        edges.computeIfAbsent(from, k -> new HashMap<>()).put(to, new Edge(distanceCar, durationCar, distanceWalking, durationWalking));
    }

    public String getNodeLabel(int nodeId) {
        return nodes.get(nodeId).label;
    }

    public List<Integer> findShortestPathWithWaypoints(int start, int end, int mode, List<Integer> waypoints) {
        List<Integer> allNodes = new ArrayList<>(List.of(start));
        allNodes.addAll(waypoints);
        allNodes.add(end);

        List<Integer> path = new ArrayList<>();
        for (int i = 0; i < allNodes.size() - 1; i++) {
            List<Integer> segment = findShortestPath(allNodes.get(i), allNodes.get(i + 1), mode == 1 ? "durationCar" : "durationWalking");
            if (i == 0) {
                path.addAll(segment);
            } else {
                path.addAll(segment.subList(1, segment.size())); // Avoid duplicating the starting node
            }
        }

        return path;
    }

    public List<Integer> findShortestPath(int start, int end, String weightKey) {
        return dijkstra(start, end, weightKey);
    }

    public double getPathWeight(List<Integer> path, String weightKey) {
        double totalWeight = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalWeight += edges.get(path.get(i)).get(path.get(i + 1)).getWeight(weightKey);
        }
        return totalWeight;
    }

    public double getPathDistance(List<Integer> path, String distanceKey) {
        double totalDistance = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalDistance += edges.get(path.get(i)).get(path.get(i + 1)).getDistance(distanceKey);
        }
        return totalDistance;
    }

    // Implementing Dijkstra's algorithm
    private List<Integer> dijkstra(int start, int end, String weightKey) {
        Map<Integer, Double> distances = new HashMap<>();
        Map<Integer, Integer> previous = new HashMap<>();
        PriorityQueue<Integer> queue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));

        for (Integer node : nodes.keySet()) {
            distances.put(node, Double.POSITIVE_INFINITY);
            previous.put(node, null);
            queue.add(node);
        }

        distances.put(start, 0.0);

        while (!queue.isEmpty()) {
            int current = queue.poll();

            if (current == end) {
                List<Integer> path = new ArrayList<>();
                for (Integer at = end; at != null; at = previous.get(at)) {
                    path.add(at);
                }
                Collections.reverse(path);
                return path;
            }

            if (distances.get(current) == Double.POSITIVE_INFINITY) break;

            for (Map.Entry<Integer, Edge> neighborEntry : edges.getOrDefault(current, Collections.emptyMap()).entrySet()) {
                int neighbor = neighborEntry.getKey();
                double newDist = distances.get(current) + neighborEntry.getValue().getWeight(weightKey);

                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    previous.put(neighbor, current);
                    queue.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return new ArrayList<>(); // Return an empty list if there is no path
    }
}

class Node {
    int id;
    double[] coord;
    String label;

    Node(int id, double[] coord, String label) {
        this.id = id;
        this.coord = coord;
        this.label = label;
    }
}

class Edge {
    double distanceCar;
    double durationCar;
    double distanceWalking;
    double durationWalking;

    Edge(double distanceCar, double durationCar, double distanceWalking, double durationWalking) {
        this.distanceCar = distanceCar;
        this.durationCar = durationCar;
        this.distanceWalking = distanceWalking;
        this.durationWalking = durationWalking;
    }

    public double getWeight(String key) {
        switch (key) {
            case "durationCar":
                return durationCar;
            case "durationWalking":
                return durationWalking;
            default:
                return 0.0;
        }
    }

    public double getDistance(String key) {
        switch (key) {
            case "distanceCar":
                return distanceCar;
            case "distanceWalking":
                return distanceWalking;
            default:
                return 0.0;
        }
    }
}