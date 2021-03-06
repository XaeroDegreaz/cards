package com.djr.cards.auth.actions;

import com.djr.cards.auth.service.AuthService;
import com.djr.cards.auth.BaseAuthAction;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.slf4j.Logger;

import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.util.Calendar;

/**
 * User: djr4488
 * Date: 1/22/14
 * Time: 9:45 PM
 */
public class ChangePasswordAction extends BaseAuthAction {
    @Inject
    private Logger logger;

    public void validate() {
        logger.debug("validate() - authModel:{}", getModel());
        if (getModel().getUserName() == null || getModel().getUserName().trim().length() == 0) {
            logger.debug("validate() - email was missing");
            addFieldError("userName", getText("reset.missing.username"));
        }
        if (getModel().getRandomString() == null || getModel().getRandomString().trim().length() == 0) {
            logger.debug("validate() - confirmation code missing");
            addFieldError("alias", getText("reset.missing.confirmation.code"));
        }
        if(getModel().getPassword() == null || getModel().getConfirmPassword() == null ||
                getModel().getPassword().trim().length() == 0 ||
                getModel().getConfirmPassword().trim().length() == 0) {
            logger.debug("validate() - password or confirm password was missing");
            addFieldError("confirmPassword", getText("reset.missing.passwords"));
        } else if (!getModel().getPassword().equals(getModel().getConfirmPassword())) {
            logger.debug("validate() - passwords not equal");
            addFieldError("confirmPassword", getText("reset.no.match.passwords"));
        }
    }

	@SkipValidation
    public String changePassword() {
        logger.info("changePassword()");
        return "success";
    }

    public String changePasswordExecute() {
        logger.info("changePasswordExecute() - authModel:{}", getModel());
        auditService.writeAudit(auditService.getAuditLog(getSessionAttribute("tracking"),
                "ChangePasswordAction.changePasswordExecute()", getIp(), Calendar.getInstance()));
        getModel().setPassword(hashingUtil.generateHmacHash(getModel().getPassword()));
        getModel().setConfirmPassword(hashingUtil.generateHmacHash(getModel().getConfirmPassword()));
        AuthService.ChangePasswordResult result = authService.changePassword(getModel(),
                getSessionAttribute("tracking"));
        if (result == AuthService.ChangePasswordResult.SUCCESS  ||
                result == AuthService.ChangePasswordResult.NOT_FOUND) {
            return "success";
        }
        addActionError(getText("reset.password.execute.error"));
        return "error";
    }
}
