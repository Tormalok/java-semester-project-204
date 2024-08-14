# Route Finder Documentation

## Overview

The Route Finder is a Java program designed to calculate the shortest path between various landmarks on the University of Ghana campus. It utilizes the Mapbox API to retrieve distance and duration matrices for both driving and walking routes. The program then uses these matrices to build a graph and find the shortest path based on user inputs.

The program uses several algorithms and techniques to achieve the desired functionality. Below is a list of these algorithms, along with an explanation of their techniques and how they contribute to the program's efficiency:

### 1. Dijkstra's Algorithm

- **Technique Used**: Dijkstra's algorithm is employed to find the shortest paths between nodes in a graph. This algorithm is effective for graphs with non-negative weights and ensures that the shortest path is found efficiently.

- **Usage in Program**: In the `Graph` class, the `dijkstra` method implements Dijkstra's algorithm to find the shortest path based on the travel mode (driving or walking). The weights used for the graph are `durationCar` for driving and `durationWalking` for walking.

- **Efficiency Contribution**: Dijkstra's algorithm offers a time complexity of \(O((V + E) \log V)\), where \(V\) is the number of vertices and \(E\) is the number of edges. This ensures that the shortest path is found efficiently, even in relatively large graphs.

### 2. Graph Construction and Edge Weighting

- **Technique Used**: The program constructs a graph where nodes represent landmarks and edges represent the paths between them. Each edge is weighted based on travel distance and duration, corresponding to the selected mode of transportation.

- **Usage in Program**: In the `RouteFinder` class, the graph is constructed using the `Graph` class. Nodes are added with their positions and names, and edges are added with weights for driving and walking modes (`distanceCar`, `durationCar`, `distanceWalking`, `durationWalking`).

- **Efficiency Contribution**: Accurate graph construction ensures that shortest path algorithms, like Dijkstra's, operate efficiently. Proper edge weighting enables the program to reliably find the shortest and most appropriate routes based on the selected travel mode.

### 3. API Integration

- **Technique Used**: The program integrates with the Mapbox API to retrieve up-to-date distance and duration data between landmarks. This offloads complex geographical calculations to the API and ensures accuracy.

- **Usage in Program**: The `getMapboxMatrix` method sends a request to the Mapbox API, which returns matrices of distances and durations for the specified landmarks and travel mode. This data is then used to weight the edges of the graph.

- **Efficiency Contribution**: By leveraging the Mapbox API, the program ensures accurate and current data for route calculations. This approach also improves efficiency by delegating complex computations to a specialized service.

### 4. Interactive User Interface

- **Technique Used**: The program employs a command-line interface to interact with the user. This interface guides the user through selecting the start and end points, the travel mode, and any waypoints.

- **Usage in Program**: The interactive part of the program is handled in the `main()` method, where the user is prompted to make selections and input data. The program then processes this input to find and display the corresponding route.

- **Efficiency Contribution**: The interactive interface ensures that users can easily provide input and receive results without needing to understand the underlying code. This improves the overall user experience and makes the program more accessible to non-technical users.


## Packages and Imports

```java
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
import io.github.cdimascio.dotenv.Dotenv;
```
## RouteFinder Class

The `RouteFinder` class is the main class of the program, responsible for initializing landmarks, querying the Mapbox API, and providing an interactive user interface.

### Fields

- **`mapboxApiKey`**: The API key for accessing the Mapbox Directions Matrix API.

- **`landmarks`**: A static list of predefined landmarks, each with its coordinates (latitude and longitude) and name.

- **`coordinates`**: A list of landmark coordinates formatted for API requests. This is used to construct the location string required by the Mapbox API.

- **`names`**: A list of landmark names, used for labeling nodes in the graph.

### Methods

- **`getMapboxMatrix(String locations, String profile)`**: Makes an HTTP GET request to the Mapbox API to retrieve the distance and duration matrix for the given locations and travel profile (driving or walking).

    - **Parameters**:
        - `locations`: A string of coordinates formatted for the Mapbox API.
        - `profile`: The travel mode (e.g., "driving" or "walking").

    - **Returns**: A `JSONObject` containing the distance and duration matrices.

- **`main(String[] args)`**: The entry point of the program. It handles user interaction, retrieves data from the Mapbox API, builds a graph of landmarks, and calculates the shortest path based on user input.

    - **Parameters**:
        - `args`: Command-line arguments (not used in this implementation).

    - **Functionality**:
        - Initializes landmarks and their coordinates.
        - Retrieves distance and duration data from the Mapbox API.
        - Constructs a graph with nodes (landmarks) and edges (paths between landmarks).
        - Provides an interactive command-line interface for users to select start and end points, travel mode, and waypoints.
        - Calculates and displays the shortest path and its distance and duration based on the user’s input.

## Landmark Class

Represents a landmark with geographic coordinates and a name.

### Fields

- **`latitude`**: Latitude of the landmark.

- **`longitude`**: Longitude of the landmark.

- **`name`**: Name of the landmark.

### Methods

- **`getLatitude()`**: Returns the latitude of the landmark.

- **`getLongitude()`**: Returns the longitude of the landmark.

- **`getName()`**: Returns the name of the landmark.

## Graph Class

Represents a graph of nodes (landmarks) and edges (connections between landmarks with distances and durations).

### Fields

- **`nodes`**: A map of node IDs to `Node` objects.

- **`edges`**: A map of node IDs to maps of adjacent nodes and `Edge` objects.

### Methods

- **`addNode(int id, double[] coord, String label)`**: Adds a node to the graph.

    - **Parameters**:
        - `id`: The ID of the node.
        - `coord`: Coordinates of the node.
        - `label`: Label of the node.

- **`addEdge(int from, int to, double distanceCar, double durationCar, double distanceWalking, double durationWalking)`**: Adds an edge between two nodes with specified distances and durations.

    - **Parameters**:
        - `from`: The ID of the starting node.
        - `to`: The ID of the ending node.
        - `distanceCar`: Distance for car travel (in kilometers).
        - `durationCar`: Duration for car travel (in minutes).
        - `distanceWalking`: Distance for walking (in kilometers).
        - `durationWalking`: Duration for walking (in minutes).

- **`getNodeLabel(int nodeId)`**: Retrieves the label of a node.

    - **Parameters**:
        - `nodeId`: The ID of the node.

    - **Returns**: The label of the node.

- **`findShortestPathWithWaypoints(int start, int end, int mode, List<Integer> waypoints)`**: Finds the shortest path from a start node to an end node, optionally including waypoints.

    - **Parameters**:
        - `start`: The ID of the start node.
        - `end`: The ID of the end node.
        - `mode`: The mode of travel (1 for driving, 2 for walking).
        - `waypoints`: A list of waypoints to include in the path.

    - **Returns**: A list of node IDs representing the shortest path.

- **`findShortestPath(int start, int end, String weightKey)`**: Finds the shortest path between two nodes based on a weight key.

    - **Parameters**:
        - `start`: The ID of the start node.
        - `end`: The ID of the end node.
        - `weightKey`: The key for the weight to be used (e.g., "durationCar" or "durationWalking").

    - **Returns**: A list of node IDs representing the shortest path.

- **`getPathWeight(List<Integer> path, String weightKey)`**: Calculates the total weight of a given path.

    - **Parameters**:
        - `path`: A list of node IDs representing the path.
        - `weightKey`: The key for the weight to be used (e.g., "durationCar" or "durationWalking").

    - **Returns**: The total weight of the path.

- **`getPathDistance(List<Integer> path, String distanceKey)`**: Calculates the total distance of a given path.

    - **Parameters**:
        - `path`: A list of node IDs representing the path.
        - `distanceKey`: The key for the distance to be used (e.g., "distanceCar" or "distanceWalking").

    - **Returns**: The total distance of the path.

## Node Class

Represents a node in the graph.

### Fields

- **`id`**: The ID of the node.

- **`coord`**: Coordinates of the node.

- **`label`**: Label of the node.

## Edge Class

Represents an edge between two nodes with associated distances and durations.

### Fields

- **`distanceCar`**: Distance for car travel (in kilometers).

- **`durationCar`**: Duration for car travel (in minutes).

- **`distanceWalking`**: Distance for walking (in kilometers).

- **`durationWalking`**: Duration for walking (in minutes).

### Methods

- **`getWeight(String key)`**: Returns the weight (duration) based on the provided key.

    - **Parameters**:
        - `key`: The key for the weight to be retrieved (e.g., "durationCar" or "durationWalking").

    - **Returns**: The weight of the edge based on the key.

- **`getDistance(String key)`**: Returns the distance based on the provided key.

    - **Parameters**:
        - `key`: The key for the distance to be retrieved (e.g., "distanceCar" or "distanceWalking").

    - **Returns**: The distance of the edge based on the key.

## Usage

### Run the Program

Execute the `RouteFinder` class to start the program.

### Choose an Option

When prompted, choose one of the following options:

1. **Find the shortest path automatically.**
2. **Find the shortest path with specific waypoints.**
3. **Quit the program.**

### Provide Inputs

For options 1 and 2:

- **Select Start and End Locations**: Choose the starting and ending landmarks from the available list.

- **Choose Mode of Travel**: Select the mode of travel:
    - `1` for driving
    - `2` for walking

- **Option 2 (Shortest Path with Specific Waypoints)**:
    - Optionally, specify waypoints to be included in the route.

### View Results

The program will display:

- The shortest path from the start location to the end location.
- The total distance of the route.
- The estimated travel time.

### Example

To find the shortest path from "Balme Library" to "University Square" while walking:

1. Select option `2` (shortest path with specific waypoints).
2. Enter the node numbers for "Balme Library" and "University Square".
3. Choose `2` for walking mode.
4. Optionally, enter waypoints if desired.
5. View the results displayed on the console.
