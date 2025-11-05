package edu.javeriana.juanfe.libreria;

import io.grpc.stub.StreamObserver;

import edu.javeriana.juanfe.libreria.SolicitudPrestamo;
import edu.javeriana.juanfe.libreria.SolicitudGenerica;
import edu.javeriana.juanfe.libreria.Respuesta;


public class LibreriaServiceImpl extends edu.javeriana.juanfe.libreria.ServicioLibreriaGrpc.ServicioLibreriaImplBase {

    private final DbHandler bd;

    public LibreriaServiceImpl(DbHandler bd){
        this.bd = bd;
    }

    @Override
    public void solicitarPrestamo(SolicitudPrestamo request,
                                  StreamObserver<Respuesta> responseObserver) {

        String titulo = request.getTitulo();
        int semanas = request.getSemanas() == 0 ? 2 : request.getSemanas();

        try {
            DbHandler.ResultadoSolicitud res = bd.prestarLibro(titulo, semanas);

            Respuesta r = Respuesta.newBuilder()
                    .setExito(res.exito)
                    .setMensaje(res.mensaje)
                    .build();

            responseObserver.onNext(r);
            responseObserver.onCompleted();

        } catch (Exception e) {

            Respuesta r = Respuesta.newBuilder()
                    .setExito(false)
                    .setMensaje("Error en BD: " + e.getMessage())
                    .build();
            responseObserver.onNext(r);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void devolverPrestamo(SolicitudGenerica request,
                                 StreamObserver<Respuesta> responseObserver) {

        String titulo = request.getTitulo();

        try {
            DbHandler.ResultadoSolicitud res = bd.devolverLibro(titulo);

            Respuesta r = Respuesta.newBuilder()
                    .setExito(res.exito)
                    .setMensaje(res.mensaje)
                    .build();

            responseObserver.onNext(r);
            responseObserver.onCompleted();

        } catch (Exception e) {

            Respuesta r = Respuesta.newBuilder()
                    .setExito(false)
                    .setMensaje("Error en BD: " + e.getMessage())
                    .build();
            responseObserver.onNext(r);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void renovarPrestamo(SolicitudGenerica request,
                                StreamObserver<Respuesta> responseObserver) {

        String titulo = request.getTitulo();

        DbHandler.ResultadoSolicitud res = bd.renovarLibro(titulo);

        Respuesta r = Respuesta.newBuilder()
                .setExito(res.exito)
                .setMensaje(res.mensaje)
                .build();

        responseObserver.onNext(r);
        responseObserver.onCompleted();
    }
}
