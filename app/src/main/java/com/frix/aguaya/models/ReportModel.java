package com.frix.aguaya.models;

import com.google.gson.annotations.SerializedName;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.io.Serializable;

public class ReportModel implements Serializable {
    private int id;
    private String barrio;
    private String tipo;
    private String descripcion;
    private String urgencia;
    private String estado;
    private double lat;
    private double lng;
    
    @SerializedName("foto_url")
    private String fotoUrl;
    
    private int confirmaciones;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;
    
    private String direccion;

    // Getters
    public int getId() { return id; }
    public String getBarrio() { return barrio; }
    public String getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public String getUrgencia() { return urgencia; }
    public String getEstado() { return estado; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public String getFotoUrl() { return fotoUrl; }
    public int getConfirmaciones() { return confirmaciones; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getDireccion() { return direccion; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setBarrio(String barrio) { this.barrio = barrio; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setUrgencia(String urgencia) { this.urgencia = urgencia; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public void setConfirmaciones(int confirmaciones) { this.confirmaciones = confirmaciones; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTiempoRelativo() {
        if (createdAt == null || createdAt.isEmpty()) return "Hace un momento";
        try {
            // Se asume formato ISO 8601
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            Date past = sdf.parse(createdAt);
            if (past == null) return "Hace un momento";
            
            Date now = new Date();
            long seconds = (now.getTime() - past.getTime()) / 1000;
            
            if (seconds < 0) return "Hace un momento";
            if (seconds < 60) return "Hace " + seconds + "s";
            
            long minutes = seconds / 60;
            if (minutes < 60) return "Hace " + minutes + "m";
            
            long hours = minutes / 60;
            if (hours < 24) return "Hace " + hours + "h";
            
            long days = hours / 24;
            return "Hace " + days + "d";
        } catch (ParseException e) {
            e.printStackTrace();
            return createdAt;
        }
    }

    public String getFechaFormateada() {
        if (createdAt == null || createdAt.isEmpty()) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            Date date = sdf.parse(createdAt);
            if (date == null) return createdAt;
            SimpleDateFormat out = new SimpleDateFormat("d 'de' MMMM, yyyy - hh:mm a", new Locale("es", "CO"));
            return out.format(date);
        } catch (Exception e) {
            return createdAt;
        }
    }
}
