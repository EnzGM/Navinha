package com.meujogo.navinha.entidades;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class Tiro extends Entidade {

    public float velX = 0; // Velocidade horizontal para tiros em leque/cone
    public float velY = 500; // Velocidade vertical

    public Tiro() {}

    public void init(Texture imagem, float x, float y, float velX, float velY) {
        super.init(x, y, 6, 14, 500);
        this.imagem = imagem;
        this.velX = velX;
        this.velY = velY;
    }

    // Sobrecarga mantendo compatibilidade caso chame sem velocidades
    public void init(Texture img, float x, float y) {
        init(img, x, y, 0, 500);
    }

    @Override
    public void atualizar(float delta) {
        if (!ativo) return;

        this.x += velX * delta;
        this.y += velY * delta;

        // Se sair da tela, desativa
        if (this.y > Gdx.graphics.getHeight() || this.x < -20 || this.x > Gdx.graphics.getWidth() + 20) {
            this.ativo = false;
        }
    }

    @Override
    public void reset() {
        super.reset();
        this.velX = 0;
        this.velY = 500;
    }
}
