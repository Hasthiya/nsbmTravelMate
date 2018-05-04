package models;




import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Student extends User {

  public String studentID;

  public String password;

  public String email;

  public LatLang location;

  public String uuid;

  public Student() {
  }

  public Student(String studentID, String password, String email,
                 LatLang location, String uuid) {
    this.studentID = studentID;
    this.password = password;
    this.email = email;
    this.location = location;
  }

  public String getStudentID() {
    return studentID;
  }

  public void setStudentID(String studentID) {
    this.studentID = studentID;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public LatLang getLocation() {
    return location;
  }

  public void setLocation(LatLang location) {
    this.location = location;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }
}
