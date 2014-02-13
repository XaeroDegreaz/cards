package com.djr.cards.games.golf.stats.service;

import com.djr.cards.data.entities.User;
import com.djr.cards.data.entities.UserStats;
import com.djr.cards.games.golf.stats.GolfStatsService;
import com.djr.cards.games.stats.model.GameStats;
import com.djr.cards.games.stats.model.PlayerStats;
import com.djr.cards.games.stats.UserStatsDAO;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: djr4488
 * Date: 2/12/14
 * Time: 7:50 PM
 */
public class GolfStatsServiceImpl implements GolfStatsService {
    @Inject
    private Logger logger;
    @Inject
    private UserStatsDAO userStatsDao;

    private boolean findOrCreateStats(String tracking, User user) {
        logger.debug("findOrCreateStats() - tracking:{}, user:{}");
        return userStatsDao.findStatsByUser(user, "Golf", tracking) != null;
    }

    private List<UserStats> loadUserStats(String tracking) {
        logger.debug("loadGolfStats() - tracking:{}");
        List<UserStats> userStats = userStatsDao.loadStatistics("Golf", tracking);
        return userStats;
    }

    private List<PlayerStats> topTenGolfPlayers(String tracking, List<UserStats> userStatsList) {
        logger.debug("topTenGolfPlayers() - tracking:{}, userStatsList size:{}", tracking, userStatsList.size());
        int count = 1;
        List<PlayerStats> playerStatsList = new ArrayList<PlayerStats>();
        for (UserStats userStats : userStatsList) {
            PlayerStats playerStats = new PlayerStats(userStats, count);
            playerStatsList.add(playerStats);
            count++;
            if (count > 10) {
                break;
            }
        }
        return playerStatsList;
    }

    private PlayerStats getUserStats(String tracking, User user, List<UserStats> userStatsList) {
        logger.debug("getUserStats() - tracking:{}, user:{}, userStatsListSize:{}", tracking, user,
                userStatsList.size());
        PlayerStats playerStats = null;
        int rank = 1;
        for (UserStats userStats : userStatsList) {
            if(userStats.user.emailAddress.equals(user.emailAddress)) {
                playerStats = new PlayerStats(userStats, rank);
                break;
            }
            rank++;
        }
        return playerStats;
    }

    @Override
    public GameStats loadGolfStats(String tracking, User user) {
        logger.debug("loadGolfStats() - tracking:{}, user:{}", tracking, user);
        if (findOrCreateStats(tracking, user)) {
            List<UserStats> userStats = loadUserStats(tracking);
            Collections.sort(userStats);
            List<PlayerStats> playerStatsList = topTenGolfPlayers(tracking, userStats);
            PlayerStats playerStats = getUserStats(tracking, user, userStats);
            GameStats gameStats = new GameStats(playerStatsList, playerStats);
            return gameStats;
        }
        logger.debug("loadGolfStats() - tracking:{} failed to load stats");
        return null;
    }
}
