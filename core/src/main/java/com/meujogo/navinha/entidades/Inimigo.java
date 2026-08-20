package com.meujogo.navinha.entidades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Pool;
import java.util.ArrayList;

public class Inimigo extends Entidade {

    public float dirX = 1f; // 1 = Direita, -1 = Esquerda

    public Inimigo() {}

    public void init(Texture imagem, float x, float y) {
        super.init(x, y, 32, 32, 120); // Velocidade horizontal
        this.imagem = imagem;
        this.dirX = 1f;
    }

    public void atualizar(float delta, ArrayList<TiroInimigo> listaTirosInimigos, Pool<TiroInimigo> poolTirosInimigos) {
        if (!ativo) return;

        // Movimento horizontal
        this.x += dirX * velocidade * delta;

        // Desativa se passar do fundo da tela
        if (this.y < -50) {
            this.ativo = false;
        }
    }

    @Override
    public void atualizar(float delta) {
        atualizar(delta, null, null);
    }

    @Override
    public void reset() {
        super.reset();
        this.dirX = 1f;
    }
}
