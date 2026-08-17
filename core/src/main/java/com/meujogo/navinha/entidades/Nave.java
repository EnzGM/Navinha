package com.meujogo.navinha.entidades;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Nave extends Entidade {

    public int vidas;
    public float tempoInvulneravel = 0;
    public static final float DURACAO_INVULNERAVEL = 1.5f; // 1.5 segundos invulnerável
    private float timerPiscar = 0;
    private boolean visivel = true;

    public int tipoArmaAtual = 0;

    public Nave(Texture imagem, float x, float y) {
        this.imagem = imagem;
        this.x = x;
        this.y = y;

        // === DIVIDA POR 2 PARA DEIXAR A NAVE PELA METADE DO TAMANHO ===
        this.largura = imagem.getWidth() / 2f;
        this.altura = imagem.getHeight() / 2f;

        this.velocidade = 300; // Mantém a velocidade
        this.vidas = 3;
        this.tipoArmaAtual = 0;
    }

    public boolean tomarDano() {
        // Se ainda estiver no tempo de invulnerabilidade, ignora o dano
        if (tempoInvulneravel > 0) {
            return false;
        }

        this.vidas--;
        if (this.vidas <= 0) {
            this.ativo = false;
            return true; // Game Over
        }

        // Ativa o tempo de invulnerabilidade após levar o tiro
        this.tempoInvulneravel = DURACAO_INVULNERAVEL;
        return false;
    }

    @Override
    public void atualizar(float delta) {
        if (!ativo) return;

        // Atualiza a invulnerabilidade e o piscar
        if (tempoInvulneravel > 0) {
            tempoInvulneravel -= delta;
            timerPiscar += delta;

            // Alterna a visibilidade a cada 0.1s para criar o efeito pisca-pisca
            if (timerPiscar >= 0.1f) {
                visivel = !visivel;
                timerPiscar = 0;
            }

            if (tempoInvulneravel <= 0) {
                visivel = true; // Garante que volta a ficar visível
            }
        }

        // Movimento
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            this.x -= (this.velocidade * delta);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            this.x += (this.velocidade * delta);
        }

        // Limites da Tela
        if (this.x < 0) this.x = 0;
        if (this.x > Gdx.graphics.getWidth() - this.largura) {
            this.x = Gdx.graphics.getWidth() - this.largura;
        }
    }

    @Override
    public void desenhar(SpriteBatch batch) {
        // Desenha apenas quando estiver visível no ciclo do pisca-pisca
        if (visivel) {
            super.desenhar(batch);
        }
    }
}
