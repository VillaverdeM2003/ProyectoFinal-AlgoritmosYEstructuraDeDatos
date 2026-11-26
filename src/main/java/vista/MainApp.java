package vista;

import algoritmos.AlgoritmosGrafo;
import algoritmos.Ordenamiento;
import io.GestorCSV;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import modelo.Arista;
import modelo.Grafo;

import java.io.File;
import java.util.*;

public class MainApp extends Application {

    private Grafo grafo = new Grafo(false);
    private Pane panelGrafo = new Pane();
    private TableView<Arista> tablaAristas = new TableView<>();
    private TextArea consola = new TextArea();

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(0, 0, 10, 0));
        Button btnCargar = new Button("Importar CSV...");
        Button btnGuardar = new Button("Exportar CSV");
        Button btnLimpiarTop = new Button("Limpiar Todo");
        btnLimpiarTop.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #842029; -fx-border-color: #f5c2c7; -fx-border-radius: 3; -fx-background-radius: 3;");
        CheckBox chkDirigido = new CheckBox("Grafo Dirigido");

        ComboBox<String> cmbArchivosData = new ComboBox<>();
        cmbArchivosData.setPromptText("Seleccionar CSV de data/...");
        cmbArchivosData.setPrefWidth(230);
        cargarListaArchivosData(cmbArchivosData);

        cmbArchivosData.setOnAction(e -> {
            String seleccionado = cmbArchivosData.getValue();
            if (seleccionado != null) {
                File archivo = new File("data", seleccionado);
                if (archivo.exists()) {
                    cargarArchivoCSV(archivo);
                }
            }
        });

        chkDirigido.setOnAction(e -> {
            grafo = new Grafo(chkDirigido.isSelected());
            actualizarVista();
            log("Modo cambiado a: " + (chkDirigido.isSelected() ? "Dirigido" : "No Dirigido"));
            if (cmbArchivosData.getValue() != null) {
                cargarArchivoCSV(new File("data", cmbArchivosData.getValue()));
            }
        });

        btnCargar.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
            File file = fc.showOpenDialog(stage);
            if (file != null) {
                cmbArchivosData.getSelectionModel().clearSelection();
                cargarArchivoCSV(file);
            }
        });

        btnGuardar.setOnAction(e -> {
            exportarCSV(stage);
            cargarListaArchivosData(cmbArchivosData);
        });

        topBar.getChildren().addAll(
                new Label("Archivos:"), cmbArchivosData, btnCargar, btnGuardar, btnLimpiarTop, new Separator(), chkDirigido
        );
        root.setTop(topBar);

        SplitPane centerSplit = new SplitPane();
        panelGrafo.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #cccccc;");
        panelGrafo.widthProperty().addListener((obs, oldVal, newVal) -> actualizarVista());
        panelGrafo.heightProperty().addListener((obs, oldVal, newVal) -> actualizarVista());

        configurarTabla();
        centerSplit.getItems().addAll(panelGrafo, tablaAristas);
        centerSplit.setDividerPositions(0.68);
        root.setCenter(centerSplit);

        VBox leftMenu = new VBox(8);
        leftMenu.setPadding(new Insets(0, 10, 0, 0));
        leftMenu.setPrefWidth(240);

        TextField txtOrigen = new TextField(); txtOrigen.setPromptText("Vértice Origen (ej. 0)");
        TextField txtDestino = new TextField(); txtDestino.setPromptText("Vértice Destino (ej. 1)");
        TextField txtPeso = new TextField(); txtPeso.setPromptText("Peso (ej. 100)");

        btnLimpiarTop.setOnAction(e -> {
            grafo = new Grafo(chkDirigido.isSelected());
            cmbArchivosData.getSelectionModel().clearSelection();
            txtOrigen.clear();
            txtDestino.clear();
            txtPeso.clear();
            panelGrafo.getChildren().clear();
            tablaAristas.getItems().clear();
            consola.clear();
        });

        Button btnAddVertice = new Button("Agregar Vértice (Origen)");
        btnAddVertice.setMaxWidth(Double.MAX_VALUE);
        btnAddVertice.setOnAction(e -> {
            try {
                int v = Integer.parseInt(txtOrigen.getText().trim());
                grafo.agregarVertice(v);
                actualizarVista();
                log("Vértice agregado: " + v);
            } catch (Exception ex) { log("Error: Ingresa un entero válido."); }
        });

        Button btnAddArista = new Button("Agregar Conexión");
        btnAddArista.setMaxWidth(Double.MAX_VALUE);
        btnAddArista.setOnAction(e -> {
            try {
                int u = Integer.parseInt(txtOrigen.getText().trim());
                int v = Integer.parseInt(txtDestino.getText().trim());
                double w = Double.parseDouble(txtPeso.getText().trim());
                if (grafo.agregarArista(u, v, w)) {
                    actualizarVista();
                    log("Arista agregada: " + u + " -> " + v + " (" + w + ")");
                } else {
                    log("Error: Conexión inválida, duplicada o mismo vértice.");
                }
            } catch (Exception ex) { log("Error en los datos de la arista."); }
        });

        Button btnBFS = new Button("Ejecutar BFS (desde Origen)");
        btnBFS.setMaxWidth(Double.MAX_VALUE);
        btnBFS.setOnAction(e -> ejecutarRecorrido(txtOrigen.getText(), true));

        Button btnDFS = new Button("Ejecutar DFS (desde Origen)");
        btnDFS.setMaxWidth(Double.MAX_VALUE);
        btnDFS.setOnAction(e -> ejecutarRecorrido(txtOrigen.getText(), false));

        Button btnDijkstra = new Button("Ruta Óptima (Dijkstra)");
        btnDijkstra.setMaxWidth(Double.MAX_VALUE);
        btnDijkstra.setOnAction(e -> ejecutarDijkstra(txtOrigen.getText(), txtDestino.getText()));

        Button btnQuickPeso = new Button("QuickSort (por Peso)");
        btnQuickPeso.setMaxWidth(Double.MAX_VALUE);
        btnQuickPeso.setOnAction(e -> ordenarAristas(true, Ordenamiento.Criterio.PESO));

        Button btnShellVertice = new Button("ShellSort (por Vértice)");
        btnShellVertice.setMaxWidth(Double.MAX_VALUE);
        btnShellVertice.setOnAction(e -> ordenarAristas(false, Ordenamiento.Criterio.VERTICE_ORIGEN));

        leftMenu.getChildren().addAll(
                new Label("Gestión Manual:"), txtOrigen, txtDestino, txtPeso,
                btnAddVertice, btnAddArista, new Separator(),
                new Label("Algoritmos:"), btnBFS, btnDFS, btnDijkstra, new Separator(),
                new Label("Ordenamiento:"), btnQuickPeso, btnShellVertice
        );
        root.setLeft(leftMenu);

        consola.setEditable(false);
        consola.setPrefHeight(130);
        BorderPane.setMargin(consola, new Insets(10, 0, 0, 0));
        root.setBottom(consola);

        Scene scene = new Scene(root, 1280, 780);
        stage.setTitle("Gestión y Análisis de Rutas mediante Grafos");
        stage.setScene(scene);
        stage.show();
    }

    private void configurarTabla() {
        TableColumn<Arista, Integer> colOrigen = new TableColumn<>("Origen");
        colOrigen.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getOrigen()).asObject());

        TableColumn<Arista, Integer> colDestino = new TableColumn<>("Destino");
        colDestino.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getDestino()).asObject());

        TableColumn<Arista, Double> colPeso = new TableColumn<>("Peso");
        colPeso.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPeso()).asObject());

        tablaAristas.getColumns().addAll(colOrigen, colDestino, colPeso);
        tablaAristas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void actualizarVista() {
        tablaAristas.setItems(FXCollections.observableArrayList(grafo.obtenerTodasLasAristas()));
        panelGrafo.getChildren().clear();

        int n = grafo.getVertices().size();
        if (n == 0) return;

        if (n > 50) {
            Text aviso = new Text(30, 50, "Grafo con " + n + " vértices (> 50).\nVisualización gráfica desactivada; consulta la tabla derecha.");
            aviso.setFont(Font.font("System", FontWeight.BOLD, 14));
            panelGrafo.getChildren().add(aviso);
            return;
        }

        double ancho = panelGrafo.getWidth() > 0 ? panelGrafo.getWidth() : 750;
        double alto = panelGrafo.getHeight() > 0 ? panelGrafo.getHeight() : 550;
        double centroX = ancho / 2.0;
        double centroY = alto / 2.0;
        double radioNodo = 18.0;
        double radioCirculo = Math.max(80, Math.min(ancho, alto) / 2.0 - 55);

        Map<Integer, double[]> coords = new HashMap<>();
        List<Integer> listaV = new ArrayList<>(grafo.getVertices());
        Collections.sort(listaV);

        for (int i = 0; i < n; i++) {
            double angulo = 2 * Math.PI * i / n - Math.PI / 2;
            double x = centroX + radioCirculo * Math.cos(angulo);
            double y = centroY + radioCirculo * Math.sin(angulo);
            coords.put(listaV.get(i), new double[]{x, y});
        }

        List<Arista> aristas = grafo.obtenerTodasLasAristas();
        Set<String> paresExistentes = new HashSet<>();
        for (Arista a : aristas) {
            paresExistentes.add(a.getOrigen() + "-" + a.getDestino());
        }

        for (Arista a : aristas) {
            double[] p1 = coords.get(a.getOrigen());
            double[] p2 = coords.get(a.getDestino());
            if (p1 == null || p2 == null) continue;

            double dx = p2[0] - p1[0];
            double dy = p2[1] - p1[1];
            double dist = Math.hypot(dx, dy);
            if (dist == 0) continue;

            double ux = dx / dist;
            double uy = dy / dist;

            double startX = p1[0] + ux * radioNodo;
            double startY = p1[1] + uy * radioNodo;
            double endX = p2[0] - ux * radioNodo;
            double endY = p2[1] - uy * radioNodo;

            boolean tieneInversa = grafo.isEsDirigido() && paresExistentes.contains(a.getDestino() + "-" + a.getOrigen());
            double labelX, labelY;

            if (tieneInversa) {
                double perpX = -uy * 22;
                double perpY = ux * 22;
                double ctrlX = (startX + endX) / 2.0 + perpX;
                double ctrlY = (startY + endY) / 2.0 + perpY;

                QuadCurve curva = new QuadCurve(startX, startY, ctrlX, ctrlY, endX, endY);
                curva.setFill(null);
                curva.setStroke(Color.web("#6c757d"));
                curva.setStrokeWidth(1.4);
                panelGrafo.getChildren().add(curva);

                dibujarFlecha(ctrlX, ctrlY, endX, endY);

                double t = 0.35;
                labelX = (1 - t) * (1 - t) * startX + 2 * (1 - t) * t * ctrlX + t * t * endX;
                labelY = (1 - t) * (1 - t) * startY + 2 * (1 - t) * t * ctrlY + t * t * endY;
            } else {
                Line linea = new Line(startX, startY, endX, endY);
                linea.setStroke(Color.web("#7f8c8d"));
                linea.setStrokeWidth(1.4);
                panelGrafo.getChildren().add(linea);

                if (grafo.isEsDirigido()) {
                    dibujarFlecha(startX, startY, endX, endY);
                }

                double factor = grafo.isEsDirigido() ? 0.30 : 0.36;
                labelX = startX + (endX - startX) * factor;
                labelY = startY + (endY - startY) * factor;
            }

            Label lblPeso = new Label(String.valueOf((int) a.getPeso()));
            lblPeso.setStyle(
                    "-fx-background-color: rgba(255, 255, 255, 0.88); " +
                            "-fx-text-fill: #b00020; " +
                            "-fx-font-size: 10px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 1 3 1 3; " +
                            "-fx-border-color: #dcdcdc; " +
                            "-fx-border-radius: 3; " +
                            "-fx-background-radius: 3;"
            );
            lblPeso.setLayoutX(labelX - 12);
            lblPeso.setLayoutY(labelY - 8);
            panelGrafo.getChildren().add(lblPeso);
        }

        for (int v : listaV) {
            double[] p = coords.get(v);
            Circle circulo = new Circle(p[0], p[1], radioNodo, Color.web("#cfe2ff"));
            circulo.setStroke(Color.web("#084298"));
            circulo.setStrokeWidth(1.8);

            Text etiqueta = new Text(String.valueOf(v));
            etiqueta.setFont(Font.font("System", FontWeight.BOLD, 12));
            etiqueta.setFill(Color.web("#052c65"));
            double textWidth = etiqueta.getLayoutBounds().getWidth();
            etiqueta.setX(p[0] - textWidth / 2.0);
            etiqueta.setY(p[1] + 4);

            panelGrafo.getChildren().addAll(circulo, etiqueta);
        }
    }

    private void dibujarFlecha(double fromX, double fromY, double toX, double toY) {
        double angulo = Math.atan2(toY - fromY, toX - fromX);
        double largoFlecha = 10.0;
        double apertura = Math.toRadians(25);

        double x1 = toX - largoFlecha * Math.cos(angulo - apertura);
        double y1 = toY - largoFlecha * Math.sin(angulo - apertura);
        double x2 = toX - largoFlecha * Math.cos(angulo + apertura);
        double y2 = toY - largoFlecha * Math.sin(angulo + apertura);

        Polygon punta = new Polygon(toX, toY, x1, y1, x2, y2);
        punta.setFill(Color.web("#212529"));
        panelGrafo.getChildren().add(punta);
    }

    private void cargarListaArchivosData(ComboBox<String> combo) {
        String seleccionPrevia = combo.getValue();
        combo.getItems().clear();
        File carpetaData = new File("data");
        if (carpetaData.exists() && carpetaData.isDirectory()) {
            File[] archivos = carpetaData.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
            if (archivos != null) {
                Arrays.sort(archivos);
                for (File f : archivos) {
                    combo.getItems().add(f.getName());
                }
            }
        }
        if (seleccionPrevia != null && combo.getItems().contains(seleccionPrevia)) {
            combo.setValue(seleccionPrevia);
        }
    }

    private void cargarArchivoCSV(File file) {
        try {
            grafo = new Grafo(grafo.isEsDirigido());
            long inicio = System.nanoTime();
            GestorCSV.ResultadoImportacion res = GestorCSV.importarCSV(file, grafo);
            long fin = System.nanoTime();
            double ms = (fin - inicio) / 1_000_000.0;

            actualizarVista();
            log(String.format("[%s] Cargado en %.4f ms | Vértices: %d | Aristas: %d",
                    file.getName(), ms, res.verticesImportados, res.aristasImportadas));
            if (!res.errores.isEmpty()) {
                log("Advertencias (" + res.errores.size() + "): " + res.errores.get(0));
            }
        } catch (Exception ex) {
            log("Error al leer CSV: " + ex.getMessage());
        }
    }

    private void exportarCSV(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        File file = fc.showSaveDialog(stage);
        if (file != null) {
            try {
                GestorCSV.exportarCSV(file, grafo);
                log("Grafo exportado correctamente (vértices aislados omitidos).");
            } catch (Exception ex) {
                log("Error al exportar: " + ex.getMessage());
            }
        }
    }

    private void ejecutarRecorrido(String strInicio, boolean esBFS) {
        try {
            int inicio = Integer.parseInt(strInicio.trim());
            long t1 = System.nanoTime();
            List<Integer> res = esBFS ? AlgoritmosGrafo.bfs(grafo, inicio) : AlgoritmosGrafo.dfs(grafo, inicio);
            long t2 = System.nanoTime();
            log(String.format("%s desde %d (%.4f ms): %s",
                    esBFS ? "BFS" : "DFS", inicio, (t2 - t1) / 1_000_000.0, res));
        } catch (Exception e) { log("Ingresa un vértice de origen válido."); }
    }

    private void ejecutarDijkstra(String strOrigen, String strDestino) {
        try {
            int u = Integer.parseInt(strOrigen.trim());
            int v = Integer.parseInt(strDestino.trim());
            long t1 = System.nanoTime();
            AlgoritmosGrafo.ResultadoDijkstra res = AlgoritmosGrafo.dijkstra(grafo, u, v);
            long t2 = System.nanoTime();

            if (res.ruta.isEmpty()) {
                log("No existe ruta entre " + u + " y " + v);
            } else {
                log(String.format("Dijkstra (%d -> %d) en %.4f ms | Costo: %.2f | Ruta: %s",
                        u, v, (t2 - t1) / 1_000_000.0, res.costoTotal, res.ruta));
            }
        } catch (Exception e) { log("Ingresa origen y destino válidos."); }
    }

    private void ordenarAristas(boolean usarQuickSort, Ordenamiento.Criterio criterio) {
        List<Arista> lista = grafo.obtenerTodasLasAristas();
        long t1 = System.nanoTime();
        if (usarQuickSort) {
            Ordenamiento.quickSort(lista, criterio);
        } else {
            Ordenamiento.shellSort(lista, criterio);
        }
        long t2 = System.nanoTime();

        tablaAristas.setItems(FXCollections.observableArrayList(lista));
        log(String.format("%s por %s ejecutado en %.4f ms",
                usarQuickSort ? "QuickSort" : "ShellSort", criterio, (t2 - t1) / 1_000_000.0));
    }

    private void log(String mensaje) {
        consola.appendText(mensaje + "\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}