package edmt.dev.androideatit.Model;

/**
 * Created by Minhaz on 2/10/2018.
 */

public class User {
    private String Name;
    private String Password;
    private String Phone;
    private String IsStaff;

    public  User(){

    }

    public User(String Pname, String Ppassword) {
        Name = Pname;
        Password = Ppassword;
        IsStaff="false";

    }

    public String getIsStaff() {
        return IsStaff;
    }

    public void setIsStaff(String isStaff) {
        IsStaff = isStaff;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password =password;
    }

    @Override
    public String toString() {
        return "User{" +
                "Name='" + Name + '\'' +
                ", Password='" + Password + '\'' +
                ", Phone='" + Phone + '\'' +
                ", IsStaff='" + IsStaff + '\'' +
                '}';
    }
}
