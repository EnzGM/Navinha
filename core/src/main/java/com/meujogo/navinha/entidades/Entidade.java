package com.meujogo.navinha.entidades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Pool.Poolable;

// "implements Poolable" diz ao LibGDX que esta classe pode ser reciclada.
// Isso evita que o jogo fique criando e destruindo objetos na memória o tempo todo (evita travamentos do Garbage Collector)[cite: 3].
public abstract class Entidade implements Poolable {

    // Posição do objeto na tela (eixos X e Y)[cite: 3]
    public float x, y;

    // Velocidade de movimento do objeto[cite: 3]
    public float velocidade;

    // A imagem que será desenhada na tela[cite: 3]
    public Texture imagem;

    // O tamanho da "caixa" do objeto, usado para desenhar e para calcular colisões[cite: 3]
    public float largura, altura;

    // Controla se o objeto está vivo no jogo. Se for false, ele some e volta pro Pool[cite: 3]
    public boolean ativo = true;

    // Primeiro inicializador: Usado quando passamos uma textura e queremos que o tamanho
    // da entidade seja exatamente o tamanho original da imagem[cite: 3].
    public void init(Texture imagem, float x, float y, float velocidade) {
        this.imagem = imagem;
        this.x = x;
        this.y = y;
        this.velocidade = velocidade;

        // Só pega a largura e altura se a imagem não for nula, para evitar erro (NullPointerException)[cite: 3].
        if (imagem != null) {
            this.largura = imagem.getWidth();
            this.altura = imagem.getHeight();
        }
        this.ativo = true;
    }

    // Segundo inicializador: Permite forçar uma LARGURA e ALTURA personalizadas.
    // Útil se você quiser desenhar a imagem maior ou menor que o tamanho original do arquivo[cite: 3].
    public void init(Texture imagem, float x, float y, float largura, float altura, float velocidade) {
        this.imagem = imagem;
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.velocidade = velocidade;
        this.ativo = true;
    }

    // Terceiro inicializador: NÃO recebe imagem.
    // Criado especialmente para objetos que são desenhados via código (como o TiroInimigo).
    public void init(float x, float y, float largura, float altura, float velocidade) {
        this.imagem = null; // Garante que não tem textura
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.velocidade = velocidade;
        this.ativo = true;
    }

    // Método abstrato: Toda classe que herdar de Entidade é OBRIGADA a ter um método atualizar().
    // O "delta" é o tempo que passou desde o último frame, usado para movimento fluido[cite: 3].
    public abstract void atualizar(float delta);

    // Pega a textura e desenha na tela usando as posições e tamanhos definidos[cite: 3].
    public void desenhar(SpriteBatch batch) {
        // Só desenha se a entidade estiver ativa e se possuir uma imagem carregada[cite: 3]
        if (this.imagem != null && this.ativo) {
            batch.draw(imagem, x, y, largura, altura);
        }
    }

    // Cria e retorna um Retângulo invisível em volta da entidade.
    // É isso que usamos para checar se um tiro bateu na nave (Colisão)[cite: 3].
    public Rectangle getBounds() {
        return new Rectangle(x, y, largura, altura);
    }

    // Método antigo mantido apenas caso alguma outra classe do seu jogo ainda chame por esse nome[cite: 3].
    public Rectangle getCaixaDeColisao() {
        return getBounds();
    }

    // MÉTODO MAIS IMPORTANTE DO POOLING:
    // Quando o objeto "morre", ele é devolvido para a piscina (Pool). O LibGDX chama esse método
    // automaticamente para "limpar" o objeto, deixando-o zerado para ser usado novamente no futuro[cite: 3].
    @Override
    public void reset() {
        this.x = 0;
        this.y = 0;
        this.velocidade = 0;
        this.ativo = false; // Desativa para não aparecer na tela[cite: 3]
        this.imagem = null; // Remove a textura para não prender memória de bobeira[cite: 3]
    }
}
