package stretch.lockout.team;

import javax.annotation.Nullable;

public class TestTeam extends LockoutTeam {
    public TestTeam(String teamName, int maxSize, @Nullable TeamManager teamManager) {
        super(teamName, maxSize, teamManager);
    }

    public TestTeam(String teamName, int maxSize) {
        super(teamName, maxSize);
    }

    @Override
    public int getScore() {
        return 0;
    }

    @Override
    public int playerCount() {
        return 1;
    }
}
