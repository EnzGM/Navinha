package com.meujogo.navinha;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Pool;
import com.meujogo.navinha.entidades.*;

import java.util.ArrayList;
import java.util.Iterator;

public class MeuJogo extends ApplicationAdapter {

    private SpriteBatch batch;
    private Texture imgFundo;
    private Texture imgNave;
    private Texture imgInimigo;
    private Texture imgInimigoAtirador;

    private BitmapFont font;
    private Nave jogador;

    private ArrayList<Inimigo> listaInimigos;
    private ArrayList<Tiro> listaTiros;
    private ArrayList<TiroInimigo> listaTirosInimigos;
    private ArrayList<ItemPowerUp> listaPowerUps;

    // Pools de objetos para otimização de memória
    private Pool<Inimigo> poolInimigos;
    private Pool<InimigoAtirador> poolAtiradores;
    private Pool<InimigoEspiral> poolEspiral;
    private Pool<Tiro> poolTiros;
    private Pool<TiroInimigo> poolTirosInimigos;
    private Pool<ItemPowerUp> poolPowerUps;

    // Chance de um inimigo abatido soltar um power-up (18%)
    private static final float CHANCE_DROP_POWERUP = 0.18f;

    // Controle de tempo, estado e Game Over
    private float tempoParaAtirar = 0;
    private int pontuacao = 0;
    private int ondaAtual = 1;

    // === NOVO: Estado atual do jogo (substitui o "boolean gameOver") ===
    private EstadoJogo estado = EstadoJogo.MENU;

    // Controle da rajada (Burst)
    private int tirosRestantesRajada = 0;
    private float tempoProximoTiroRajada = 0;

    @Override
    public void create() {
        batch = new SpriteBatch();

        font = new BitmapFont();
        font.getData().setScale(1.2f);

        // Carregamento de Texturas
        imgFundo = new Texture("fundo.png");
        imgNave = new Texture("nave.png");
        imgInimigo = new Texture("inimigo.png");
        imgInimigoAtirador = new Texture("inimigo_atirador.png");

        // Criar Jogador
        float startX = Gdx.graphics.getWidth() / 2f - (imgNave.getWidth() / 4f);
        float startY = 30;
        jogador = new Nave(imgNave, startX, startY);

        // Listas de Entidades Visíveis
        listaInimigos = new ArrayList<>();
        listaTiros = new ArrayList<>();
        listaTirosInimigos = new ArrayList<>();
        listaPowerUps = new ArrayList<>();

        // Inicialização dos Pools
        poolInimigos = new Pool<Inimigo>() {
            @Override
            protected Inimigo newObject() {
                return new Inimigo();
            }
        };

        poolAtiradores = new Pool<InimigoAtirador>() {
            @Override
            protected InimigoAtirador newObject() {
                return new InimigoAtirador();
            }
        };

        poolEspiral = new Pool<InimigoEspiral>() {
            @Override
            protected InimigoEspiral newObject() {
                return new InimigoEspiral();
            }
        };

        poolTiros = new Pool<Tiro>() {
            @Override
            protected Tiro newObject() {
                return new Tiro();
            }
        };

        poolTirosInimigos = new Pool<TiroInimigo>() {
            @Override
            protected TiroInimigo newObject() {
                return new TiroInimigo();
            }
        };

        poolPowerUps = new Pool<ItemPowerUp>() {
            @Override
            protected ItemPowerUp newObject() {
                return new ItemPowerUp();
            }
        };

        // NOVO: não carrega a onda aqui. Começamos no MENU e a onda 1
        // só é carregada quando o jogador aperta ENTER (veja iniciarJogo()).
        estado = EstadoJogo.MENU;
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // Limpa a tela (feito sempre, em qualquer estado)
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // === A MÁQUINA DE ESTADOS ===
        // Cada estado decide o que atualizar e o que desenhar.
        switch (estado) {
            case MENU:
                atualizarMenu();
                desenharMenu();
                break;

            case JOGANDO:
                atualizarJogo(delta);
                desenharJogo();
                break;

            case PAUSADO:
                atualizarPausa();
                desenharJogo();     // desenha o jogo "congelado" por trás
                desenharPausa();    // e por cima desenha o aviso de pausa
                break;

            case GAME_OVER:
                atualizarGameOver();
                desenharJogo();     // mostra a cena final por trás
                desenharGameOver(); // e por cima o texto de game over
                break;
        }
    }

    // =========================================================
    // ===================== ESTADO: MENU =====================
    // =========================================================

    private void atualizarMenu() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            iniciarJogo();
        }
    }

    private void desenharMenu() {
        batch.begin();
        batch.draw(imgFundo, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        font.draw(batch, "=== NAVINHA ===", Gdx.graphics.getWidth() / 2f - 70, Gdx.graphics.getHeight() / 2f + 40);
        font.draw(batch, "Pressione ENTER para comecar", Gdx.graphics.getWidth() / 2f - 115, Gdx.graphics.getHeight() / 2f);
        font.draw(batch, "Setas ou A/D para mover, ESPACO para atirar", Gdx.graphics.getWidth() / 2f - 160, Gdx.graphics.getHeight() / 2f - 30);
        font.draw(batch, "P para pausar", Gdx.graphics.getWidth() / 2f - 55, Gdx.graphics.getHeight() / 2f - 55);

        batch.end();
    }

    // =========================================================
    // ==================== ESTADO: JOGANDO ====================
    // =========================================================

    private void atualizarJogo(float delta) {

        // Tecla de pausa. Pode trocar Input.Keys.P por ESCAPE se preferir.
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            estado = EstadoJogo.PAUSADO;
            return; // não processa mais nada neste frame, já entra pausado
        }

        // === ATUALIZAÇÃO DO JOGADOR ===
        jogador.atualizar(delta);

        // Troca de armas para testes (Teclas 1, 2, 3, 4)
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) jogador.tipoArmaAtual = 0; // Normal
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) jogador.tipoArmaAtual = 1; // Spread
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) jogador.tipoArmaAtual = 2; // Homing
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) jogador.tipoArmaAtual = 3; // Burst

        // === LÓGICA DE ATIRAR DO JOGADOR ===
        tempoParaAtirar += delta;

        float xDoTiro = jogador.x + (jogador.largura / 2f) - 3f;
        float yDoTiro = jogador.y + jogador.altura;

        // 1. Disparo de Rajada Encadeada (BURST)
        if (tirosRestantesRajada > 0) {
            tempoProximoTiroRajada += delta;

            if (tempoProximoTiroRajada >= 0.08f) {
                Tiro t = poolTiros.obtain();
                t.init(xDoTiro, yDoTiro);
                listaTiros.add(t);

                tirosRestantesRajada--;
                tempoProximoTiroRajada = 0;
            }
        }

        // 2. Disparo Manual ao apertar ESPAÇO
        // Define um intervalo maior de pausa se a arma atual for o Burst (ex: 0.8 segundos de pausa)
        float cooldownAtual = (jogador.tipoArmaAtual == 3) ? 0.8f : 0.35f;

// 2. Disparo Manual ao apertar ESPAÇO
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && tempoParaAtirar >= cooldownAtual && tirosRestantesRajada == 0) {

            if (jogador.tipoArmaAtual == 0) { // NORMAL
                Tiro t = poolTiros.obtain();
                t.init(xDoTiro, yDoTiro);
                listaTiros.add(t);

            } else if (jogador.tipoArmaAtual == 1) { // SPREAD
                Tiro t1 = poolTiros.obtain(); t1.init(xDoTiro, yDoTiro);

                Tiro t2 = poolTiros.obtain(); t2.init(xDoTiro, yDoTiro);
                t2.velocidadeX = -120;

                Tiro t3 = poolTiros.obtain(); t3.init(xDoTiro, yDoTiro);
                t3.velocidadeX = 120;

                listaTiros.add(t1); listaTiros.add(t2); listaTiros.add(t3);

            } else if (jogador.tipoArmaAtual == 2) { // HOMING
                Tiro t = poolTiros.obtain();
                t.init(xDoTiro, yDoTiro);
                t.ehHoming = true;
                listaTiros.add(t);

            } else if (jogador.tipoArmaAtual == 3) { // BURST
                tirosRestantesRajada = 3;
                tempoProximoTiroRajada = 0.35f;
            }

            tempoParaAtirar = 0;
        }

        // === ATUALIZAR TIROS DO JOGADOR ===
        Iterator<Tiro> itTiro = listaTiros.iterator();
        while (itTiro.hasNext()) {
            Tiro t = itTiro.next();
            t.atualizar(delta, listaInimigos);
            if (!t.ativo) {
                itTiro.remove();
                poolTiros.free(t);
            }
        }

        // === ATUALIZAR TIROS DOS INIMIGOS ===
        Iterator<TiroInimigo> itTiroInimigo = listaTirosInimigos.iterator();
        while (itTiroInimigo.hasNext()) {
            TiroInimigo ti = itTiroInimigo.next();
            ti.atualizar(delta);

            if (ti.ativo && ti.getBounds().overlaps(jogador.getBounds())) {
                ti.ativo = false;
                jogador.vidas--;
            }

            if (!ti.ativo) {
                itTiroInimigo.remove();
                poolTirosInimigos.free(ti);
            }
        }

        // === ATUALIZAR INIMIGOS ===
        Iterator<Inimigo> itInimigo = listaInimigos.iterator();
        while (itInimigo.hasNext()) {
            Inimigo ini = itInimigo.next();

            ini.atualizar(delta, listaTirosInimigos, poolTirosInimigos);

            for (Tiro t : listaTiros) {
                if (t.ativo && ini.ativo && t.getBounds().overlaps(ini.getBounds())) {
                    t.ativo = false;
                    ini.ativo = false;
                    pontuacao += 100;

                    // Chance do inimigo abatido soltar um power-up no lugar onde morreu
                    tentarDropPowerUp(ini.x + ini.largura / 2f, ini.y);
                }
            }

            if (ini.ativo && ini.getBounds().overlaps(jogador.getBounds())) {
                ini.ativo = false;
                jogador.vidas--;
            }

            if (!ini.ativo) {
                itInimigo.remove();
                if (ini instanceof InimigoEspiral) {
                    poolEspiral.free((InimigoEspiral) ini);
                } else if (ini instanceof InimigoAtirador) {
                    poolAtiradores.free((InimigoAtirador) ini);
                } else {
                    poolInimigos.free(ini);
                }
            }
        }

        // === ATUALIZAR POWER-UPS (caindo + coleta pelo jogador) ===
        Iterator<ItemPowerUp> itPowerUp = listaPowerUps.iterator();
        while (itPowerUp.hasNext()) {
            ItemPowerUp p = itPowerUp.next();
            p.atualizar(delta);

            if (p.ativo && p.getBounds().overlaps(jogador.getBounds())) {
                jogador.tipoArmaAtual = p.tipoPoder; // 1=Spread, 2=Homing, 3=Burst (mesmos códigos da Nave)
                p.ativo = false;
            }

            if (!p.ativo) {
                itPowerUp.remove();
                poolPowerUps.free(p);
            }
        }

        // Verifica se as vidas acabaram -> muda para o estado GAME_OVER
        if (jogador.vidas <= 0) {
            estado = EstadoJogo.GAME_OVER;
            return;
        }

        // === PROGRESSÃO DE ONDAS ===
        if (listaInimigos.isEmpty()) {
            ondaAtual++;
            carregarOnda(ondaAtual);
        }
    }

    private void desenharJogo() {
        batch.begin();

        batch.draw(imgFundo, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        jogador.desenhar(batch);

        for (Inimigo ini : listaInimigos) {
            ini.desenhar(batch);
        }

        for (Tiro t : listaTiros) {
            t.desenhar(batch);
        }

        for (TiroInimigo ti : listaTirosInimigos) {
            ti.desenhar(batch);
        }

        for (ItemPowerUp p : listaPowerUps) {
            p.desenhar(batch);
        }

        // HUD (Textos)
        font.draw(batch, "Vidas: " + jogador.vidas, 20, Gdx.graphics.getHeight() - 20);
        font.draw(batch, "Pontos: " + pontuacao, 20, Gdx.graphics.getHeight() - 45);
        font.draw(batch, "Arma: " + nomeArma(jogador.tipoArmaAtual), 20, Gdx.graphics.getHeight() - 70);
        font.draw(batch, "Onda: " + ondaAtual, Gdx.graphics.getWidth() - 100, Gdx.graphics.getHeight() - 20);
        font.draw(batch, "Inimigos: " + listaInimigos.size(), Gdx.graphics.getWidth() - 130, Gdx.graphics.getHeight() - 45);

        batch.end();
    }

    // Sorteia se um power-up vai cair e, se sim, de qual tipo (1=Spread, 2=Homing, 3=Burst)
    private void tentarDropPowerUp(float x, float y) {
        if (Math.random() > CHANCE_DROP_POWERUP) return;

        int tipo = 1 + (int) (Math.random() * 3); // gera 1, 2 ou 3

        ItemPowerUp p = poolPowerUps.obtain();
        p.init(x - 7.5f, y, tipo); // -7.5 pra centralizar (largura do item é 15)
        listaPowerUps.add(p);
    }

    // Nome amigável da arma atual, só pra mostrar no HUD
    private String nomeArma(int tipo) {
        switch (tipo) {
            case 1:  return "SPREAD";
            case 2:  return "HOMING";
            case 3:  return "BURST";
            default: return "NORMAL";
        }
    }

    // =========================================================
    // ==================== ESTADO: PAUSADO ====================
    // =========================================================

    private void atualizarPausa() {
        // Aperta P de novo (ou ESC) para voltar a jogar
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            estado = EstadoJogo.JOGANDO;
        }
    }

    private void desenharPausa() {
        batch.begin();
        font.draw(batch, "=== PAUSADO ===", Gdx.graphics.getWidth() / 2f - 65, Gdx.graphics.getHeight() / 2f + 20);
        font.draw(batch, "Pressione P para continuar", Gdx.graphics.getWidth() / 2f - 105, Gdx.graphics.getHeight() / 2f - 10);
        batch.end();
    }

    // =========================================================
    // =================== ESTADO: GAME_OVER ===================
    // =========================================================

    private void atualizarGameOver() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            voltarParaMenu();
        }
    }

    private void desenharGameOver() {
        batch.begin();
        font.draw(batch, "=== GAME OVER ===", Gdx.graphics.getWidth() / 2f - 75, Gdx.graphics.getHeight() / 2f + 40);
        font.draw(batch, "Pontuacao Final: " + pontuacao, Gdx.graphics.getWidth() / 2f - 80, Gdx.graphics.getHeight() / 2f);
        font.draw(batch, "Pressione ENTER para voltar ao menu", Gdx.graphics.getWidth() / 2f - 140, Gdx.graphics.getHeight() / 2f - 45);
        batch.end();
    }

    // =========================================================
    // ================= TRANSIÇÕES DE ESTADO ==================
    // =========================================================

    // Chamado ao sair do MENU (ENTER) para começar uma partida nova
    private void iniciarJogo() {
        resetarValores();
        estado = EstadoJogo.JOGANDO;
    }

    // Chamado ao sair do GAME_OVER (ENTER) para voltar à tela inicial
    private void voltarParaMenu() {
        resetarValores();
        estado = EstadoJogo.MENU;
    }

    // Zera pontuação, vidas, onda e devolve tudo para os pools.
    // Usado tanto ao iniciar uma partida quanto ao voltar pro menu.
    private void resetarValores() {
        for (Inimigo ini : listaInimigos) {
            if (ini instanceof InimigoEspiral) {
                poolEspiral.free((InimigoEspiral) ini);
            } else if (ini instanceof InimigoAtirador) {
                poolAtiradores.free((InimigoAtirador) ini);
            } else {
                poolInimigos.free(ini);
            }
        }
        listaInimigos.clear();

        for (Tiro t : listaTiros) {
            poolTiros.free(t);
        }
        listaTiros.clear();

        for (TiroInimigo ti : listaTirosInimigos) {
            poolTirosInimigos.free(ti);
        }
        listaTirosInimigos.clear();

        for (ItemPowerUp p : listaPowerUps) {
            poolPowerUps.free(p);
        }
        listaPowerUps.clear();

        pontuacao = 0;
        ondaAtual = 1;
        jogador.vidas = 3;
        jogador.tipoArmaAtual = 0; // volta pra arma NORMAL
        jogador.x = Gdx.graphics.getWidth() / 2f - (jogador.largura / 2f);

        carregarOnda(ondaAtual);
    }

    // === GERENCIADOR DE ONDAS ===
    private void carregarOnda(int onda) {
        int tipoOnda = (onda - 1) % 4;
        switch (tipoOnda) {
            case 0:
                criarOndaGrade();
                break;
            case 1:
                criarOndaLinha();
                break;
            case 2:
                criarOndaVInvertido();
                break;
            case 3:
                criarOndaEspiral();
                break;
        }
    }

    private void criarOndaGrade() {
        int colunas = 8, fileiras = 4;
        float espacamentoX = 50, espacamentoY = 40;
        float margemEsquerda = (Gdx.graphics.getWidth() - (colunas * espacamentoX)) / 2f;
        float topoTela = Gdx.graphics.getHeight() + 60;

        for (int f = 0; f < fileiras; f++) {
            for (int c = 0; c < colunas; c++) {
                float x = margemEsquerda + (c * espacamentoX);
                float y = topoTela + (f * espacamentoY);

                if (f == 0) {
                    InimigoAtirador atirador = poolAtiradores.obtain();
                    atirador.init(imgInimigoAtirador, x, y);
                    listaInimigos.add(atirador);
                } else {
                    Inimigo comum = poolInimigos.obtain();
                    comum.init(imgInimigo, x, y);
                    listaInimigos.add(comum);
                }
            }
        }
    }

    private void criarOndaLinha() {
        int quantidade = 10;
        float espacamento = 45;
        float margemEsquerda = (Gdx.graphics.getWidth() - (quantidade * espacamento)) / 2f;
        float y = Gdx.graphics.getHeight() + 50;

        for (int i = 0; i < quantidade; i++) {
            float x = margemEsquerda + (i * espacamento);
            if (i % 2 == 0) {
                InimigoAtirador atirador = poolAtiradores.obtain();
                atirador.init(imgInimigoAtirador, x, y);
                listaInimigos.add(atirador);
            } else {
                Inimigo comum = poolInimigos.obtain();
                comum.init(imgInimigo, x, y);
                listaInimigos.add(comum);
            }
        }
    }

    private void criarOndaVInvertido() {
        int quantidadeLado = 5;
        float centroX = Gdx.graphics.getWidth() / 2f - 16;
        float topoY = Gdx.graphics.getHeight() + 80;

        InimigoAtirador pico = poolAtiradores.obtain();
        pico.init(imgInimigoAtirador, centroX, topoY);
        listaInimigos.add(pico);

        for (int i = 1; i <= quantidadeLado; i++) {
            float offsetY = i * 40, offsetX = i * 45;

            Inimigo esq = poolInimigos.obtain();
            esq.init(imgInimigo, centroX - offsetX, topoY + offsetY);
            listaInimigos.add(esq);

            Inimigo dir = poolInimigos.obtain();
            dir.init(imgInimigo, centroX + offsetX, topoY + offsetY);
            listaInimigos.add(dir);
        }
    }

    private void criarOndaEspiral() {
        int quantidade = 10;
        float centroX = Gdx.graphics.getWidth() / 2f - 16;
        float startY = Gdx.graphics.getHeight() + 30;

        for (int i = 0; i < quantidade; i++) {
            float y = startY + (i * 25);
            InimigoEspiral espira = poolEspiral.obtain();
            espira.init(imgInimigo, centroX, y, i * 0.7f);
            listaInimigos.add(espira);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        imgFundo.dispose();
        imgNave.dispose();
        imgInimigo.dispose();
        imgInimigoAtirador.dispose();

        if (font != null) {
            font.dispose();
        }
    }
}
