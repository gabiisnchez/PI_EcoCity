package com.ecocity.app.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Interfaz Retrofit definiendo los endpoints de la API de Gemini
 */
public interface GeminiApiService {

    // Documentación:
    // https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=YOUR_API_KEY
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    Call<GeminiResponse> generateContent(
            @Query("key") String apiKey,
            @Body GeminiRequest body);
}
