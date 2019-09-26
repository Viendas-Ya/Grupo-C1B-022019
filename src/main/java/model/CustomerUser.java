package model;

import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerUser extends User {

    private int id;
    private MoneyAccount account = new MoneyAccount();
    List<UserScore> userScores = new ArrayList<>();

    MoneyAccount getAccount() {
        return account;
    }

    public CustomerUser(String name, String lastName, String eMail, String phone, String address) {
        super(name, lastName, eMail, phone, address);
    }

    boolean hasPendingPunctuations() {
        return userScores.stream().anyMatch(us -> !us.isFinished());
    }

    void addDefaultScore(Service service, Menu menu) {
        userScores.add(new UserScore(this.getName(), service, menu));
    }
}
