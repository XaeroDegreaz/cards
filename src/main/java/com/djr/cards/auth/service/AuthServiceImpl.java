package com.djr.cards.auth.service;

import com.djr.cards.audit.AuditService;
import com.djr.cards.auth.AuthModel;
import com.djr.cards.auth.AuthService;
import com.djr.cards.auth.login.LoginResult;
import com.djr.cards.data.dao.UserDAO;
import com.djr.cards.email.EmailService;
import com.djr.cards.data.entities.User;
import org.slf4j.Logger;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.mail.Session;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;
import java.util.Calendar;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: djr4488
 * Date: 1/4/14
 * Time: 5:07 PM
 */
public class AuthServiceImpl implements AuthService {
    @Inject
    private Logger logger;
    @Inject
    private AuditService auditService;
    @Inject
    private EmailService emailService;
    @Inject
    private Session session;
    @Inject
    private UserDAO userDao;

    public AuthServiceImpl() { }

    @Override
    public CreateResult createUser(AuthModel authModel, String trackingId) {
        logger.debug("createUser - authModel:{}, trackingId:{}", authModel, trackingId);
        FindUserResult findUserResult = userDao.findOrCreateUser(authModel, trackingId);
        if (findUserResult == null) {
            return CreateResult.OTHER_FAILURE;
        } else if (!findUserResult.created) {
            return CreateResult.USERNAME_TAKEN;
        }
        return CreateResult.CREATED;
    }

    @Override
    public LoginResult login(AuthModel authModel, String trackingId) {
        logger.debug("login() - authModel:{}, trackingId:{}", authModel, trackingId);
        User user = userDao.findUser(authModel, trackingId);
        LoginResult loginResult = new LoginResult();
        if (user == null || !user.hashedPassword.equals(authModel.getPassword())) {
            auditService.writeAudit(auditService.getAuditLog(trackingId, "AuthService.auth()",
                    authModel.toString(), Calendar.getInstance()));
            loginResult.result = LoginResult.ResultOptions.FAILED_USER_OR_PASS;
            loginResult.user = null;
            return loginResult;
        }
        loginResult.result = LoginResult.ResultOptions.SUCCESS;
        loginResult.user = user;
        return loginResult;
    }

    @Override
    public ForgotPasswordResult forgotPassword(AuthModel authModel, String trackingId) {
        logger.debug("forgotPassword() - authModel:{}, trackingId:{}", authModel, trackingId);
        auditService.writeAudit(auditService.getAuditLog(trackingId, "forgotPassword()", authModel.toString(),
                Calendar.getInstance()));
        authModel.setUserName(authModel.getUserName());
        User user = userDao.findUser(authModel, trackingId);
        if (user == null) {
            return ForgotPasswordResult.NOT_FOUND;
        } else {
            Random r = new Random();
            Integer code = r.nextInt(50000);
            String emailBody = "Here to help, lets make it so you can change your password!\nJust use the code below " +
                    "to change your password.  If you didn't initiate this, you might want to change go ahead and " +
                    "change it anyway, just to go the cards website(link not sent).  \n\nCode -> " + code + ".";
            String subject = "Cards - Forgot Password Service";
            if (emailService.sendEmail(user.userName, user.alias, subject, emailBody, session)) {
                user.changePasswordProof = code.toString();
                userDao.updateUser(user, trackingId);
                return ForgotPasswordResult.SUCCESS;
            }
        }
        return ForgotPasswordResult.OTHER_FAILURE;
    }

    @Override
    public ChangePasswordResult changePassword(AuthModel authModel, String trackingId) {
        logger.debug("changePassword() - authModel:{}, trackingId:{}");
        authModel.setUserName(authModel.getUserName());
        User user = userDao.findUser(authModel, trackingId);
        if (user == null) {
            auditService.writeAudit(auditService.getAuditLog(trackingId, "changePassword()", authModel.toString(),
                    Calendar.getInstance()));
            return ChangePasswordResult.OTHER_FAILURE;
        } else {
            if (user.changePasswordProof == null) {
                return ChangePasswordResult.OTHER_FAILURE;
            }
            if (user.changePasswordProof.equals(authModel.getRandomString()) &&
                    authModel.getPassword().equals(authModel.getConfirmPassword())) {
                user.changePasswordProof = null;
                user.hashedPassword = authModel.getPassword();
                userDao.updateUser(user, trackingId);
            }
        }
        return ChangePasswordResult.SUCCESS;
    }
}