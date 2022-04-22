import com.google.gson.Gson;
import p2p.P2PInitialization;
import pojo.Node;
import utils.LocalUtils;

import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class Main {

    public static void main(String[] args) {

        P2PInitialization.initP2P();
        // System.out.println(LocalUtils.long2IPStr(2130706433));

    }

}