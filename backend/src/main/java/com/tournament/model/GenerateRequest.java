package com.tournament.model;

import java.util.List;

public class GenerateRequest {
    private int playerCount;
    private List<String> playerNames;

    public int getPlayerCount() { return playerCount; }
    public void setPlayerCount(int playerCount) { this.playerCount = playerCount; }
    public List<String> getPlayerNames() { return playerNames; }
    public void setPlayerNames(List<String> playerNames) { this.playerNames = playerNames; }
}
