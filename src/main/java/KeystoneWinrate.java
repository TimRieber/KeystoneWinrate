import com.merakianalytics.orianna.Orianna;
import com.merakianalytics.orianna.types.common.Queue;
import com.merakianalytics.orianna.types.common.Region;
import com.merakianalytics.orianna.types.core.match.Match;
import com.merakianalytics.orianna.types.core.match.MatchHistory;
import com.merakianalytics.orianna.types.core.match.Participant;
import com.merakianalytics.orianna.types.core.staticdata.Champion;
import com.merakianalytics.orianna.types.core.summoner.Summoner;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class KeystoneWinrate {

    static final String[] keystones = {"Press the Attack", "Lethal Tempo", "Fleet Footwork", "Conqueror", "Electrocute",
            "Predator", "Dark Harvest", "Hail of Blades", "Summon Aery", "Arcane Comet", "Phase Rush",
            "Grasp of the Undying", "Aftershock", "Guardian", "Glacial Augment", "Unsealed Spellbook",
            "Prototype: Omnistone"};
    static DateTime fromDate = DateTime.parse("2020-01-10T00:00:00.000");

    public static void main(String[] args) {

        //*****************EDIT HERE*******************
        Region region = Region.EUROPE_WEST;
        Orianna.setDefaultRegion(region);

        String summonerName = "5Weapon Timboron";

        //games from this date to present are processed, default is start date of season 10
        fromDate = DateTime.parse("2020-01-10T00:00:00.000");

        //add champions if you want the data of certain champions. or don't add champions to look at all
        List<Champion> champions = new ArrayList<>();
        champions.add(Champion.named("Aphelios").get());

        List<Queue> queues = new ArrayList<>();
        queues.add(Queue.RANKED_SOLO);
        queues.add(Queue.NORMAL);
        //*********************************************

        getRuneWinRate(summonerName, region, champions, queues);
    }

    /**
     * Write out the win rate of all keystones in the given environment
     * @param summonerName name of the summoner
     * @param region region of the summoner
     * @param champions desired queues
     * @param queues desired champions (or none)
     */
    private static void getRuneWinRate(String summonerName, Region region, List<Champion> champions, List<Queue> queues) {
        final Summoner summoner = Summoner.named(summonerName).withRegion(region).get();
        MatchHistory mh = MatchHistory.forSummoner(summoner).withQueues(queues).withChampions(champions).get();

        HashMap<String, Integer> keystoneWins = new java.util.HashMap<>();
        HashMap<String, Integer> keystoneLosses = new java.util.HashMap<>();

        for(String s: KeystoneWinrate.keystones) {
            keystoneWins.put(s, 0);
            keystoneLosses.put(s, 0);
        }
        for (Match m: mh) {

            //time out to account for rate limit of API (as a not-partner you can call 100 times per 2 minutes)
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //console log to keep track of where you are at
            System.out.println(m.getCreationTime());
            if (m.getCreationTime().compareTo(KeystoneWinrate.fromDate) <= 0) break;

            Participant p;
            p = m.getParticipants().find(part -> {
                assert part != null;
                return part.getSummoner().getName().equals(summonerName);
            });

            String runeName = p.getRuneStats().get(0).getRune().getName();

            if(p.getTeam().isWinner()) {
                keystoneWins.put(runeName, keystoneWins.get(runeName) + 1);
            } else {
                keystoneLosses.put(runeName, keystoneLosses.get(runeName) + 1);
            }
        }

        for (String s: KeystoneWinrate.keystones) {
            float wins = keystoneWins.get(s);
            float losses = keystoneLosses.get(s);
            if (wins + losses == 0) {
                System.out.println(s + " was never used.   ");
            } else {
                float winrate = wins / (wins + losses) * 100;
                if(wins+losses == 1) {
                    System.out.println(s + ": " + winrate + "% with a total of " + (int) (wins + losses) + " game.   ");
                } else {
                    System.out.println(s + ": " + winrate + "% with a total of " + (int) (wins + losses) + " games.   ");
                }
            }
        }
    }
}
