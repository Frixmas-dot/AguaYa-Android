package com.frix.aguaya.models;

import java.io.Serializable;

public class RutaCarrotanqueModel implements Serializable {
    private int id;
    private String barrio;
    private String sector;
    private String horario;
    private String capacidad;
    private String estado; // "en_camino", "programada", "completada"
    private String puntoDistribucion;
    private String observaciones;
    private String vehiculo;

    public RutaCarrotanqueModel(int id, String barrio, String sector, String horario,
                               String capacidad, String estado, String puntoDistribucion,
                               String observaciones, String vehiculo) {
        this.id = id;
        this.barrio = barrio;
        this.sector = sector;
        this.horario = horario;
        this.capacidad = capacidad;
        this.estado = estado;
        this.puntoDistribucion = puntoDistribucion;
        this.observaciones = observaciones;
        this.vehiculo = vehiculo;
    }

    public int getId() { return id; }
    public String getBarrio() { return barrio; }
    public String getSector() { return sector; }
    public String getHorario() { return horario; }
    public String getCapacidad() { return capacidad; }
    public String getEstado() { return estado; }
    public String getPuntoDistribucion() { return puntoDistribucion; }
    public String getObservaciones() { return observaciones; }
    public String getVehiculo() { return vehiculo; }

    public void setEstado(String estado) { this.estado = estado; }
}
