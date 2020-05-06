package cn.vove7.common.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.vove7.common.appbus.AppBus;
import cn.vove7.quantumclock.QuantumClock;

/**
 * # UserInfo
 *
 * @author 17719247306
 * 2018/9/11
 */
public class UserInfo implements Serializable {
    @Expose(serialize = false)
    private static final long serialVersionUID = -5758449218926660550L;
    private Long userId;
    private String userName;
    private String email;
    private String userPass;
    @Expose(serialize = false)
    private Date regTime;
    private String userToken;
    @Expose(serialize = false)
    private Date vipEndDate;

    @Expose(serialize = false)
    private boolean isVip = false;

    public void success() {
        INSTANCE = this;
        if (!isLogin()) {
            isLogin = true;
            AppBus.post(AppBus.EVENT_USER_INIT);
        }
    }

    public synchronized static void logout() {
        INSTANCE = null;
        boolean l = isLogin;
        isLogin = false;
        if (l) {
            AppBus.post(AppBus.EVENT_LOGOUT);
        }
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }

    public void setRegTime(Date regTime) {
        this.regTime = regTime;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public void setVip(boolean vip) {
        isVip = vip;
    }

    public static Long getUserId() {
        return INSTANCE == null ? -1L : INSTANCE.userId;
    }


    @Expose(serialize = false)
    public static UserInfo INSTANCE;
    @Expose(serialize = false)
    private static boolean isLogin = false;


    public static String getUserName() {
        return INSTANCE == null ? null : INSTANCE.userName;
    }

    public static String getEmail() {
        return INSTANCE == null ? null : INSTANCE.email;
    }

    public String getUserPass() {
        return userPass;
    }

    public static Date getRegTime() {
        return INSTANCE == null ? null : INSTANCE.regTime;
    }

    public static String getUserToken() {
        return INSTANCE == null ? null : INSTANCE.userToken;
    }

    public static boolean isVip() {
        return INSTANCE != null && (INSTANCE.vipEndDate != null &&
                INSTANCE.vipEndDate.getTime() > QuantumClock.INSTANCE.getCurrentTimeMillis());
    }

    public static boolean isPermanentVip() {
        try {
            return INSTANCE != null && (INSTANCE.vipEndDate != null &&
                    INSTANCE.vipEndDate.getTime() > new SimpleDateFormat("yyyy-MM-dd")
                            .parse("2100-01-01").getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static UserInfo getINSTANCE() {
        return INSTANCE;
    }

    public static boolean isLogin() {
        return isLogin && INSTANCE != null;
    }

    public static Date getVipEndDate() {
        return INSTANCE == null ? null : INSTANCE.vipEndDate;
    }

    public void setVipEndDate(Date vipEndDate) {
    }
}


