package modelo;

import java.util.*;

public class Grafo {
    private boolean esDirigido;
    private Set<Integer> vertices;
    private Map<Integer, List<Arista>> listaAdyacencia;

    public Grafo(boolean esDirigido) {
        this.esDirigido = esDirigido;
        this.vertices = new TreeSet<>();
        this.listaAdyacencia = new HashMap<>();
    }

    public boolean isEsDirigido() { return esDirigido; }
    public void setEsDirigido(boolean esDirigido) { this.esDirigido = esDirigido; }

    public Set<Integer> getVertices() { return vertices; }
    public Map<Integer, List<Arista>> getListaAdyacencia() { return listaAdyacencia; }

    public void agregarVertice(int v) {
        if (v < 0) throw new IllegalArgumentException("El vértice debe ser un entero no negativo.");
        vertices.add(v);
        listaAdyacencia.putIfAbsent(v, new ArrayList<>());
    }
}