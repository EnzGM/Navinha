package com.meujogo.navinha.entidades;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Pool;
import java.util.ArrayList;

public class BossInvocador extends Inimigo {

    public int vidaMax = 50;
    public int vidaAtual = 50;
    private float tempoInvocacao = 0;
    private int direcaoX = 1;

    private static Texture textFundoVida;
    private static Texture textVida;

    public BossInvocador() {}

    @Override
    public void init(Texture imagem, float x, float y) {
        super.init(imagem, x, y);
        this.vidaMax = 50;
        this.vidaAtual = this.vidaMax;
        this.velocidade = 100;

        if (imagem != null) {
            // Boss gigante (3x o tamanho padrão)
            this.largura = (imagem.getWidth() / 3f) * 3.0f;
            this.altura = (imagem.getHeight() / 3f) * 3.0f;
        }

        if (textFundoVida == null) {
            Pixmap pixBg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixBg.setColor(Color.DARK_GRAY);
            pixBg.fill();
            textFundoVida = new Texture(pixBg);
            pixBg.dispose();

            Pixmap pixFg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixFg.setColor(Color.PURPLE); // Barra de vida roxa!
            pixFg.fill();
            textVida = new Texture(pixFg);
            pixFg.dispose();
        }
    }

    public void tomarDano(int dano) {
        this.vidaAtual -= dano;
        if (this.vidaAtual <= 0) {
            this.ativo = false;
        }
    }

    // Método customizado para atualizar o Boss e gerar os Minions na lista do jogo
    public void atualizarBoss(float delta, ArrayList<Inimigo> listaInimigos, Pool<Minion> poolMinions, Texture imgMinion) {
        if (!ativo) return;

        // Entra até o topo da tela
        float limiteSuperiorY = Gdx.graphics.getHeight() - 180;
        if (this.y > limiteSuperiorY) {
            this.y -= 70 * delta;
        } else {
            // Movimento lateral
            this.x += direcaoX * velocidade * delta;

            if (this.x <= 10) {
                this.x = 10;
                direcaoX = 1;
            } else if (this.x >= Gdx.graphics.getWidth() - largura - 10) {
                this.x = Gdx.graphics.getWidth() - largura - 10;
                direcaoX = -1;
            }
        }

        // Invocação periódica
        tempoInvocacao += delta;
        if (tempoInvocacao >= 2.5f) {
            tempoInvocacao = 0;
            invocarMinions(listaInimigos, poolMinions, imgMinion);
        }
    }

    private void invocarMinions(ArrayList<Inimigo> listaInimigos, Pool<Minion> poolMinions, Texture imgMinion) {
        if (poolMinions == null || listaInimigos == null) return;

        // Spawna 3 minions espalhados em leque abaixo do Boss (posição inicial diferente,
        // mas cada um desce reto balançando em torno do próprio X de nascimento — ver Minion.java).
        float[] offsetsX = {-largura * 0.3f, 0f, largura * 0.3f};
        for (float offset : offsetsX) {
            Minion m = poolMinions.obtain();
            m.init(imgMinion, (this.x + largura / 2f) + offset, this.y);
            listaInimigos.add(m);
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

            float pctVida = (float) vidaAtual / (float) vidaMax;
            if (pctVida > 0) {
                batch.draw(textVida, xBarra, yBarra, larguraBarra * pctVida, alturaBarra);
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
        this.vidaAtual = 0;
        this.tempoInvocacao = 0;
    }
}
