package com.ecocity.app.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ecocity.app.database.IncidenciaDAO;

/**
 * Utilidad para monitorizar el estado de la conexión a Internet.
 * Dispara la sincronización SQLite -> Firestore cuando se detecta conexión.
 */
public class NetworkMonitor {

    private static final String TAG = "NetworkMonitor";
    private Context context;
    private IncidenciaDAO incidenciaDAO;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean isRegistered = false;

    public NetworkMonitor(Context context) {
        this.context = context.getApplicationContext(); // Evitar memory leaks
        this.incidenciaDAO = new IncidenciaDAO(this.context);
        this.connectivityManager = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * Inicia la monitorización continua de la red.
     */
    public void startMonitoring() {
        if (isRegistered || connectivityManager == null)
            return;

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                Log.d(TAG, "Conexión a Internet detectada. Iniciando sincronización en segundo plano...");

                // Ejecutamos la sincronización en un nuevo hilo para no bloquear el proceso de
                // red del sistema
                new Thread(() -> {
                    try {
                        incidenciaDAO.open();
                        incidenciaDAO.syncWithFirestore();
                        // No cerramos inmediatamente porque la sinc es asíncrona (usa callbacks de
                        // Firebase)
                    } catch (Exception e) {
                        Log.e(TAG, "Error durante la sincronización automática: " + e.getMessage());
                    }
                }).start();
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                Log.d(TAG, "Conexión a Internet perdida. Trabajando en modo Offline-First (SQLite).");
            }
        };

        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
            isRegistered = true;
            Log.d(TAG, "Monitor de red iniciado correctamente.");
        } catch (Exception e) {
            Log.e(TAG, "Error registrando NetworkCallback: " + e.getMessage());
        }
    }

    /**
     * Detiene la monitorización de la red (Llamar en el onDestroy() de la
     * aplicación o actividad principal).
     */
    public void stopMonitoring() {
        if (!isRegistered || connectivityManager == null || networkCallback == null)
            return;

        try {
            connectivityManager.unregisterNetworkCallback(networkCallback);
            isRegistered = false;
        } catch (Exception e) {
            Log.e(TAG, "Error desregistrando NetworkCallback: " + e.getMessage());
        }
    }

    /**
     * Comprueba si hay conexión a Internet en este instante exacto.
     */
    public boolean isNetworkAvailable() {
        if (connectivityManager == null)
            return false;

        Network network = connectivityManager.getActiveNetwork();
        if (network == null)
            return false;

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }
}
