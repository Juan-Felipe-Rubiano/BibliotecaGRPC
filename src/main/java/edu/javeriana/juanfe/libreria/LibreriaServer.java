package edu.javeriana.juanfe.libreria;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;

import java.io.IOException;
import java.net.InetSocketAddress;
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
        String sql;
        try(var is = LibreriaServer.class.getResourceAsStream("/libros.sql")){
            if(is == null){
                throw new RuntimeException("No se encontro el archivo libros.sql en recursos.");
            }
            sql = new String(is.readAllBytes());
        }
        try(Connection c = DriverManager.getConnection("jdbc:sqlite:" + dbUrl);
            Statement s = c.createStatement()){
            s.executeUpdate(sql);
        } catch (Exception e){
            e.printStackTrace();
        }

        LibreriaServiceImpl servicio = new LibreriaServiceImpl(bd);

        /*Server server = NettyServerBuilder
                .forAddress(new InetSocketAddress("0.0.0.0", port))
                .addService(servicio)
                .build()
                .start();*/
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv4Addresses", "true");

        Server server = NettyServerBuilder
                .forAddress(new InetSocketAddress("0.0.0.0", port)) // esto ya será IPv4
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
