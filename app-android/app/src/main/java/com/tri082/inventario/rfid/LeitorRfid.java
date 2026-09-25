// Integracao com o SDK oficial da Urovo: inicializa o leitor, inicia e para a leitura continua e entrega cada tag lida.
// A leitura de tag so funciona no coletor fisico; em outros aparelhos o leitor fica indisponivel sem quebrar o app.

package com.tri082.inventario.rfid;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.ubx.usdk.RFIDSDKManager;
import com.ubx.usdk.rfid.RfidManager;
import com.ubx.usdk.rfid.aidl.IRfidCallback;

public class LeitorRfid {

    public interface Ouvinte {
        void aoMudarStatus(boolean disponivel);

        void aoLerTag(String epc, int sinal);
    }

    private final Handler principal = new Handler(Looper.getMainLooper());
    private RfidManager rfidManager;
    private Ouvinte ouvinte;

    private final IRfidCallback callback = new IRfidCallback() {
        @Override
        public void onInventoryTag(String epc, String memInfo, int sinal) {
            if (epc == null || epc.isEmpty()) {
                return;
            }
            principal.post(() -> {
                if (ouvinte != null) {
                    ouvinte.aoLerTag(epc, sinal);
                }
            });
        }

        @Override
        public void onInventoryTagEnd() {
        }
    };

    public void iniciar(Context context, Ouvinte ouvinte) {
        this.ouvinte = ouvinte;
        try {
            RFIDSDKManager.getInstance().init(context.getApplicationContext(), sucesso -> {
                if (sucesso) {
                    rfidManager = RFIDSDKManager.getInstance().getRfidManager();
                    if (rfidManager != null) {
                        rfidManager.registerCallback(callback);
                    }
                }
                principal.post(this::avisarStatus);
            });
        } catch (Throwable erro) {
            principal.post(this::avisarStatus);
        }
    }

    private void avisarStatus() {
        if (ouvinte != null) {
            ouvinte.aoMudarStatus(rfidManager != null);
        }
    }

    public boolean disponivel() {
        return rfidManager != null;
    }

    public void ligarLeitura() {
        if (rfidManager == null) {
            return;
        }
        try {
            rfidManager.startInventory();
        } catch (Throwable ignorado) {
        }
    }

    public void desligarLeitura() {
        if (rfidManager == null) {
            return;
        }
        try {
            rfidManager.stopInventory();
        } catch (Throwable ignorado) {
        }
    }

    public void liberar() {
        try {
            if (rfidManager != null) {
                rfidManager.unregisterCallback(callback);
            }
            RFIDSDKManager.getInstance().release();
        } catch (Throwable ignorado) {
        }
        rfidManager = null;
        ouvinte = null;
    }
}
