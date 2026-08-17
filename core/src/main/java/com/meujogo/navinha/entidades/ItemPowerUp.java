package com.meujogo.navinha.entidades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class ItemPowerUp extends Entidade {

    // O tipo de poder que esse item dá:
    // 1 = Spread (Espalhado), 2 = Homing (Perseguidor), 3 = Burst (Rajada)
    public int tipoPoder;

    // Um leve "flutuar" pra ficar mais fácil de ver caindo (puramente visual)
    private float tempoVida = 0;

    // Cada tipo de power-up tem sua própria textura de cor sólida, gerada uma
    // única vez via Pixmap (mesmo truque usado em Tiro.java e TiroInimigo.java).
    private static Texture texturaSpread;  // Verde
    private static Texture texturaHoming;  // Magenta
    private static Texture texturaBurst;   // Amarelo

    public ItemPowerUp() {} // Necessário para a Pool

    public void init(float x, float y, int tipo) {
        // X, Y, Largura(15), Altura(15), Velocidade(150 - cai mais devagar que o tiro)
        super.init(x, y, 15, 15, 150);
        this.tipoPoder = tipo; // Guarda qual poder ele carrega
        this.tempoVida = 0;

        criarTexturasSeNecessario();
    }

    private void criarTexturasSeNecessario() {
        if (texturaSpread == null) texturaSpread = criarTexturaColorida(Color.LIME);
        if (texturaHoming == null) texturaHoming = criarTexturaColorida(Color.MAGENTA);
        if (texturaBurst == null) texturaBurst = criarTexturaColorida(Color.YELLOW);
    }

    private Texture criarTexturaColorida(Color cor) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(cor);
        pixmap.fill();
        Texture textura = new Texture(pixmap);
        pixmap.dispose();
        return textura;
    }

    @Override
    public void atualizar(float delta) {
        // O item sempre cai (vai para baixo no eixo Y)
        this.y -= this.velocidade * delta;
        this.tempoVida += delta;

        // Se sair da tela por baixo, desativa para ser reciclado
        if (this.y < -this.altura) {
            this.ativo = false;
        }
    }

    // Escolhe a textura certa de acordo com o tipo de poder e desenha.
    // Como o init() não guarda nada em "imagem", precisamos sobrescrever aqui
    // (senão o desenhar() da Entidade não desenharia nada).
    @Override
    public void desenhar(SpriteBatch batch) {
        if (!ativo) return;

        Texture textura;
        switch (tipoPoder) {
            case 1:  textura = texturaSpread; break;
            case 2:  textura = texturaHoming; break;
            case 3:  textura = texturaBurst;  break;
            default: textura = texturaSpread; break;
        }

        if (textura != null) {
            batch.draw(textura, x, y, largura, altura);
        }
    }

    // Nome curto do poder, útil pra mostrar no HUD quando o jogador pega o item
    public String getNome() {
        switch (tipoPoder) {
            case 1:  return "SPREAD";
            case 2:  return "HOMING";
            case 3:  return "BURST";
            default: return "?";
        }
    }

    @Override
    public void reset() {
        super.reset();
        this.tipoPoder = 0;
        this.tempoVida = 0;
    }
}
