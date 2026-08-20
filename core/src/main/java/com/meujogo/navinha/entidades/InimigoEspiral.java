package com.meujogo.navinha.entidades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Pool;
import java.util.ArrayList;

public class InimigoEspiral extends Inimigo {

    private float tempo = 0;
    private float centroX;

    public InimigoEspiral() {
        super();
    }

    @Override
    public void init(Texture imagem, float x, float y) {
        super.init(imagem, x, y);
        this.centroX = x; // Guarda a posição original para fazer o vai-e-vem
    }

    @Override
    public void atualizar(float delta, ArrayList<TiroInimigo> listaTirosInimigos, Pool<TiroInimigo> poolTirosInimigos) {
        if (!ativo) return;

        tempo += delta;

        // Faz a nave descer continuamente pela tela
        this.y -= 150f * delta;

        // Aplica o movimento em senóide usando o centro original gravado no init
        this.x = centroX + (float) Math.sin(tempo * 5f) * 100f;

        // Desativa se passar do fundo da tela
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
