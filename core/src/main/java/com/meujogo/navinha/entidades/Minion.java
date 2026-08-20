package com.meujogo.navinha.entidades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Pool;
import java.util.ArrayList;

public class Minion extends Inimigo {

    // Velocidade de descida constante do minion, em direção ao jogador.
    private static final float VELOCIDADE_DESCIDA = 110f;

    // Amplitude e frequência do balanço lateral (efeito "enxame"), sempre
    // em torno do X onde o minion nasceu — nunca sai disparado pro lado.
    private static final float AMPLITUDE_BALANCO = 45f;
    private static final float FREQUENCIA_BALANCO = 3.5f;

    private float tempo = 0;
    private float centroX;

    public Minion() {}

    @Override
    public void init(Texture imagem, float x, float y) {
        super.init(imagem, x, y);
        this.centroX = x; // Guarda a posição de nascimento para balançar em torno dela
        this.tempo = 0;

        if (imagem != null) {
            // Fica bem pequeno (60% do tamanho do inimigo normal)
            this.largura = (imagem.getWidth() / 3f) * 0.6f;
            this.altura = (imagem.getHeight() / 3f) * 0.6f;
        }
    }

    // CORREÇÃO: antes o Minion usava dirX * velocidade (220 px/s) na horizontal,
    // o que fazia ele disparar pro lado bem mais rápido do que descia — formando
    // aquele "cone" esquisito e, como ele nunca saía de dirX até estourar a
    // borda, os que iam pra fora da tela nunca eram desativados (só havia
    // checagem de saída por baixo), travando a onda para sempre.
    // Agora ele desce reto e só balança suavemente em torno do X de nascimento,
    // como um enxame, sem nunca sair disparado pra fora da tela.
    @Override
    public void atualizar(float delta, ArrayList<TiroInimigo> listaTirosInimigos, Pool<TiroInimigo> poolTirosInimigos) {
        if (!ativo) return;

        tempo += delta;

        this.y -= VELOCIDADE_DESCIDA * delta;
        this.x = centroX + (float) Math.sin(tempo * FREQUENCIA_BALANCO) * AMPLITUDE_BALANCO;

        if (this.y < -50) {
            this.ativo = false;
        }
    }

    @Override
    public void reset() {
        super.reset();
        tempo = 0;
    }
}
