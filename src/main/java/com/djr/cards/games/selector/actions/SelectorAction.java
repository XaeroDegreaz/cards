package com.djr.cards.games.selector.actions;

import com.djr.cards.BaseAction;
import com.djr.cards.data.entities.GameSelection;
import com.djr.cards.games.selector.SelectorService;
import com.djr.cards.games.selector.model.SelectorModel;
import com.opensymphony.xwork2.ModelDriven;
import org.slf4j.Logger;
import javax.inject.Inject;

/**
 * User: djr4488
 * Date: 2/10/14
 * Time: 6:29 PM
 */
public class SelectorAction extends BaseAction implements ModelDriven<SelectorModel> {
    @Inject
    private Logger logger;
    @Inject
    private SelectorService selectorService;
    private SelectorModel selectorModel = new SelectorModel();

    @Override
    public SelectorModel getModel() {
        return selectorModel;
    }

    public String displaySelectOptions() {
        logger.info("displaySelectOptions() - tracking:{}", getSessionAttribute("tracking"));
        selectorModel.setGameTypes(selectorService.getGameList(getSessionAttribute("tracking")));
        if (selectorModel.getGameTypes() == null || selectorModel.getGameTypes().size() == 0) {
            logger.debug("displaySelectOptions() - no game types found for tracking id:{}",
                    getSessionAttribute("tracking"));
            getSession().setAttribute("msgbold", "error.load.game.choices.bold");
            getSession().setAttribute("msgtext", "error.load.game.choices.text");
            return "error";
        }
        logger.debug("displaySelectOptions() - returning {} game options for tracking id:{}",
                selectorModel.getGameTypes().size(), getSessionAttribute("tracking"));
        return "success";
    }

    public String redirectToGameLanding() {
        logger.info("redirectToGameLanding() - tracking:{}, gameType:{}", getSessionAttribute("tracking"),
                selectorModel.getSelectedGameType());
        if (selectorModel.getSelectedGameType() == null || selectorModel.getSelectedGameType().trim().equals("")) {
            getSession().setAttribute("msgbold", "error.game.selected.bold");
            getSession().setAttribute("msgtext", "error.game.selected.text");
            return "error";
        }
        String landingAction = selectorService.getSelectedLandingAction(
				getSessionAttribute("tracking"), selectorModel);
        return landingAction;
    }
}
