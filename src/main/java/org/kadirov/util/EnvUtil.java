package org.kadirov.util;

import java.util.Map;

public final class EnvUtil {

    private EnvUtil(){}

    public static boolean exists(String envVarName){
        Map<String, String> envVars = System.getenv();

        for (Map.Entry<String, String> entry : envVars.entrySet()) {
            String envName = entry.getKey();

            if(envName.equals(envVarName))
                return true;
        }

        return false;
    }
}
