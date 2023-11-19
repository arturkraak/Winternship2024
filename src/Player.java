import java.math.BigDecimal;
import java.math.RoundingMode;

public class Player {

    private long casinoBalance = 0; // Casino balance relative to the player.  This will be totalled if the player is legitimate.
    private long playerBalance = 0;
    private int bets = 0;
    private int wins = 0;
    private String illegalAction;

    public long getCasinoBalance() {
        return casinoBalance;
    }

    public long getPlayerBalance() {
        return playerBalance;
    }

    public void addPlayerBalance(int amount) {
        this.playerBalance += amount;
    }

    public void subPlayerBalance(int amount) {
        this.playerBalance -= amount;
    }

    public String getIllegalAction() {
        return illegalAction;
    }

    public void setIllegalAction(String illegalAction) {
        this.illegalAction = illegalAction;
    }

    public void incrementBets() {
        this.bets++;
    }

    public void incrementWins() {
        this.wins++;
    }

    public void addCasinoBalance(int amount) {
        this.casinoBalance += amount;
    }

    public void subCasinoBalance(int amount) {
        this.casinoBalance -= amount;
    }

    public BigDecimal getWinRate() {
        if (this.bets > 0) {
            return new BigDecimal((double) this.wins / this.bets).setScale(2, RoundingMode.DOWN);
        } else {
            return new BigDecimal("0.00");
        }
    }
}
