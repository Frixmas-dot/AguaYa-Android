package com.frix.aguaya.controllers;

import com.frix.aguaya.models.EstadisticasModel;
import com.frix.aguaya.models.ReportModel;
import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("api/reportes")
    Call<List<ReportModel>> getReportes(
            @Query("tipo") String tipo,
            @Query("estado") String estado,
            @Query("urgencia") String urgencia,
            @Query("fecha") String fecha
    );

    @GET("api/reportes/{id}")
    Call<ReportModel> getReporte(@Path("id") int id);

    @POST("api/reportes")
    Call<ReportModel> crearReporte(@Body MultipartBody body);

    @PATCH("api/reportes/{id}/estado")
    Call<ReportModel> cambiarEstado(
            @Path("id") int id,
            @Body Map<String, String> body
    );

    @DELETE("api/reportes/{id}")
    Call<Void> eliminarReporte(@Path("id") int id);

    @POST("api/reportes/{id}/confirmar")
    Call<Void> confirmarReporte(@Path("id") int id);

    @GET("api/estadisticas")
    Call<EstadisticasModel> getEstadisticas();
}
