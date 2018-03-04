package org.pl.android.drively.data.model.eventbus;

public class UserCompanyChanged {

    private String userCompany;

    public UserCompanyChanged(String userCompany) {
        this.userCompany = userCompany;
    }

    public String getUserCompany() {
        return userCompany;
    }

    public void setUserCompany(String userCompany) {
        this.userCompany = userCompany;
    }
}
