package com.tournament.controller;

import com.tournament.model.GenerateRequest;
import com.tournament.model.Match;
import com.tournament.model.TournamentBracket;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tournament")
@CrossOrigin(origins = "http://localhost:5173")
public class TournamentController {

    @PostMapping("/generate")
    public TournamentBracket generate(@RequestBody GenerateRequest request) {
        int playerCount = request.getPlayerCount();
        List<String> names = request.getPlayerNames();

        // 補齊名稱
        List<String> players = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            if (names != null && i < names.size() && !names.get(i).trim().isEmpty()) {
                players.add(names.get(i).trim());
            } else {
                players.add("Player " + (i + 1));
            }
        }

        // 計算 bracket size（2 的冪次）
        int bracketSize = 1;
        while (bracketSize < playerCount) {
            bracketSize *= 2;
        }

        // 補 BYE
        while (players.size() < bracketSize) {
            players.add("BYE");
        }

        // 建立第一輪
        List<List<Match>> rounds = new ArrayList<>();
        List<Match> firstRound = new ArrayList<>();
        int matchId = 1;
        for (int i = 0; i < bracketSize; i += 2) {
            firstRound.add(new Match(matchId++, players.get(i), players.get(i + 1)));
        }
        rounds.add(firstRound);

        // 建立後續輪（空白）
        int matchesInRound = bracketSize / 4;
        while (matchesInRound >= 1) {
            List<Match> round = new ArrayList<>();
            for (int i = 0; i < matchesInRound; i++) {
                round.add(new Match(matchId++, "TBD", "TBD"));
            }
            rounds.add(round);
            matchesInRound /= 2;
        }

        return new TournamentBracket(playerCount, bracketSize, rounds);
    }
}
