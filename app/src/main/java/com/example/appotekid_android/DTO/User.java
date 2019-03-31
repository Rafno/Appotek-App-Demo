package com.example.appotekid_android.DTO;


import java.io.Serializable;

/**
 * The class for the Users itself.
 * Users come from API and are created as a Serializable object
 * Serializable objects can be broken down to bit code and sent between activities.
 * This is crucial for us to check which user is logged in.
 */

public class User implements Serializable {
        private Long id;
    private String name;
    private String username;
    private boolean confirmed;
    private String role;
    // Notice the empty constructor, because we need to be able to create an empty User to add
    // to our model so we can use it with our form

    public User(){}
    public User(Long id, String name,String username, String role)
    {
        this.id = id;
        this.name = name;
        this.username = username;
        this.role = role;

    }
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }



    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    // Enabled and confirmed are about email confirmations and notification enables.
    // Possibly deprecated for app.
    public String isEnabled()
    {
        return role;
    }


    public boolean getConfirmed(){
        return this.confirmed;
    }

    public void setConfirmed(boolean confirmed){
        this.confirmed = confirmed;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", username='" + username + '\''+
                '}';
    }
}