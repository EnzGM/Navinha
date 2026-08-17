package com.meujogo.navinha;

/**
 * Representa em qual "tela" ou estado o jogo está no momento.
 * Cada estado decide o que é desenhado e quais teclas fazem efeito.
 */
public enum EstadoJogo {
    MENU,       // Tela inicial, esperando o jogador apertar ENTER
    JOGANDO,    // Jogo rodando normalmente
    PAUSADO,    // Jogo congelado, esperando o jogador despausar
    GAME_OVER   // Vidas acabaram, esperando ENTER para reiniciar
}
