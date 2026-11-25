package io;

import modelo.Arista;
import modelo.Grafo;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class GestorCSV {


    public static Grafo cargarDesdeCSV(File archivo, boolean dirigidoPorDefecto) throws IOException {
        boolean esDirigido = dirigidoPorDefecto;
        Grafo grafo = new Grafo(esDirigido);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            String linea;
            boolean primeraLinea = true;

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }

                if (primeraLinea) {
                    primeraLinea = false;
                    if (linea.equalsIgnoreCase("DIRIGIDO")) {
                        esDirigido = true;
                        grafo.setEsDirigido(true);
                        continue;
                    } else if (linea.equalsIgnoreCase("NO_DIRIGIDO")) {
                        esDirigido = false;
                        grafo.setEsDirigido(false);
                        continue;
                    }
                }

                String[] partes = linea.split(",");
                if (partes.length == 1) {
                    // Permite declarar vertices aislados en una sola columna
                    int v = Integer.parseInt(partes[0].trim());
                    grafo.agregarVertice(v);
                } else if (partes.length >= 2) {
                    int origen = Integer.parseInt(partes[0].trim());
                    int destino = Integer.parseInt(partes[1].trim());
                    double peso = (partes.length >= 3) ? Double.parseDouble(partes[2].trim()) : 1.0;
                    grafo.agregarArista(origen, destino, peso);
                }
            }
        } catch (NumberFormatException e) {
            throw new IOException("Formato numérico inválido en el archivo CSV: " + e.getMessage(), e);
        }

        return grafo;
    }


    public static void guardarEnCSV(Grafo grafo, File archivo) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
            bw.write(grafo.isEsDirigido() ? "DIRIGIDO" : "NO_DIRIGIDO");
            bw.newLine();


            for (Arista a : grafo.obtenerTodasLasAristas()) {
                bw.write(a.getOrigen() + "," + a.getDestino() + "," + a.getPeso());
                bw.newLine();
            }

            for (int v : grafo.getVertices()) {
                boolean tieneConexion = !grafo.getListaAdyacencia().get(v).isEmpty();
                if (!tieneConexion && grafo.isEsDirigido()) {
                    for (Arista a : grafo.obtenerTodasLasAristas()) {
                        if (a.getDestino() == v) {
                            tieneConexion = true;
                            break;
                        }
                    }
                }
                if (!tieneConexion) {
                    bw.write(String.valueOf(v));
                    bw.newLine();
                }
            }
        }
    }
}