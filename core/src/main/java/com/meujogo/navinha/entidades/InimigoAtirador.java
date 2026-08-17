package com.meujogo.navinha.entidades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Pool;
import java.util.ArrayList;

public class InimigoAtirador extends Inimigo {

    private float tempoParaAtirar = 0;

    public InimigoAtirador() {}

    @Override
    public void init(Texture imagem, float x, float y) {
        super.init(imagem, x, y); // Aplica tamanho reduzido e posição

        // SEGREDO DA DESINCRONIZAÇÃO:
        // Sorteia um número inicial aleatório entre 0.0 e 2.0 segundos.
        // Assim, cada atirador começa a contar o tempo de um ponto diferente!
        this.tempoParaAtirar = (float) (Math.random() * 2.0f);
    }

    @Override
    public void atualizar(float delta, ArrayList<TiroInimigo> tirosInimigos, Pool<TiroInimigo> poolTirosInimigos) {
        super.atualizar(delta);

        tempoParaAtirar += delta;

        // Atira a cada 2.5 segundos (individual de cada inimigo)
        if (tempoParaAtirar >= 2.5f) {
            tempoParaAtirar = 0; // Reseta o contador apenas deste inimigo

            if (poolTirosInimigos != null && tirosInimigos != null) {
                TiroInimigo tiro = poolTirosInimigos.obtain();
                tiro.init(this.x + (this.largura / 2f) - 2, this.y);
                tirosInimigos.add(tiro);
            }
        }
    }
}
