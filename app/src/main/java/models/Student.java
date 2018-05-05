package models;

import com.google.firebase.database.Exclude;

/**
 * Created by Hasthi on 5/5/2018.
 */

public class Student {

    private String email;
    private String firstName;
    private String LastName;
    private String mobileNumber;
    private String studentID;

    private int userType = 2;

    private static Student instance;

    private Student() {
    }

    public static Student getInstance(){
        if(instance == null){
            instance = new Student();
        }
        return instance;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public int getUserType() {
        return userType;
    }

    @Exclude
    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }
}
