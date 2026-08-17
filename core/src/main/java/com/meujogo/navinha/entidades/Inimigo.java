package com.meujogo.navinha.entidades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Pool;
import java.util.ArrayList;

public class Inimigo extends Entidade {

    public Inimigo() {}

    public void init(Texture imagem, float x, float y) {
        this.imagem = imagem;
        this.x = x;
        this.y = y;
        this.largura = imagem.getWidth() / 3f;
        this.altura = imagem.getHeight() / 3f;
        this.velocidade = 50;
        this.ativo = true;
    }

    @Override
    public void atualizar(float delta) {
        // Movimento padrão: apenas desce na tela
        this.y -= this.velocidade * delta;

        if (this.y < -this.altura) {
            this.ativo = false;
        }
    }

    // NOVO: Adicionamos este método aqui!
    // Inimigos comuns não atiram, então eles só executam o movimento padrão acima.
    public void atualizar(float delta, ArrayList<TiroInimigo> tirosInimigos, Pool<TiroInimigo> poolTirosInimigos) {
        atualizar(delta);
    }

    @Override
    public void desenhar(SpriteBatch batch) {
        if (ativo && imagem != null) {
            batch.draw(imagem, x, y, largura, altura);
        }
    }
}
