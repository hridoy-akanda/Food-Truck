package edmt.dev.androideatit.Common;

import edmt.dev.androideatit.Model.User;

/**
 * Created by Minhaz on 2/11/2018.
 */

public class Common {
    public static User currentUser;

    public static String convertCodeToStatus(String status) {
        if(status.equals("0"))
            return "Placed";
        else if(status.equals("1"))
            return "On my way";
        else
            return "Shipped";
    }
}
