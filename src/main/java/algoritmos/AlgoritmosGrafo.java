package algoritmos;

import modelo.Arista;
import modelo.Grafo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
}