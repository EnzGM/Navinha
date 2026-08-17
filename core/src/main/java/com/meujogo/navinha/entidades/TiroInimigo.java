package com.meujogo.navinha.entidades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TiroInimigo extends Entidade {

    // A palavra "static" significa que esta variável pertence à CLASSE inteira, e não a um tiro específico.
    // Ou seja: se tivermos 50 tiros na tela, TODOS vão compartilhar essa mesma imagem gerada,
    // economizando muita memória e processamento.
    private static Texture texturaDesenhada;

    // Construtor vazio necessário para o sistema de Pooling funcionar[cite: 10]
    public TiroInimigo() {}

    // O método de inicialização quando um inimigo dispara o tiro[cite: 10]
    public void init(float x, float y) {

        // Chamamos o init SEM TEXTURA da classe pai (Entidade), configurando:
        // Posição (x,y), Largura=4, Altura=12, Velocidade=350.
        // O tiro inimigo é pequeno (4x12) e vai para baixo[cite: 10].
        super.init(x, y, 4, 12, 350);

        // Se a textura via código ainda NÃO foi gerada (é o primeiro tiro inimigo do jogo), nós criamos ela.
        if (texturaDesenhada == null) {

            // Pixmap é uma "tela de pintura" em memória ram. Criamos um quadradinho de 1 pixel de largura por 1 de altura.
            // O formato RGBA8888 permite usar cores sólidas ou transparentes.
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);

            // Definimos a cor do "pincel" para Laranja.
            pixmap.setColor(Color.ORANGE);

            // Preenchemos todo o pixmap (que tem só 1 pixel) com essa cor.
            pixmap.fill();

            // Convertemos esse Pixmap de 1 pixel em uma Textura (que vai pra placa de vídeo).
            texturaDesenhada = new Texture(pixmap);

            // Como a Textura já foi criada, não precisamos mais do Pixmap na memória RAM, então descartamos ele para evitar vazamento.
            pixmap.dispose();
        }
    }

    // Calcula a física do tiro inimigo a cada frame[cite: 10]
    @Override
    public void atualizar(float delta) {

        // Diminui o Y do tiro, fazendo ele descer na tela (porque o (0,0) é embaixo no LibGDX)[cite: 10].
        this.y -= this.velocidade * delta;

        // Se o tiro sair completamente por baixo da tela (y for menor que o próprio tamanho da altura negado),
        // ele morre e é devolvido pro Pool[cite: 10].
        if (this.y < -this.altura) {
            this.ativo = false;
        }
    }

    // Como o init() não salva textura em `this.imagem`, a classe pai não desenharia nada.
    // Então, nós SOBRESCREVEMOS o método de desenhar para usar a nossa textura gerada (texturaDesenhada).
    @Override
    public void desenhar(SpriteBatch batch) {

        // Se o tiro está ativo e a textura procedural foi criada com sucesso:
        if (ativo && texturaDesenhada != null) {

            // Desenhamos a textura de 1x1 pixel na posição (x,y).
            // A mágica: passamos "largura" (4) e "altura" (12). O LibGDX pega aquele 1 único pixel laranja
            // e ESTICA ele como se fosse uma "lingueta" vertical, formando um tiro de energia laser amarelo/laranja!
            batch.draw(texturaDesenhada, x, y, largura, altura);
        }
    }
}
