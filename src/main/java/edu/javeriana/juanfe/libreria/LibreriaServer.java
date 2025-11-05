package edu.javeriana.juanfe.libreria;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class LibreriaServer {
    public static void main(String args[]) throws IOException, InterruptedException {
        int port = 50051;
        String dbUrl = "libreria.db";

        if(args.length >= 1){
            dbUrl = args[0];
        }
        if(args.length >= 2){
            port = Integer.parseInt(args[1]);
        }

        DbHandler bd = new DbHandler(dbUrl);

        String sql = Files.readString(Paths.get("src/main/resources/libros.sql"));
        try(Connection c = DriverManager.getConnection("jdbc:sqlite:" + dbUrl);
            Statement s = c.createStatement()){
            s.executeUpdate(sql);
        } catch (Exception e){
            e.printStackTrace();
        }

        LibreriaServiceImpl servicio = new LibreriaServiceImpl(bd);

        Server server = ServerBuilder.forPort(port)
                .addService(servicio)
                .build()
                .start();

        System.out.println("Servidor GRPC iniciado en puerto " + port + ". Con BD en " + dbUrl);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Cerrando servidor GRPC...");
            server.shutdown();
            System.out.println("Servidor cerrado.");
        }));
        server.awaitTermination();
    }
}
