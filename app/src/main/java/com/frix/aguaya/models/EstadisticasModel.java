package com.frix.aguaya.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EstadisticasModel {
    private int totalReportes;
    private int barriosUnicos;
    private double pctResueltos;
    private double pctEnProceso;
    private double pctPendientes;
    private List<TipoCount> porTipo;
    private List<BarrioCount> porBarrio;

    // Getters and Setters
    public int getTotalReportes() { return totalReportes; }
    public void setTotalReportes(int totalReportes) { this.totalReportes = totalReportes; }

    public int getBarriosUnicos() { return barriosUnicos; }
    public void setBarriosUnicos(int barriosUnicos) { this.barriosUnicos = barriosUnicos; }

    public double getPctResueltos() { return pctResueltos; }
    public void setPctResueltos(double pctResueltos) { this.pctResueltos = pctResueltos; }

    public double getPctEnProceso() { return pctEnProceso; }
    public void setPctEnProceso(double pctEnProceso) { this.pctEnProceso = pctEnProceso; }

    public double getPctPendientes() { return pctPendientes; }
    public void setPctPendientes(double pctPendientes) { this.pctPendientes = pctPendientes; }

    public List<TipoCount> getPorTipo() { return porTipo; }
    public void setPorTipo(List<TipoCount> porTipo) { this.porTipo = porTipo; }

    public List<BarrioCount> getPorBarrio() { return porBarrio; }
    public void setPorBarrio(List<BarrioCount> porBarrio) { this.porBarrio = porBarrio; }

    public static class TipoCount {
        private String tipo;
        private int cantidad;

        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }

        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    }

    public static class BarrioCount {
        private String barrio;
        private int cantidad;
        private String tipoPrincipal;

        public String getBarrio() { return barrio; }
        public void setBarrio(String barrio) { this.barrio = barrio; }

        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }

        public String getTipoPrincipal() { return tipoPrincipal; }
        public void setTipoPrincipal(String tipoPrincipal) { this.tipoPrincipal = tipoPrincipal; }
    }
}
