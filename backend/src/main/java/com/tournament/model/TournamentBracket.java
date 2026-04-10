package com.tournament.model;

import java.util.List;

public class TournamentBracket {
    private int totalPlayers;
    private int bracketSize;
    private List<List<Match>> rounds;

    public TournamentBracket(int totalPlayers, int bracketSize, List<List<Match>> rounds) {
        this.totalPlayers = totalPlayers;
        this.bracketSize = bracketSize;
        this.rounds = rounds;
    }

    public int getTotalPlayers() { return totalPlayers; }
    public int getBracketSize() { return bracketSize; }
    public List<List<Match>> getRounds() { return rounds; }
}
