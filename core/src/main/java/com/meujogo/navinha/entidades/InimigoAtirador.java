package com.meujogo.navinha.entidades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Pool;
import java.util.ArrayList;

public class InimigoAtirador extends Inimigo {

    private float tempoTiro = 0;
    private float intervaloTiro = 1.5f;

    public InimigoAtirador() {
        super();
    }

    @Override
    public void atualizar(float delta, ArrayList<TiroInimigo> listaTirosInimigos, Pool<TiroInimigo> poolTirosInimigos) {
        super.atualizar(delta, listaTirosInimigos, poolTirosInimigos);

        if (!ativo) return;

        tempoTiro += delta;
        if (tempoTiro >= intervaloTiro && listaTirosInimigos != null && poolTirosInimigos != null) {
            tempoTiro = 0;
            TiroInimigo ti = poolTirosInimigos.obtain();

            // CORREÇÃO: Usando o init de 6 argumentos para forçar tamanho 8x16 no tiro comum
            ti.init(this.imagem, this.x + (this.largura / 2f) - 4, this.y - 16, 8, 16, -300);
            listaTirosInimigos.add(ti);
        }
    }

    @Override
    public void reset() {
        super.reset();
        tempoTiro = 0;
    }
}
