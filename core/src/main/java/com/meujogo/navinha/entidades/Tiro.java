package com.meujogo.navinha.entidades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayList;

public class Tiro extends Entidade {

    public float velocidadeX = 0;
    public float velocidadeY = 500;
    public boolean ehHoming = false;

    // NOVO: Assim como no tiro inimigo, criamos uma textura estática (compartilhada)
    // para não gastar memória carregando uma imagem para cada tiro.
    private static Texture texturaDesenhada;

    public Tiro() {} // Necessário para a Pool

    // NOVO: Inicializador principal sem imagem (desenhado por código)
    public void init(float x, float y) {
        // Inicializamos usando o método da Entidade: x, y, Largura(6), Altura(16), Velocidade(500)
        super.init(x, y, 6, 16, 500);

        this.velocidadeX = 0;
        this.velocidadeY = 500;
        this.ehHoming = false;
        this.ativo = true;

        // Se a textura procedural ainda não existir, nós a criamos na memória
        if (texturaDesenhada == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

            // Cor do tiro do jogador: CYAN (Ciano/Azul claro). Você pode mudar para GREEN, BLUE, etc.
            pixmap.setColor(Color.CYAN);
            pixmap.fill();

            texturaDesenhada = new Texture(pixmap);
            pixmap.dispose(); // Limpa o pixmap da RAM após gerar a textura
        }
    }

    // TRUQUE DE COMPATIBILIDADE:
    // Como a sua classe MeuJogo.java ainda tenta passar uma imagem (imgTiro) neste método,
    // nós recebemos a imagem, ignoramos ela, e chamamos o init(x, y) logo acima que usa a cor desenhada!
    public void init(Texture imagem, float x, float y) {
        init(x, y);
    }

    @Override
    public void atualizar(float delta) {
        atualizar(delta, new ArrayList<Inimigo>());
    }

    public void atualizar(float delta, ArrayList<Inimigo> listaInimigos) {
        if (ehHoming && !listaInimigos.isEmpty()) {
            Inimigo alvo = listaInimigos.get(0);
            if (alvo.x > this.x) this.x += 150 * delta;
            if (alvo.x < this.x) this.x -= 150 * delta;
        } else {
            this.x += velocidadeX * delta;
        }

        this.y += velocidadeY * delta;

        if (this.y > 1000) {
            this.ativo = false;
        }
    }

    // NOVO: Sobrescrevemos o desenhar para usar a nossa texturaDesenhada Ciano
    @Override
    public void desenhar(SpriteBatch batch) {
        if (ativo && texturaDesenhada != null) {
            batch.draw(texturaDesenhada, x, y, largura, altura);
        }
    }

    @Override
    public void reset() {
        super.reset();
        this.velocidadeX = 0;
        this.velocidadeY = 500;
        this.ehHoming = false;
    }
}
