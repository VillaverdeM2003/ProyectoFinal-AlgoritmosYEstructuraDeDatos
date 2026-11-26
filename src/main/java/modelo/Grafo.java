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

    public boolean isEsDirigido() {
        return esDirigido;
    }
    public void setEsDirigido(boolean esDirigido) {
        this.esDirigido = esDirigido;
    }

    public Set<Integer> getVertices() {
        return vertices;
    }
    public Map<Integer, List<Arista>> getListaAdyacencia() {
        return listaAdyacencia;
    }

    public void agregarVertice(int v) {
        if (v < 0) throw new IllegalArgumentException("El vértice debe ser un entero no negativo.");
        vertices.add(v);
        listaAdyacencia.putIfAbsent(v, new ArrayList<>());
    }
    public boolean agregarArista(int origen, int destino, double peso) {
        if (origen < 0 || destino < 0) {
            throw new IllegalArgumentException("Los vértices deben ser enteros no negativos.");
        }
        if (origen == destino) {
            throw new IllegalArgumentException("No se permiten bucles (origen y destino no pueden ser iguales).");
        }
        agregarVertice(origen);
        agregarVertice(destino);

        boolean esNueva = actualizarOAgregar(origen, destino, peso);
        if (!esDirigido) {
            actualizarOAgregar(destino, origen, peso);
        }
        return esNueva;
    }

    private boolean actualizarOAgregar(int u, int v, double peso) {
        List<Arista> adyacentes = listaAdyacencia.get(u);
        for (Arista a : adyacentes) {
            if (a.getDestino() == v) {
                a.setPeso(peso);
                return false;
            }
        }
        adyacentes.add(new Arista(u, v, peso));
        return true;
    }

    public void eliminarArista(int origen, int destino) {
        if (listaAdyacencia.containsKey(origen)) {
            listaAdyacencia.get(origen).removeIf(a -> a.getDestino() == destino);
        }
        if (!esDirigido && listaAdyacencia.containsKey(destino)) {
            listaAdyacencia.get(destino).removeIf(a -> a.getDestino() == origen);
        }
    }

    public void eliminarVertice(int v) {
        if (!vertices.contains(v)) return;
        vertices.remove(v);
        listaAdyacencia.remove(v);
        for (List<Arista> adyacentes : listaAdyacencia.values()) {
            adyacentes.removeIf(a -> a.getDestino() == v);
        }
    }

    public List<Arista> obtenerTodasLasAristas() {
        List<Arista> todas = new ArrayList<>();
        Set<String> registradas = new HashSet<>();

        for (int u : vertices) {
            for (Arista a : listaAdyacencia.getOrDefault(u, Collections.emptyList())) {
                if (esDirigido) {
                    todas.add(a);
                } else {
                    int min = Math.min(a.getOrigen(), a.getDestino());
                    int max = Math.max(a.getOrigen(), a.getDestino());
                    String clave = min + "-" + max;
                    if (registradas.add(clave)) {
                        todas.add(a);
                    }
                }
            }
        }
        return todas;
    }

    public double[][] obtenerMatrizAdyacencia() {
        List<Integer> listaVertices = new ArrayList<>(vertices);
        int n = listaVertices.size();
        double[][] matriz = new double[n][n];

        for (int i = 0; i < n; i++) {
            Arrays.fill(matriz[i], Double.POSITIVE_INFINITY);
            matriz[i][i] = 0.0;
        }

        Map<Integer, Integer> indice = new HashMap<>();
        for (int i = 0; i < n; i++) {
            indice.put(listaVertices.get(i), i);
        }

        for (int u : listaVertices) {
            int i = indice.get(u);
            for (Arista a : listaAdyacencia.getOrDefault(u, Collections.emptyList())) {
                Integer j = indice.get(a.getDestino());
                if (j != null) {
                    matriz[i][j] = a.getPeso();
                }
            }
        }
        return matriz;
    }

    public void limpiar() {
        vertices.clear();
        listaAdyacencia.clear();
    }
}
