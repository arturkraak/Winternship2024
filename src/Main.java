import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Main {
    public static Map<String, Player> players = new HashMap<>();

    public static void main(String[] args) {
        // PROCESS DATA
        try {
            BufferedReader reader = new BufferedReader(new FileReader("./resource/player_data.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                processesData(line);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // RESULTS
        long casinoBalanceTotal = 0;
        List<String> legitimatePlayers = new ArrayList<>();
        List<String> illegalPlayers = new ArrayList<>();
        for (var player : players.entrySet()) {
            if (player.getValue().getIllegalAction() != null) {
                illegalPlayers.add(player.getKey() + " " + player.getValue().getIllegalAction());
            } else {
                // The win rate DOT is replaced with a COMMA to match the result in the sample.
                String winRate = String.format(Locale.GERMAN, "%,.2f", player.getValue().getWinRate());
                legitimatePlayers.add(player.getKey() + " " + player.getValue().getPlayerBalance() + " " + winRate);
                casinoBalanceTotal += player.getValue().getCasinoBalance();
            }
        }

        // Write results to result.txt in the same location with Main class
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("./out/production/Winternship2024/result.txt"));
            if (legitimatePlayers.isEmpty()) {
                writer.newLine(); // write empty line for this section if there are no legitimate players.
            } else {
                for (var player : legitimatePlayers) {
                    writer.write(player + "\n");
                }
            }
            writer.newLine(); // separate the sections with an empty line
            if (illegalPlayers.isEmpty()) {
                writer.newLine(); // write empty line for this section if there are no illegal players.
            } else {
                for (var player : illegalPlayers) {
                    writer.write(player + "\n");
                }
            }
            writer.newLine(); // separate the sections with an empty line
            writer.write(Long.toString(casinoBalanceTotal));
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void processesData(String line) {
        String[] lineArr = line.split(",");
        String playerID = lineArr[0];
        String operation = lineArr[1];
        int amount = Integer.parseInt(lineArr[3]);

        if (!players.containsKey(playerID)) { // Create player Object if it does not exist.
            players.put(playerID, new Player());
        }
        Player player = players.get(playerID);

        if (player.getIllegalAction() == null) {
            switch (operation) {
                case "DEPOSIT":
                    player.addPlayerBalance(amount);
                    break;
                case "BET":
                    String matchID = lineArr[2];
                    String betSide = lineArr[4];
                    bet(player, matchID, amount, betSide);
                    break;
                case "WITHDRAW":
                    withdraw(player, amount);
                    break;
            }
        }
    }

    public static void withdraw(Player player, int withdrawAmount) {
        if (withdrawAmount > player.getPlayerBalance()) {
            player.setIllegalAction("WITHDRAW null " + withdrawAmount + " null");
        } else {
            player.subPlayerBalance(withdrawAmount);
        }
    }

    public static void bet(Player player, String matchID, int betAmount, String betSide) {
        String[] matchArr;
        try (Stream<String> lines = Files.lines(Paths.get("./resource/match_data.txt"))) {
            String match = lines.filter(str -> str.startsWith(matchID)).findFirst().orElseThrow(() -> new RuntimeException("Match " + matchID + " not found."));
            matchArr = match.split(",");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        double sideA_Rate = Double.parseDouble(matchArr[1]);
        double sideB_Rate = Double.parseDouble(matchArr[2]);
        String outcome = matchArr[3];

        if (betAmount > player.getPlayerBalance()) {
            player.setIllegalAction("BET " + matchID + " " + betAmount + " " + betSide);
        } else {
            player.subPlayerBalance(betAmount);
            player.incrementBets();
            switch (outcome) {
                case "A":
                    if (betSide.equals("A")) {
                        int winAmount = (int) Math.floor(betAmount * sideA_Rate);
                        player.addPlayerBalance(betAmount + winAmount);
                        player.subCasinoBalance(winAmount);
                        player.incrementWins();
                    } else {
                        player.addCasinoBalance(betAmount);
                    }
                    break;
                case "B":
                    if (betSide.equals("B")) {
                        int winAmount = (int) Math.floor(betAmount * sideB_Rate);
                        player.addPlayerBalance(betAmount + winAmount);
                        player.subCasinoBalance(winAmount);
                        player.incrementWins();
                    } else {
                        player.addCasinoBalance(betAmount);
                    }
                    break;
                case "DRAW":
                    player.addPlayerBalance(betAmount);
                    break;
            }
        }
    }
}
