// Medidor circular do Localizar: arco de 270 graus com marcacoes em volta e preenchimento animado.

package com.tri082.inventario.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.tri082.inventario.R;

public class MedidorSinal extends View {

    private static final float INICIO = 135f;
    private static final float VARREDURA = 270f;
    private static final int MARCACOES = 46;

    private final Paint trilho = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arco = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint marca = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF area = new RectF();
    private final float densidade;
    private float valor;
    private ValueAnimator animacao;

    public MedidorSinal(Context contexto, @Nullable AttributeSet atributos) {
        super(contexto, atributos);
        densidade = getResources().getDisplayMetrics().density;
        float espessura = 14 * densidade;

        trilho.setStyle(Paint.Style.STROKE);
        trilho.setStrokeWidth(espessura);
        trilho.setStrokeCap(Paint.Cap.ROUND);
        trilho.setColor(ContextCompat.getColor(contexto, R.color.fundo));

        arco.setStyle(Paint.Style.STROKE);
        arco.setStrokeWidth(espessura);
        arco.setStrokeCap(Paint.Cap.ROUND);
        arco.setColor(ContextCompat.getColor(contexto, R.color.texto_secundario));

        marca.setStyle(Paint.Style.STROKE);
        marca.setStrokeWidth(1.5f * densidade);
        marca.setStrokeCap(Paint.Cap.ROUND);
        marca.setColor(ContextCompat.getColor(contexto, R.color.linha));
    }

    public void definir(float novoValor, int cor) {
        arco.setColor(cor);
        float destino = Math.max(0f, Math.min(100f, novoValor));
        if (animacao != null) {
            animacao.cancel();
        }
        animacao = ValueAnimator.ofFloat(valor, destino);
        animacao.setDuration(220);
        animacao.addUpdateListener(a -> {
            valor = (float) a.getAnimatedValue();
            invalidate();
        });
        animacao.start();
    }

    @Override
    protected void onMeasure(int larguraSpec, int alturaSpec) {
        int lado = Math.min(MeasureSpec.getSize(larguraSpec), MeasureSpec.getSize(alturaSpec));
        setMeasuredDimension(lado, lado);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float lado = Math.min(getWidth(), getHeight());
        float centro = lado / 2f;
        float externo = centro - 2 * densidade;
        float tamanhoMarca = 7 * densidade;
        float raioArco = externo - tamanhoMarca - 10 * densidade - arco.getStrokeWidth() / 2f;

        for (int i = 0; i <= MARCACOES; i++) {
            double angulo = Math.toRadians(INICIO + VARREDURA * i / MARCACOES);
            float cos = (float) Math.cos(angulo);
            float sen = (float) Math.sin(angulo);
            canvas.drawLine(centro + cos * (externo - tamanhoMarca), centro + sen * (externo - tamanhoMarca),
                    centro + cos * externo, centro + sen * externo, marca);
        }

        area.set(centro - raioArco, centro - raioArco, centro + raioArco, centro + raioArco);
        canvas.drawArc(area, INICIO, VARREDURA, false, trilho);
        if (valor > 0.5f) {
            canvas.drawArc(area, INICIO, VARREDURA * valor / 100f, false, arco);
        }
    }
}
