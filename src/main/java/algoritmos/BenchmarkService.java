package algoritmos;

import io.GestorCSV;
import modelo.Arista;
import modelo.Grafo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Ejecuta mediciones comparables sobre los archivos CSV disponibles en data/. */
public final class BenchmarkService {
    private static final int REPETICIONES = 5;
    private static final int CALENTAMIENTOS = 2;
    private static volatile Object resultadoDescartable;

    private BenchmarkService() {
    }

    public static List<Resultado> ejecutar(File carpeta, boolean dirigido) throws IOException {
        File[] archivos = carpeta.listFiles((dir, nombre) -> nombre.toLowerCase().endsWith(".csv"));
        if (archivos == null || archivos.length == 0) {
            throw new IOException("No se encontraron archivos CSV en " + carpeta.getPath());
        }
        Arrays.sort(archivos, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));

        List<Resultado> resultados = new ArrayList<>();
        for (File archivo : archivos) {
            Grafo referencia = new Grafo(dirigido);
            GestorCSV.importarCSV(archivo, referencia);
            List<Integer> vertices = new ArrayList<>(referencia.getVertices());
            List<Arista> aristas = referencia.obtenerTodasLasAristas();

            double importacionMs = medirMediana(() -> {
                Grafo cargado = new Grafo(dirigido);
                try {
                    GestorCSV.importarCSV(archivo, cargado);
                    resultadoDescartable = cargado;
                } catch (IOException e) {
                    throw new BenchmarkException(e);
                }
            });

            double construccionMs = medirMediana(() ->
                    resultadoDescartable = construirGrafo(vertices, aristas, dirigido));

            if (vertices.isEmpty()) {
                resultados.add(new Resultado(archivo.getName(), 0, 0, importacionMs,
                        construccionMs, 0, 0, 0, 0, 0, 0, 0));
                continue;
            }

            int inicio = vertices.get(0);
            int destino = vertices.get(vertices.size() - 1);
            double bfsMs = medirMediana(() ->
                    resultadoDescartable = AlgoritmosGrafo.bfs(referencia, inicio));
            double dfsMs = medirMediana(() ->
                    resultadoDescartable = AlgoritmosGrafo.dfs(referencia, inicio));
            double dijkstraMs = medirMediana(() ->
                    resultadoDescartable = AlgoritmosGrafo.dijkstra(referencia, inicio, destino));
            double quickSortPesoMs = medirOrdenamiento(aristas, true, Ordenamiento.Criterio.PESO);
            double shellSortPesoMs = medirOrdenamiento(aristas, false, Ordenamiento.Criterio.PESO);
            double quickSortVerticeMs = medirOrdenamiento(aristas, true, Ordenamiento.Criterio.VERTICE_ORIGEN);
            double shellSortVerticeMs = medirOrdenamiento(aristas, false, Ordenamiento.Criterio.VERTICE_ORIGEN);

            resultados.add(new Resultado(archivo.getName(), vertices.size(), aristas.size(),
                    importacionMs, construccionMs, bfsMs, dfsMs, dijkstraMs,
                    quickSortPesoMs, shellSortPesoMs, quickSortVerticeMs, shellSortVerticeMs));
        }
        return resultados;
    }

    private static Grafo construirGrafo(List<Integer> vertices, List<Arista> aristas, boolean dirigido) {
        Grafo nuevo = new Grafo(dirigido);
        for (int vertice : vertices) nuevo.agregarVertice(vertice);
        for (Arista arista : aristas) {
            nuevo.agregarArista(arista.getOrigen(), arista.getDestino(), arista.getPeso());
        }
        return nuevo;
    }

    private static double medirOrdenamiento(List<Arista> aristas, boolean quickSort,
                                            Ordenamiento.Criterio criterio) {
        long[] muestras = new long[REPETICIONES];
        for (int i = -CALENTAMIENTOS; i < REPETICIONES; i++) {
            List<Arista> copia = new ArrayList<>(aristas);
            long inicio = System.nanoTime();
            if (quickSort) {
                Ordenamiento.quickSort(copia, criterio);
            } else {
                Ordenamiento.shellSort(copia, criterio);
            }
            long duracion = System.nanoTime() - inicio;
            resultadoDescartable = copia;
            if (i >= 0) muestras[i] = duracion;
        }
        return medianaMs(muestras);
    }

    private static double medirMediana(Runnable operacion) {
        for (int i = 0; i < CALENTAMIENTOS; i++) operacion.run();
        long[] muestras = new long[REPETICIONES];
        for (int i = 0; i < REPETICIONES; i++) {
            long inicio = System.nanoTime();
            operacion.run();
            muestras[i] = System.nanoTime() - inicio;
        }
        return medianaMs(muestras);
    }

    private static double medianaMs(long[] muestras) {
        Arrays.sort(muestras);
        return muestras[muestras.length / 2] / 1_000_000.0;
    }

    public static final class Resultado {
        public final String archivo;
        public final int vertices;
        public final int aristas;
        public final double importacionMs;
        public final double construccionMs;
        public final double bfsMs;
        public final double dfsMs;
        public final double dijkstraMs;
        public final double quickSortPesoMs;
        public final double shellSortPesoMs;
        public final double quickSortVerticeMs;
        public final double shellSortVerticeMs;

        private Resultado(String archivo, int vertices, int aristas, double importacionMs,
                          double construccionMs, double bfsMs, double dfsMs, double dijkstraMs,
                          double quickSortPesoMs, double shellSortPesoMs,
                          double quickSortVerticeMs, double shellSortVerticeMs) {
            this.archivo = archivo;
            this.vertices = vertices;
            this.aristas = aristas;
            this.importacionMs = importacionMs;
            this.construccionMs = construccionMs;
            this.bfsMs = bfsMs;
            this.dfsMs = dfsMs;
            this.dijkstraMs = dijkstraMs;
            this.quickSortPesoMs = quickSortPesoMs;
            this.shellSortPesoMs = shellSortPesoMs;
            this.quickSortVerticeMs = quickSortVerticeMs;
            this.shellSortVerticeMs = shellSortVerticeMs;
        }
    }

    private static final class BenchmarkException extends RuntimeException {
        private BenchmarkException(IOException cause) {
            super(cause.getMessage(), cause);
        }
    }
}
