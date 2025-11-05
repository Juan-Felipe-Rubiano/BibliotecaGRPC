package edu.javeriana.juanfe.libreria;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.BufferedReader;
import java.io.FileReader;

public class ClienteRun {
    public static void main(String[] args) throws Exception{
       if(args.length < 1){
           System.out.println("Uso: java -jar cliente.jar entrada.txt [puerto]");
           return;
       }

       String archivo = args[0];
       int port = 50051;
         if(args.length >= 2){
              port = Integer.parseInt(args[1]);
         }

        System.out.println("Conectando al servidor en el puerto " + port + " y leyendo archivo " + archivo);
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost",port)
                .usePlaintext()
                .build();

         edu.javeriana.juanfe.libreria.ServicioLibreriaGrpc.ServicioLibreriaBlockingStub stub = edu.javeriana.juanfe.libreria.ServicioLibreriaGrpc.newBlockingStub(channel);

         try(BufferedReader br = new BufferedReader(new FileReader(archivo))){
             String linea;
             while((linea = br.readLine()) != null){
                 String[] partes = linea.split(",");
                 String op = partes[0].trim();
                 String titulo = partes[1].trim();

                 switch (op){
                     case "P":
                         int semanas = Integer.parseInt(partes[2].trim());
                         edu.javeriana.juanfe.libreria.SolicitudPrestamo reqP = edu.javeriana.juanfe.libreria.SolicitudPrestamo.newBuilder()
                                 .setTitulo(titulo)
                                 .setSemanas(semanas)
                                 .build();
                         edu.javeriana.juanfe.libreria.Respuesta respP = stub.solicitarPrestamo(reqP);
                         System.out.println("Prestamo >>" + respP.getMensaje());
                            break;
                     case "R":
                         edu.javeriana.juanfe.libreria.SolicitudGenerica reqR = edu.javeriana.juanfe.libreria.SolicitudGenerica.newBuilder()
                                 .setTitulo(titulo)
                                 .build();
                         edu.javeriana.juanfe.libreria.Respuesta respR = stub.renovarPrestamo(reqR);
                         System.out.println("Renovacion >>" + respR.getMensaje());
                            break;
                     case "D":
                            edu.javeriana.juanfe.libreria.SolicitudGenerica reqD = edu.javeriana.juanfe.libreria.SolicitudGenerica.newBuilder()
                                    .setTitulo(titulo)
                                    .build();
                            edu.javeriana.juanfe.libreria.Respuesta respD = stub.devolverPrestamo(reqD);
                            System.out.println("Devolucion >>" + respD.getMensaje());
                                break;
                 }
             }
         }
         channel.shutdown();
    }
}
