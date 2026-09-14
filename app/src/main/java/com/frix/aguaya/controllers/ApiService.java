package com.frix.aguaya.controllers;

import com.frix.aguaya.models.ReportModel;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("api/reportes")
    Call<List<ReportModel>> obtenerReportes();
}
