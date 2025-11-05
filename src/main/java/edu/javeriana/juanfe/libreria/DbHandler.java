package edu.javeriana.juanfe.libreria;

import java.sql.*;
import java.time.LocalDate;

public class DbHandler {
    private final String url;
    public static class ResultadoSolicitud {
        public final boolean exito;
        public final String mensaje;
        public ResultadoSolicitud(boolean exito, String mensaje){
            this.exito = exito;
            this.mensaje = mensaje;
        }
    }

    public DbHandler(String url) {
        this.url = "jdbc:sqlite:" + url;
        init();
    }

    private void init() {
        try (Connection conn = DriverManager.getConnection(url)) {
            String create = "CREATE TABLE IF NOT EXISTS libros ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "titulo TEXT UNIQUE NOT NULL,"
                    + "estado CHAR(1) NOT NULL DEFAULT 'D',"
                    + "num_renovaciones INTEGER DEFAULT 0,"
                    + "fecha_prestamo TEXT"
                    + ");";
            try (Statement s = conn.createStatement()) {
                s.execute(create);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized boolean existeLibro(String titulo) {
        String query = "SELECT 1 FROM libros WHERE titulo = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, titulo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized void crearLibro(String titulo){
        if (!existeLibro(titulo)) {
            String ddl = "INSERT INTO libros (titulo, fechaPrestamo) VALUES (?)";
            try (Connection conn = DriverManager.getConnection(url);
                 PreparedStatement ps = conn.prepareStatement(ddl)) {
                ps.setString(1, titulo);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized ResultadoSolicitud prestarLibro(String titulo, int semanas) throws SQLException {
        crearLibro(titulo);
        String query = "SELECT estado FROM libros WHERE titulo = ?";
        try (Connection c = DriverManager.getConnection(url);
             PreparedStatement ps = c.prepareStatement(query)) {
            ps.setString(1, titulo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt("estado") == 'P') {
                    return new ResultadoSolicitud(false, "El libro ya está prestado.");
                }
            }
            String update = "UPDATE libros SET estado = 'P', fecha_prestamo = ? WHERE titulo = ?";
            LocalDate fecha = LocalDate.now();
            try (PreparedStatement ps2 = c.prepareStatement(update)) {
                ps2.setString(1, fecha.toString());
                ps2.setString(2, titulo);
                ps2.executeUpdate();
            }
            return new ResultadoSolicitud(true, "Libro prestado exitosamente por " + semanas + " semanas.");
        } catch (SQLException e) {
            e.printStackTrace();
            return new ResultadoSolicitud(false, "Error en la BD: " + e.getMessage());
        }
    }

    public synchronized ResultadoSolicitud renovarLibro(String titulo){
        crearLibro(titulo);
        String query = "SELECT estado, num_renovaciones, fechaPrestamo FROM libros WHERE titulo = ?";
        try(Connection c = DriverManager.getConnection(url);
            PreparedStatement ps = c.prepareStatement(query)) {
            ps.setString(1, titulo);
            try(ResultSet rs = ps.executeQuery()){
                if(!rs.next()){
                    return new ResultadoSolicitud(false, "El libro no existe en la BD.");
                }
                int prestado = rs.getInt("estado");
                int renovaciones = rs.getInt("num_renovaciones");
                if(prestado == 'D'){
                    return new ResultadoSolicitud(false, "El libro no está prestado.");
                }
                if (renovaciones >= 2) {
                    return new ResultadoSolicitud(false, "El libro ya ha sido renovado el máximo de veces (2).");
                }

                String update = "UPDATE libros SET num_renovaciones = num_renovaciones + 1 WHERE titulo = ?";
                try(PreparedStatement ps2 = c.prepareStatement(update)){
                    ps2.setString(1,titulo);
                    ps2.executeUpdate();
                }
                return new ResultadoSolicitud(true, "Renovación exitosa. Número de renovaciones: " + (renovaciones + 1));
            }
        }catch (SQLException e){
            e.printStackTrace();
            return new ResultadoSolicitud(false, "Error en la BD: " + e.getMessage());
        }
    }

    public synchronized ResultadoSolicitud devolverLibro(String titulo) throws SQLException {
        crearLibro(titulo);
        String query = "SELECT estado FROM libros WHERE titulo = ?";
        try(Connection c = DriverManager.getConnection(url);
        PreparedStatement ps = c.prepareStatement(query)){
            ps.setString(1, titulo);
            try(ResultSet rs = ps.executeQuery()){
                if(!rs.next()){
                    return new ResultadoSolicitud(false, "El libro no existe en la BD.");
                }
                int prestado = rs.getInt("estado");
                if(prestado == 'D'){
                    return new ResultadoSolicitud(false, "El libro no estaba prestado, no se puede devolver.");
                }

                String update = "UPDATE libros SET estado = 'D', num_renovaciones = 0, fecha_prestamo = NULL WHERE titulo = ?";
                try(PreparedStatement ps2 = c.prepareStatement(update)){
                    ps2.setString(1,titulo);
                    ps2.executeUpdate();
                }
                return new ResultadoSolicitud(true, "Devolución exitosa.");
            }catch (SQLException e){
                e.printStackTrace();
                return new ResultadoSolicitud(false, "Error en la BD: " + e.getMessage());
            }
        }
    }
}
