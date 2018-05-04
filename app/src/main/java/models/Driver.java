package models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Driver extends User{

  public String key;

  public Driver() {
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }
}
