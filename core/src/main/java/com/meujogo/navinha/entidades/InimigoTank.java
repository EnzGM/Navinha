package com.meujogo.navinha.entidades;

import com.badlogic.gdx.utils.Pool;
import java.util.ArrayList;

public class InimigoTank extends Inimigo {

    public int vida = 5;

    public InimigoTank() {
        super();
    }

    @Override
    public void atualizar(float delta, ArrayList<TiroInimigo> listaTirosInimigos, Pool<TiroInimigo> poolTirosInimigos) {
        // CORREÇÃO: Passando as listas no super (ele só se move e aguenta mais tiros)
        super.atualizar(delta, listaTirosInimigos, poolTirosInimigos);
    }

    public void tomarDano(int dano) {
        vida -= dano;
        if (vida <= 0) {
            this.ativo = false;
        }
    }

    @Override
    public void reset() {
        super.reset();
        vida = 5;
    }
}
