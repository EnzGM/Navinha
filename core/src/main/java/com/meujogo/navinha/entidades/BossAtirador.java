package com.meujogo.navinha.entidades;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Pool;
import java.util.ArrayList;

public class BossAtirador extends Inimigo {

    public int vidaMax = 20;
    public int vida = 20;
    private float tempoTiro = 0;
    private float intervaloTiro = 1.0f;
    private int direcaoX = 1;

    // NOVO: controla os padrões de tiro do boss
    private int contadorRajadas = 0;

    // Texturas para a barra de vida
    private static Texture textFundoVida;
    private static Texture textVida;

    public BossAtirador() {
        super();
    }

    @Override
    public void init(Texture imagem, float x, float y) {
        super.init(imagem, x, y);

        // Deixa o Boss grandão (96x96)
        this.largura = 96;
        this.altura = 96;

        this.vidaMax = 20;
        this.vida = this.vidaMax;
        this.direcaoX = 1;

        // Cria as texturas da barra de vida (Vermelha)
        if (textFundoVida == null) {
            Pixmap pixBg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixBg.setColor(Color.DARK_GRAY);
            pixBg.fill();
            textFundoVida = new Texture(pixBg);
            pixBg.dispose();

            Pixmap pixFg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixFg.setColor(Color.RED);
            pixFg.fill();
            textVida = new Texture(pixFg);
            pixFg.dispose();
        }
    }

    @Override
    public void atualizar(float delta, ArrayList<TiroInimigo> listaTirosInimigos, Pool<TiroInimigo> poolTirosInimigos) {
        if (!ativo) return;

        // Movimento lateral independente (vai e vem)
        this.x += direcaoX * velocidade * delta;

        if (this.x <= 10) {
            this.x = 10;
            direcaoX = 1;
        } else if (this.x >= Gdx.graphics.getWidth() - largura - 10) {
            this.x = Gdx.graphics.getWidth() - largura - 10;
            direcaoX = -1;
        }

        // Lógica de tiro
        tempoTiro += delta;

        // Fase de fúria: abaixo de 50% de vida o boss atira bem mais rápido
        boolean enfurecido = vida <= vidaMax / 2;
        float intervaloAtual = enfurecido ? intervaloTiro * 0.55f : intervaloTiro;

        if (tempoTiro >= intervaloAtual && listaTirosInimigos != null && poolTirosInimigos != null) {
            tempoTiro = 0;
            contadorRajadas++;

            float baseX = this.x + (this.largura / 2f) - 4;
            float baseY = this.y - 16;

            // A cada 4 disparos, solta uma rajada especial bem mais ampla
            if (contadorRajadas % 4 == 0) {
                disparar(listaTirosInimigos, poolTirosInimigos, baseX, baseY,
                    new float[]{-320, -220, -110, 0, 110, 220, 320}, 260);
            } else {
                // Disparo padrão: leque de 3 projéteis
                disparar(listaTirosInimigos, poolTirosInimigos, baseX, baseY,
                    new float[]{-150, 0, 150}, 320);
            }
        }
    }

    // NOVO: cria vários projéteis de uma vez, cada um com uma velocidade horizontal (velX)
    // diferente, formando um leque. velocidadeY é a velocidade de descida, igual para todos.
    private void disparar(ArrayList<TiroInimigo> listaTirosInimigos, Pool<TiroInimigo> poolTirosInimigos,
                          float baseX, float baseY, float[] velocidadesX, float velocidadeY) {
        for (float vx : velocidadesX) {
            TiroInimigo ti = poolTirosInimigos.obtain();
            ti.init(baseX, baseY, 8, 16, vx, velocidadeY);
            listaTirosInimigos.add(ti);
        }
    }

    public void tomarDano(int dano) {
        vida -= dano;
        if (vida <= 0) {
            this.ativo = false;
        }
    }

    @Override
    public void desenhar(SpriteBatch batch) {
        super.desenhar(batch);

        if (this.ativo && textFundoVida != null && textVida != null) {
            float larguraBarra = Gdx.graphics.getWidth() * 0.7f;
            float alturaBarra = 12;
            float xBarra = (Gdx.graphics.getWidth() - larguraBarra) / 2f;
            float yBarra = Gdx.graphics.getHeight() - 75;

            batch.draw(textFundoVida, xBarra, yBarra, larguraBarra, alturaBarra);

            float pctVida = (float) vida / (float) vidaMax;
            if (pctVida > 0) {
                batch.draw(textVida, xBarra, yBarra, larguraBarra * pctVida, alturaBarra);
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        vida = 20;
        tempoTiro = 0;
        direcaoX = 1;
        contadorRajadas = 0;
    }
}
