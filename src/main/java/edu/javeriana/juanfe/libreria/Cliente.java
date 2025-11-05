package edu.javeriana.juanfe.libreria;

import io.grpc.ManagedChannel;

public class Cliente {
    private final edu.javeriana.juanfe.libreria.ServicioLibreriaGrpc.ServicioLibreriaBlockingStub stub;

    public Cliente(ManagedChannel channel){
        stub = edu.javeriana.juanfe.libreria.ServicioLibreriaGrpc.newBlockingStub(channel);
    }

    public edu.javeriana.juanfe.libreria.Respuesta solicitarPrestamo(String titulo, int semanas){
        edu.javeriana.juanfe.libreria.SolicitudPrestamo request = edu.javeriana.juanfe.libreria.SolicitudPrestamo.newBuilder()
                .setTitulo(titulo)
                .setSemanas(semanas)
                .build();
        return stub.solicitarPrestamo(request);
    }

    public edu.javeriana.juanfe.libreria.Respuesta renovarPrestamo(String titulo){
        edu.javeriana.juanfe.libreria.SolicitudGenerica req = edu.javeriana.juanfe.libreria.SolicitudGenerica.newBuilder()
                .setTitulo(titulo)
                .build();
        return stub.renovarPrestamo(req);
    }

    public edu.javeriana.juanfe.libreria.Respuesta devolverPrestamo(String titulo){
        edu.javeriana.juanfe.libreria.SolicitudGenerica req = edu.javeriana.juanfe.libreria.SolicitudGenerica.newBuilder()
                .setTitulo(titulo)
                .build();
        return stub.devolverPrestamo(req);
    }
}
