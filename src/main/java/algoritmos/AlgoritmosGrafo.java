package algoritmos;

import modelo.Arista;
import modelo.Grafo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class AlgoritmosGrafo {

    public static List<Integer> bfs(Grafo grafo, int inicio) {
        List<Integer> ordenVisita = new ArrayList<>();
        if (!grafo.getVertices().contains(inicio)) {
            return ordenVisita;
        }

        Set<Integer> visitados = new HashSet<>();
        Queue<Integer> cola = new LinkedList<>();
        visitados.add(inicio);
        cola.add(inicio);

        while (!cola.isEmpty()) {
            int actual = cola.poll();
            ordenVisita.add(actual);

            for (Arista arista : grafo.getListaAdyacencia()
                    .getOrDefault(actual, Collections.emptyList())) {
                int vecino = arista.getDestino();
                if (visitados.add(vecino)) {
                    cola.add(vecino);
                }
            }
        }

        return ordenVisita;
    }

    public static List<Integer> dfs(Grafo grafo, int inicio) {
        List<Integer> ordenVisita = new ArrayList<>();
        if (!grafo.getVertices().contains(inicio)) {
            return ordenVisita;
        }

        Set<Integer> visitados = new HashSet<>();
        dfsRecursivo(grafo, inicio, visitados, ordenVisita);
        return ordenVisita;
    }

    private static void dfsRecursivo(
            Grafo grafo,
            int actual,
            Set<Integer> visitados,
            List<Integer> ordenVisita) {
        visitados.add(actual);
        ordenVisita.add(actual);

        for (Arista arista : grafo.getListaAdyacencia()
                .getOrDefault(actual, Collections.emptyList())) {
            int vecino = arista.getDestino();
            if (!visitados.contains(vecino)) {
                dfsRecursivo(grafo, vecino, visitados, ordenVisita);
            }
        }
    }

    public static ResultadoDijkstra dijkstra(Grafo grafo, int origen, int destino) {
        if (!grafo.getVertices().contains(origen) || !grafo.getVertices().contains(destino)) {
            return new ResultadoDijkstra(Collections.emptyList(), Double.POSITIVE_INFINITY);
        }

        for (List<Arista> aristas : grafo.getListaAdyacencia().values()) {
            for (Arista arista : aristas) {
                if (!Double.isFinite(arista.getPeso()) || arista.getPeso() < 0) {
                    throw new IllegalArgumentException(
                            "Dijkstra requiere pesos finitos no negativos.");
                }
            }
        }

        Map<Integer, Double> distancias = new HashMap<>();
        Map<Integer, Integer> previos = new HashMap<>();
        PriorityQueue<NodoDistancia> pendientes = new PriorityQueue<>(
                Comparator.comparingDouble((NodoDistancia nodo) -> nodo.distancia)
                        .thenComparingInt(nodo -> nodo.vertice));

        for (int vertice : grafo.getVertices()) {
            distancias.put(vertice, Double.POSITIVE_INFINITY);
        }
        distancias.put(origen, 0.0);
        pendientes.add(new NodoDistancia(origen, 0.0));

        while (!pendientes.isEmpty()) {
            NodoDistancia actual = pendientes.poll();
            if (actual.distancia > distancias.get(actual.vertice)) {
                continue;
            }
            if (actual.vertice == destino) {
                break;
            }

            for (Arista arista : grafo.getListaAdyacencia()
                    .getOrDefault(actual.vertice, Collections.emptyList())) {
                int vecino = arista.getDestino();
                double nuevaDistancia = actual.distancia + arista.getPeso();

                if (nuevaDistancia < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDistancia);
                    previos.put(vecino, actual.vertice);
                    pendientes.add(new NodoDistancia(vecino, nuevaDistancia));
                }
            }
        }

        double costoTotal = distancias.get(destino);
        if (costoTotal == Double.POSITIVE_INFINITY) {
            return new ResultadoDijkstra(Collections.emptyList(), Double.POSITIVE_INFINITY);
        }

        List<Integer> ruta = new ArrayList<>();
        for (Integer vertice = destino; vertice != null; vertice = previos.get(vertice)) {
            ruta.add(vertice);
        }
        Collections.reverse(ruta);
        return new ResultadoDijkstra(ruta, costoTotal);
    }

    private static class NodoDistancia {
        private final int vertice;
        private final double distancia;

        private NodoDistancia(int vertice, double distancia) {
            this.vertice = vertice;
            this.distancia = distancia;
        }
    }

    public static class ResultadoDijkstra {
        public final List<Integer> ruta;
        public final double costoTotal;

        public ResultadoDijkstra(List<Integer> ruta, double costoTotal) {
            this.ruta = ruta;
            this.costoTotal = costoTotal;
        }
    }
}
