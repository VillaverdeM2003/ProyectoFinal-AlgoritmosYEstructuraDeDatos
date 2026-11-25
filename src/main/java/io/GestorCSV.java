package io;

import modelo.Arista;
import modelo.Grafo;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GestorCSV {

    public static class ResultadoImportacion {
        public int verticesImportados;
        public int aristasImportadas;
        public List<String> errores = new ArrayList<>();
    }

    public static ResultadoImportacion importarCSV(File archivo, Grafo grafo) throws IOException {
        ResultadoImportacion resultado = new ResultadoImportacion();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea = br.readLine();
            if (linea == null) {
                throw new IOException("El archivo CSV está vacío.");
            }


            String[] encabezados = linea.trim().toLowerCase().split(",");
            if (encabezados.length < 3 || !encabezados[0].trim().equals("origen") ||
                    !encabezados[1].trim().equals("destino") || !encabezados[2].trim().equals("peso")) {
                throw new IOException("Encabezado inválido. Se requiere: origen,destino,peso");
            }

            int numLinea = 1;
            while ((linea = br.readLine()) != null) {
                numLinea++;
                if (linea.trim().isEmpty()) continue;

                String[] partes = linea.split(",", -1);
                if (partes.length < 3) {
                    resultado.errores.add("Línea " + numLinea + ": Registro incompleto.");
                    continue;
                }

                String strOrigen = partes[0].trim();
                String strDestino = partes[1].trim();
                String strPeso = partes[2].trim();


                if (!strOrigen.isEmpty() && strDestino.isEmpty()) {
                    try {
                        int vAislado = Integer.parseInt(strOrigen);
                        grafo.agregarVertice(vAislado);
                    } catch (NumberFormatException e) {
                        resultado.errores.add("Línea " + numLinea + ": Vértice aislado no numérico.");
                    }
                    continue;
                }

                if (strOrigen.isEmpty() || strDestino.isEmpty()) {
                    resultado.errores.add("Línea " + numLinea + ": Vértice vacío en conexión.");
                    continue;
                }

                try {
                    int origen = Integer.parseInt(strOrigen);
                    int destino = Integer.parseInt(strDestino);
                    double peso = Double.parseDouble(strPeso);

                    if (peso < 0) {
                        resultado.errores.add("Línea " + numLinea + ": Peso negativo no permitido (" + peso + ").");
                        continue;
                    }
                    if (origen == destino) {
                        resultado.errores.add("Línea " + numLinea + ": Conexión consigo mismo rechazada (" + origen + ").");
                        continue;
                    }

                    boolean agregada = grafo.agregarArista(origen, destino, peso);
                    if (agregada) {
                        resultado.aristasImportadas++;
                    } else {
                        resultado.errores.add("Línea " + numLinea + ": Conexión duplicada (" + origen + " -> " + destino + ").");
                    }

                } catch (NumberFormatException e) {
                    resultado.errores.add("Línea " + numLinea + ": Formato numérico inválido.");
                }
            }
        }

        resultado.verticesImportados = grafo.getVertices().size();
        return resultado;
    }

    public static void exportarCSV(File archivo, Grafo grafo) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            bw.write("origen,destino,peso");
            bw.newLine();


            List<Arista> aristas = grafo.obtenerTodasLasAristas();
            for (Arista a : aristas) {
                bw.write(a.getOrigen() + "," + a.getDestino() + "," + a.getPeso());
                bw.newLine();
            }
        }
    }
}