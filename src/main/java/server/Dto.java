package server;

import model.User;

import java.io.Serializable;
import java.util.List;

public class Dto implements Serializable {
    private User user;
    private List<User> users;

    public Dto(User user, List<User> users) {
        this.user = user;
        this.users = users;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
