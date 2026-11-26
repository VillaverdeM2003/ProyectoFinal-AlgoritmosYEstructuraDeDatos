package algoritmos;

import modelo.Arista;

import java.util.List;
import java.util.Objects;

public class Ordenamiento {

    public enum Criterio {
        PESO,
        VERTICE_ORIGEN
    }

    public static void quickSort(List<Arista> lista, Criterio criterio) {
        if (lista == null || lista.size() <= 1) {
            return;
        }
        Objects.requireNonNull(criterio, "El criterio de ordenamiento es obligatorio.");
        quickSortRecursivo(lista, 0, lista.size() - 1, criterio);
    }

    private static void quickSortRecursivo(
            List<Arista> lista,
            int bajo,
            int alto,
            Criterio criterio) {
        if (bajo < alto) {
            int indicePivote = particion(lista, bajo, alto, criterio);
            quickSortRecursivo(lista, bajo, indicePivote - 1, criterio);
            quickSortRecursivo(lista, indicePivote + 1, alto, criterio);
        }
    }

    private static int particion(
            List<Arista> lista,
            int bajo,
            int alto,
            Criterio criterio) {
        Arista pivote = lista.get(alto);
        int indiceMenores = bajo - 1;

        for (int indice = bajo; indice < alto; indice++) {
            if (comparar(lista.get(indice), pivote, criterio) <= 0) {
                indiceMenores++;
                intercambiar(lista, indiceMenores, indice);
            }
        }

        int indicePivote = indiceMenores + 1;
        intercambiar(lista, indicePivote, alto);
        return indicePivote;
    }

    private static int comparar(Arista primera, Arista segunda, Criterio criterio) {
        if (criterio == Criterio.PESO) {
            return Double.compare(primera.getPeso(), segunda.getPeso());
        }

        int comparacionOrigen = Integer.compare(primera.getOrigen(), segunda.getOrigen());
        if (comparacionOrigen != 0) {
            return comparacionOrigen;
        }
        return Integer.compare(primera.getDestino(), segunda.getDestino());
    }

    private static void intercambiar(List<Arista> lista, int primero, int segundo) {
        Arista temporal = lista.get(primero);
        lista.set(primero, lista.get(segundo));
        lista.set(segundo, temporal);
    }
}
