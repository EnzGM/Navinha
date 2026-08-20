package com.meujogo.navinha.entidades;

import com.badlogic.gdx.graphics.Texture;
import java.util.ArrayList;

public class TiroHoming extends Entidade {

    private float velocidadeX = 0;
    private float velocidadeY = 400;

    public TiroHoming() {}

    public void init(Texture imagem, float x, float y) {
        super.init(x, y, 6, 12, 400);
        this.imagem = imagem;
        this.velocidadeX = 0;
        this.velocidadeY = 400;
    }

    // Método obrigatório exigido pela classe abstrata Entidade
    @Override
    public void atualizar(float delta) {
        if (!ativo) return;
        // Movimento padrão reto caso não seja chamado com a lista
        this.x += velocidadeX * delta;
        this.y += velocidadeY * delta;

        if (this.y > com.badlogic.gdx.Gdx.graphics.getHeight() || this.x < -20 || this.x > com.badlogic.gdx.Gdx.graphics.getWidth() + 20) {
            this.ativo = false;
        }
    }

    // Sobrecarga para o rastreamento dos inimigos
    public void atualizar(float delta, ArrayList<Inimigo> listaInimigos) {
        if (!ativo) return;

        Inimigo alvoMaisProximo = null;
        float menorDistanciaSq = Float.MAX_VALUE;

        if (listaInimigos != null) {
            for (Inimigo ini : listaInimigos) {
                if (ini.ativo) {
                    float distSq = (ini.x - this.x) * (ini.x - this.x) + (ini.y - this.y) * (ini.y - this.y);
                    if (distSq < menorDistanciaSq) {
                        menorDistanciaSq = distSq;
                        alvoMaisProximo = ini;
                    }
                }
            }
        }

        if (alvoMaisProximo != null) {
            float alvoX = alvoMaisProximo.x + (alvoMaisProximo.largura / 2f);
            float alvoY = alvoMaisProximo.y + (alvoMaisProximo.altura / 2f);

            float dirX = alvoX - this.x;
            float dirY = alvoY - this.y;
            float comprimento = (float) Math.sqrt(dirX * dirX + dirY * dirY);

            if (comprimento != 0) {
                dirX /= comprimento;
                dirY /= comprimento;
            }

            // Manteve a lógica de curva suave e veloz até o alvo
            float velocidadeMaxima = 550f;
            float fatorCurva = 8f * delta;

            this.velocidadeX = this.velocidadeX + (dirX * velocidadeMaxima - this.velocidadeX) * fatorCurva;
            this.velocidadeY = this.velocidadeY + (dirY * velocidadeMaxima - this.velocidadeY) * fatorCurva;
        }

        this.x += velocidadeX * delta;
        this.y += velocidadeY * delta;

        if (y > com.badlogic.gdx.Gdx.graphics.getHeight() || x < -50 || x > com.badlogic.gdx.Gdx.graphics.getWidth() + 50) {
            this.ativo = false;
        }
    }

    @Override
    public void reset() {
        super.reset();
        this.velocidadeX = 0;
        this.velocidadeY = 400;
    }
}
