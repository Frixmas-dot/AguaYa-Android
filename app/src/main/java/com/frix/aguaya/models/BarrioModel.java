package com.frix.aguaya.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class BarrioModel implements Serializable {
    private int id;
    private String nombre;
    private String localidad;
    private double lat;
    private double lng;

    public BarrioModel() {
    }

    public BarrioModel(int id, String nombre, String localidad, double lat, double lng) {
        this.id = id;
        this.nombre = nombre;
        this.localidad = localidad;
        this.lat = lat;
        this.lng = lng;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getLocalidad() { return localidad; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }

    public void setId(int id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }

    @Override
    public String toString() {
        return nombre;
    }
}
