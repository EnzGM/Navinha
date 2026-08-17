package com.meujogo.navinha.entidades;

import com.badlogic.gdx.graphics.Texture;

public class InimigoEspiral extends Inimigo {

    private float angulo = 0;
    private float raio = 60; // Tamanho do círculo da espiral
    private float centroX;   // Ponto central X que vai descendo
    private float centroY;   // Ponto central Y que vai descendo

    public InimigoEspiral() {}

    public void init(Texture imagem, float x, float y, float atraso) {
        // Inicializa imagem e tamanho reduzido da classe Inimigo
        super.init(imagem, x, y);

        this.velocidade = 90; // Velocidade de descida do centro
        this.centroX = x;
        this.centroY = y;
        this.angulo = atraso;  // Ângulo inicial de cada um na fila
        this.raio = 60;
    }

    @Override
    public void atualizar(float delta) {
        // 1. O centro da rotação vai caindo pela tela
        this.centroY -= this.velocidade * delta;

        // 2. O ângulo roda constantemente
        this.angulo += 4.0f * delta;

        // 3. Calcula a posição em volta do centro usando Cosseno (X) e Seno (Y)
        this.x = centroX + (float) Math.cos(angulo) * raio;
        this.y = centroY + (float) Math.sin(angulo) * raio;

        // Desativa quando o centro passar do fundo da tela
        if (this.centroY < -100) {
            this.ativo = false;
        }
    }
}
