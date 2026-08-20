package com.meujogo.navinha;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;
import java.util.ArrayList;
import java.util.Iterator;

import com.meujogo.navinha.entidades.*;

public class MeuJogo extends ApplicationAdapter {

    private SpriteBatch batch;
    private BitmapFont font;

    // Texturas Principais
    private Texture imgNave;
    private Texture imgInimigo;
    private Texture imgInimigoAtirador;

    // Texturas Procedurais
    private Texture imgTiro;
    private Texture imgTiroInimigo;
    private Texture imgTiroHoming;
    private Texture imgFundoHud;
    private Texture imgLinhaHud;

    // Entidade Principal
    private Nave jogador;

    // Listas do Jogo
    private ArrayList<Tiro> listaTiros;
    private ArrayList<TiroInimigo> listaTirosInimigos;
    private ArrayList<TiroHoming> listaTirosHoming;
    private ArrayList<Inimigo> listaInimigos;
    private ArrayList<ItemPowerUp> listaPowerUps;

    // Pools de Otimização
    private Pool<Tiro> poolTiros;
    private Pool<TiroInimigo> poolTirosInimigos;
    private Pool<TiroHoming> poolTirosHoming;
    private Pool<Inimigo> poolInimigos;
    private Pool<InimigoAtirador> poolAtiradores;
    private Pool<InimigoEspiral> poolEspiral;
    private Pool<InimigoTank> poolTanks;
    private Pool<BossAtirador> poolBossAtirador;
    private Pool<BossInvocador> poolBossInvocador;
    private Pool<Minion> poolMinions;
    private Pool<ItemPowerUp> poolPowerUps;

    // Variáveis de Controle
    private int pontuacao = 0;
    private int ondaAtual = 1;
    private float tempoTiro = 0;
    private float intervaloTiro = 0.3f;
    private int tirosRajada = 0;

    // Controle de movimento estilo Space Invaders
    private float direcaoInimigosX = 1f; // 1 = Direita, -1 = Esquerda

    public enum EstadoJogo { JOGANDO, GAME_OVER }
    private EstadoJogo estado;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.2f);
        estado = EstadoJogo.JOGANDO;

        imgNave = new Texture("nave.png");
        imgInimigo = new Texture("inimigo.png");
        // Sprite próprio para o InimigoAtirador e para o BossAtirador (fase 5).
        // Lembre de colocar o arquivo "inimigo_atirador.png" dentro da pasta assets/ do projeto.
        imgInimigoAtirador = new Texture("inimigo_atirador.png");

        imgTiro = criarTexturaColorida(Color.CYAN, 1, 1);
        imgTiroInimigo = criarTexturaColorida(Color.ORANGE, 1, 1);
        imgTiroHoming = criarTexturaColorida(Color.MAGENTA, 1, 1);

        imgFundoHud = criarTexturaColorida(new Color(0.05f, 0.05f, 0.12f, 0.85f), 1, 1);
        imgLinhaHud = criarTexturaColorida(new Color(0.0f, 0.8f, 1.0f, 1.0f), 1, 1);

        jogador = new Nave(imgNave, Gdx.graphics.getWidth() / 2f - imgNave.getWidth() / 4f, 50);

        listaTiros = new ArrayList<>();
        listaTirosInimigos = new ArrayList<>();
        listaTirosHoming = new ArrayList<>();
        listaInimigos = new ArrayList<>();
        listaPowerUps = new ArrayList<>();

        poolTiros = new Pool<Tiro>() { @Override protected Tiro newObject() { return new Tiro(); } };
        poolTirosInimigos = new Pool<TiroInimigo>() { @Override protected TiroInimigo newObject() { return new TiroInimigo(); } };
        poolTirosHoming = new Pool<TiroHoming>() { @Override protected TiroHoming newObject() { return new TiroHoming(); } };
        poolPowerUps = new Pool<ItemPowerUp>() { @Override protected ItemPowerUp newObject() { return new ItemPowerUp(); } };
        poolInimigos = new Pool<Inimigo>() { @Override protected Inimigo newObject() { return new Inimigo(); } };
        poolAtiradores = new Pool<InimigoAtirador>() { @Override protected InimigoAtirador newObject() { return new InimigoAtirador(); } };
        poolEspiral = new Pool<InimigoEspiral>() { @Override protected InimigoEspiral newObject() { return new InimigoEspiral(); } };
        poolTanks = new Pool<InimigoTank>() { @Override protected InimigoTank newObject() { return new InimigoTank(); } };
        poolBossAtirador = new Pool<BossAtirador>() { @Override protected BossAtirador newObject() { return new BossAtirador(); } };
        poolBossInvocador = new Pool<BossInvocador>() { @Override protected BossInvocador newObject() { return new BossInvocador(); } };
        poolMinions = new Pool<Minion>() { @Override protected Minion newObject() { return new Minion(); } };

        carregarOnda(ondaAtual);
    }

    private Texture criarTexturaColorida(Color cor, int width, int height) {
        Pixmap pix = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pix.setColor(cor);
        pix.fill();
        Texture tex = new Texture(pix);
        pix.dispose();
        return tex;
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float delta = Gdx.graphics.getDeltaTime();

        if (estado == EstadoJogo.JOGANDO) {
            atualizarJogo(delta);
        } else if (estado == EstadoJogo.GAME_OVER) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                resetarValores();
            }
        }

        batch.begin();
        desenharJogo(batch);
        batch.end();
    }

    private void atualizarJogo(float delta) {
        jogador.atualizar(delta);

        // === SISTEMA DE TIROS ===
        tempoTiro += delta;
        if (tempoTiro >= intervaloTiro && jogador.ativo) {
            tempoTiro = 0;
            float centroX = jogador.x + (jogador.largura / 2f) - 3;

            if (jogador.tipoArmaAtual == 0) { // Padrão
                intervaloTiro = 0.3f;
                tirosRajada = 0;
                Tiro t = poolTiros.obtain();
                t.init(imgTiro, centroX, jogador.y + jogador.altura, 0, 600);
                listaTiros.add(t);

            } else if (jogador.tipoArmaAtual == 1) { // Spread (3 Tiros em leque)
                intervaloTiro = 0.35f;
                tirosRajada = 0;
                float[] angulosX = {-100f, 0f, 100f};
                for (float vx : angulosX) {
                    Tiro t = poolTiros.obtain();
                    t.init(imgTiro, centroX, jogador.y + jogador.altura, vx, 550);
                    listaTiros.add(t);
                }

            } else if (jogador.tipoArmaAtual == 2) { // Homing (1 tiro com cadência menor)
                intervaloTiro = 0.5f; // Cadência reduzida (intervalo maior)
                tirosRajada = 0;
                TiroHoming th = poolTirosHoming.obtain();
                th.init(imgTiroHoming, centroX, jogador.y + jogador.altura);
                listaTirosHoming.add(th);

            } else if (jogador.tipoArmaAtual == 3) { // Burst (Rajada "PAPAPA - pausa")
                Tiro t = poolTiros.obtain();
                t.init(imgTiro, centroX, jogador.y + jogador.altura, 0, 800);
                listaTiros.add(t);

                tirosRajada++;
                if (tirosRajada >= 3) {
                    intervaloTiro = 0.5f;
                    tirosRajada = 0;
                } else {
                    intervaloTiro = 0.08f;
                }
            }
        }

        // === ATUALIZAR TIROS DO JOGADOR ===
        Iterator<Tiro> itTiro = listaTiros.iterator();
        while (itTiro.hasNext()) {
            Tiro t = itTiro.next();
            t.atualizar(delta);
            if (!t.ativo) {
                itTiro.remove();
                poolTiros.free(t);
            }
        }

        // === ATUALIZAR TIROS HOMING ===
        Iterator<TiroHoming> itHoming = listaTirosHoming.iterator();
        while (itHoming.hasNext()) {
            TiroHoming th = itHoming.next();
            th.atualizar(delta, listaInimigos);

            for (Inimigo ini : listaInimigos) {
                if (th.ativo && ini.ativo && th.getBounds().overlaps(ini.getBounds())) {
                    th.ativo = false;

                    if (ini instanceof BossInvocador) {
                        BossInvocador bossInv = (BossInvocador) ini;
                        bossInv.tomarDano(1);
                        if (!bossInv.ativo) { pontuacao += 2000; tentarDropPowerUp(ini.x, ini.y); }
                    } else if (ini instanceof BossAtirador) {
                        BossAtirador boss = (BossAtirador) ini;
                        boss.tomarDano(1);
                        if (!boss.ativo) { pontuacao += 1000; tentarDropPowerUp(ini.x, ini.y); }
                    } else if (ini instanceof InimigoTank) {
                        InimigoTank tank = (InimigoTank) ini;
                        tank.tomarDano(1);
                        if (!tank.ativo) { pontuacao += 300; tentarDropPowerUp(ini.x, ini.y); }
                    } else {
                        ini.ativo = false;
                        pontuacao += 100;
                        tentarDropPowerUp(ini.x, ini.y);
                    }
                }
            }

            if (!th.ativo) {
                itHoming.remove();
                poolTirosHoming.free(th);
            }
        }

        // === ATUALIZAR TIROS INIMIGOS ===
        Iterator<TiroInimigo> itTiroInimigo = listaTirosInimigos.iterator();
        while (itTiroInimigo.hasNext()) {
            TiroInimigo ti = itTiroInimigo.next();
            ti.atualizar(delta);

            if (ti.ativo && ti.getBounds().overlaps(jogador.getBounds())) {
                if (!jogador.tomarDano()) {
                    ti.ativo = false;
                }
                if (!jogador.ativo) estado = EstadoJogo.GAME_OVER;
            }

            if (!ti.ativo) {
                itTiroInimigo.remove();
                poolTirosInimigos.free(ti);
            }
        }

        // === ATUALIZAR POWER-UPS ===
        Iterator<ItemPowerUp> itPowerUp = listaPowerUps.iterator();
        while (itPowerUp.hasNext()) {
            ItemPowerUp p = itPowerUp.next();
            p.atualizar(delta);

            if (p.ativo && p.getBounds().overlaps(jogador.getBounds())) {
                jogador.tipoArmaAtual = p.tipoPoder;
                tirosRajada = 0;
                pontuacao += 50;
                p.ativo = false;
            }

            if (!p.ativo) {
                itPowerUp.remove();
                poolPowerUps.free(p);
            }
        }

        // === DIFICULDADE DINÂMICA ===
        // Quanto menos inimigos restam na tela, mais rápido eles ficam.
        // Começa a acelerar a partir de 8 inimigos vivos e vai crescendo até
        // um teto de 2.5x de velocidade quando sobra só 1.
        float multiplicadorDificuldade = 1.0f;
        int inimigosVivos = listaInimigos.size();
        if (inimigosVivos > 0) {
            int limiteBase = 8;
            int faltando = Math.max(0, limiteBase - inimigosVivos);
            multiplicadorDificuldade = 1.0f + (faltando * 0.2f);
            multiplicadorDificuldade = Math.min(multiplicadorDificuldade, 2.5f);
        }

        // === CONTROLE DE BORDAS ESTILO SPACE INVADERS ===
        boolean inverterDirecao = false;
        for (Inimigo ini : listaInimigos) {
            // Ignora InimigoEspiral e Minion, que têm movimento próprio (mergulho)
            if (ini.ativo && !(ini instanceof BossAtirador) && !(ini instanceof BossInvocador) && !(ini instanceof InimigoEspiral) && !(ini instanceof Minion)) {
                if ((direcaoInimigosX > 0 && ini.x + ini.largura >= Gdx.graphics.getWidth() - 10) ||
                    (direcaoInimigosX < 0 && ini.x <= 10)) {
                    inverterDirecao = true;
                    break;
                }
            }
        }

        if (inverterDirecao) {
            direcaoInimigosX *= -1f;
            for (Inimigo ini : listaInimigos) {
                // Ignora InimigoEspiral e Minion, que têm movimento próprio (mergulho)
                if (!(ini instanceof BossAtirador) && !(ini instanceof BossInvocador) && !(ini instanceof InimigoEspiral) && !(ini instanceof Minion)) {
                    ini.dirX = direcaoInimigosX;
                    ini.y -= 25f; // Desce o degrau
                    ini.x += direcaoInimigosX * 6f; // Afasta da borda para não travar o loop
                }
            }
        }

        // === ATUALIZAR INIMIGOS ===
        ArrayList<Inimigo> novosInimigos = new ArrayList<>();

        Iterator<Inimigo> itInimigo = listaInimigos.iterator();
        while (itInimigo.hasNext()) {
            Inimigo ini = itInimigo.next();

            float velocidadeOriginal = ini.velocidade;
            ini.velocidade = velocidadeOriginal * multiplicadorDificuldade;

            if (ini instanceof BossInvocador) {
                ((BossInvocador) ini).atualizarBoss(delta, novosInimigos, poolMinions, imgInimigo);
            } else {
                ini.atualizar(delta, listaTirosInimigos, poolTirosInimigos);
            }

            ini.velocidade = velocidadeOriginal;

            // Colisão Tiro Normal -> Inimigo
            for (Tiro t : listaTiros) {
                if (t.ativo && ini.ativo && t.getBounds().overlaps(ini.getBounds())) {
                    t.ativo = false;

                    if (ini instanceof BossInvocador) {
                        BossInvocador bossInv = (BossInvocador) ini;
                        bossInv.tomarDano(1);
                        if (!bossInv.ativo) {
                            pontuacao += 2000;
                            tentarDropPowerUp(ini.x, ini.y);
                        }
                    } else if (ini instanceof BossAtirador) {
                        BossAtirador boss = (BossAtirador) ini;
                        boss.tomarDano(1);
                        if (!boss.ativo) {
                            pontuacao += 1000;
                            tentarDropPowerUp(ini.x, ini.y);
                        }
                    } else if (ini instanceof InimigoTank) {
                        InimigoTank tank = (InimigoTank) ini;
                        tank.tomarDano(1);
                        if (!tank.ativo) {
                            pontuacao += 300;
                            tentarDropPowerUp(ini.x, ini.y);
                        }
                    } else {
                        ini.ativo = false;
                        pontuacao += 100;
                        tentarDropPowerUp(ini.x, ini.y);
                    }
                }
            }

            // Colisão Inimigo -> Nave (Kamikaze)
            if (ini.ativo && ini.getBounds().overlaps(jogador.getBounds())) {
                if (jogador.tomarDano()) {
                    estado = EstadoJogo.GAME_OVER;
                }
            }

            if (!ini.ativo) {
                itInimigo.remove();
                if (ini instanceof BossInvocador) {
                    poolBossInvocador.free((BossInvocador) ini);
                } else if (ini instanceof Minion) {
                    poolMinions.free((Minion) ini);
                } else if (ini instanceof BossAtirador) {
                    poolBossAtirador.free((BossAtirador) ini);
                } else if (ini instanceof InimigoTank) {
                    poolTanks.free((InimigoTank) ini);
                } else if (ini instanceof InimigoEspiral) {
                    poolEspiral.free((InimigoEspiral) ini);
                } else if (ini instanceof InimigoAtirador) {
                    poolAtiradores.free((InimigoAtirador) ini);
                } else {
                    poolInimigos.free(ini);
                }
            }
        }

        listaInimigos.addAll(novosInimigos);

        if (listaInimigos.isEmpty()) {
            ondaAtual++;
            carregarOnda(ondaAtual);
            pontuacao += 500;
        }
    }

    private void tentarDropPowerUp(float x, float y) {
        if (MathUtils.random(1, 100) <= 10) {
            ItemPowerUp p = poolPowerUps.obtain();
            int tipoAleatorio = MathUtils.random(1, 3);
            p.init(x, y, tipoAleatorio);
            listaPowerUps.add(p);
        }
    }

    private void carregarOnda(int onda) {
        direcaoInimigosX = 1f; // Reseta a direção para a direita a cada nova onda

        if (onda % 10 == 0) {
            criarOndaBossInvocador();
        } else if (onda % 5 == 0) {
            criarOndaBossAtirador();
        } else {
            int tipoOnda = (onda - 1) % 4;
            switch (tipoOnda) {
                case 0: criarOndaGrade(); break;
                case 1: criarOndaLinha(); break;
                case 2: criarOndaVInvertido(); break;
                case 3: criarOndaEspiral(); break;
            }
        }
    }

    private void criarOndaGrade() {
        int linhas = 3;
        int colunas = 5;
        float espacamentoX = 60, espacamentoY = 50;
        float inicioX = (Gdx.graphics.getWidth() - (colunas - 1) * espacamentoX) / 2f;
        float inicioY = Gdx.graphics.getHeight() - 100;

        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                if (i == linhas - 1) {
                    InimigoAtirador atirador = poolAtiradores.obtain();
                    atirador.init(imgInimigoAtirador, inicioX + j * espacamentoX, inicioY + i * espacamentoY);
                    listaInimigos.add(atirador);
                } else {
                    Inimigo ini = poolInimigos.obtain();
                    ini.init(imgInimigo, inicioX + j * espacamentoX, inicioY + i * espacamentoY);
                    listaInimigos.add(ini);
                }
            }
        }
    }

    private void criarOndaLinha() {
        int quantidade = 7;
        float espacamentoX = 50;
        float inicioX = (Gdx.graphics.getWidth() - (quantidade - 1) * espacamentoX) / 2f;
        float topoY = Gdx.graphics.getHeight() - 80;

        for (int i = 0; i < quantidade; i++) {
            Inimigo ini = poolInimigos.obtain();
            ini.init(imgInimigo, inicioX + i * espacamentoX, topoY);
            listaInimigos.add(ini);
        }
    }

    private void criarOndaVInvertido() {
        int quantidadeLado = 5;
        float centroX = Gdx.graphics.getWidth() / 2f - 16;
        float topoY = Gdx.graphics.getHeight() - 20; // Alterado

        InimigoTank bossTank = poolTanks.obtain();
        bossTank.init(imgInimigo, centroX - (imgInimigo.getWidth() / 3f * 1.5f) / 2f, topoY);
        listaInimigos.add(bossTank);

        for (int i = 1; i <= quantidadeLado; i++) {
            float offsetY = i * 28; // Alterado
            float offsetX = i * 35; // Alterado

            Inimigo esq = poolInimigos.obtain();
            esq.init(imgInimigo, centroX - offsetX, topoY - offsetY);
            listaInimigos.add(esq);

            Inimigo dir = poolInimigos.obtain();
            dir.init(imgInimigo, centroX + offsetX, topoY - offsetY);
            listaInimigos.add(dir);
        }
    }

    private void criarOndaEspiral() {
        int quantidade = 8;
        float inicioX = Gdx.graphics.getWidth() / 2f - 16;
        float inicioY = Gdx.graphics.getHeight() + 50; // Alterado

        for (int i = 0; i < quantidade; i++) {
            InimigoEspiral esp = poolEspiral.obtain();
            esp.init(imgInimigo, inicioX, inicioY + (i * 60)); // Alterado (somando para empilhar pra cima)
            listaInimigos.add(esp);
        }
    }

    private void criarOndaBossAtirador() {
        BossAtirador boss = poolBossAtirador.obtain();
        float centroX = Gdx.graphics.getWidth() / 2f - 35;
        float topoY = Gdx.graphics.getHeight() - 100;
        boss.init(imgInimigoAtirador, centroX, topoY);
        listaInimigos.add(boss);
    }

    private void criarOndaBossInvocador() {
        BossInvocador boss = poolBossInvocador.obtain();
        float centroX = Gdx.graphics.getWidth() / 2f - 40;
        float topoY = Gdx.graphics.getHeight() - 120;
        boss.init(imgInimigo, centroX, topoY);
        listaInimigos.add(boss);
    }

    private void desenharJogo(SpriteBatch batch) {
        for (Tiro t : listaTiros) t.desenhar(batch);
        for (TiroHoming th : listaTirosHoming) th.desenhar(batch);
        for (TiroInimigo ti : listaTirosInimigos) ti.desenhar(batch);
        for (ItemPowerUp p : listaPowerUps) p.desenhar(batch);
        for (Inimigo ini : listaInimigos) ini.desenhar(batch);
        if (jogador.ativo) jogador.desenhar(batch);

        // HUD Superior
        float alturaHud = 50;
        float topoY = Gdx.graphics.getHeight();

        batch.draw(imgFundoHud, 0, topoY - alturaHud, Gdx.graphics.getWidth(), alturaHud);
        batch.draw(imgLinhaHud, 0, topoY - alturaHud - 2, Gdx.graphics.getWidth(), 2);

        float textoY = topoY - 18;
        font.draw(batch, "VIDAS: " + jogador.vidas, 20, textoY);
        font.draw(batch, "PONTOS: " + pontuacao, 140, textoY);
        font.draw(batch, "ARMA: " + nomeArma(jogador.tipoArmaAtual), 300, textoY);
        font.draw(batch, "ONDA: " + ondaAtual, Gdx.graphics.getWidth() - 210, textoY);
        font.draw(batch, "INIMIGOS: " + listaInimigos.size(), Gdx.graphics.getWidth() - 110, textoY);

        if (estado == EstadoJogo.GAME_OVER) {
            font.draw(batch, "GAME OVER! APERTE ENTER PARA REINICIAR",
                Gdx.graphics.getWidth() / 2f - 180, Gdx.graphics.getHeight() / 2f);
        }
    }

    private String nomeArma(int tipo) {
        switch (tipo) {
            case 1: return "SPREAD";
            case 2: return "HOMING";
            case 3: return "BURST";
            default: return "PADRAO";
        }
    }

    private void resetarValores() {
        estado = EstadoJogo.JOGANDO;
        pontuacao = 0;
        ondaAtual = 1;
        tirosRajada = 0;
        direcaoInimigosX = 1f;
        jogador.vidas = 3;
        jogador.ativo = true;
        jogador.x = Gdx.graphics.getWidth() / 2f - imgNave.getWidth() / 4f;
        jogador.y = 50;
        jogador.tipoArmaAtual = 0;
        jogador.tempoInvulneravel = Nave.DURACAO_INVULNERAVEL;

        for (Inimigo ini : listaInimigos) {
            if (ini instanceof BossInvocador) {
                poolBossInvocador.free((BossInvocador) ini);
            } else if (ini instanceof Minion) {
                poolMinions.free((Minion) ini);
            } else if (ini instanceof BossAtirador) {
                poolBossAtirador.free((BossAtirador) ini);
            } else if (ini instanceof InimigoTank) {
                poolTanks.free((InimigoTank) ini);
            } else if (ini instanceof InimigoEspiral) {
                poolEspiral.free((InimigoEspiral) ini);
            } else if (ini instanceof InimigoAtirador) {
                poolAtiradores.free((InimigoAtirador) ini);
            } else {
                poolInimigos.free(ini);
            }
        }
        listaInimigos.clear();

        for (Tiro t : listaTiros) poolTiros.free(t);
        listaTiros.clear();

        for (TiroHoming th : listaTirosHoming) poolTirosHoming.free(th);
        listaTirosHoming.clear();

        for (TiroInimigo ti : listaTirosInimigos) poolTirosInimigos.free(ti);
        listaTirosInimigos.clear();

        for (ItemPowerUp p : listaPowerUps) poolPowerUps.free(p);
        listaPowerUps.clear();

        carregarOnda(ondaAtual);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        imgNave.dispose();
        imgInimigo.dispose();
        imgInimigoAtirador.dispose();
        imgTiro.dispose();
        imgTiroInimigo.dispose();
        if (imgTiroHoming != null) imgTiroHoming.dispose();
        if (imgFundoHud != null) imgFundoHud.dispose();
        if (imgLinhaHud != null) imgLinhaHud.dispose();
    }
}
