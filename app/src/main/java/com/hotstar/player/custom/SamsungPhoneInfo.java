package com.hotstar.player.custom;

import android.os.Build;

import com.hotstar.player.HotStarApplication;
import com.hotstar.player.adplayer.AdVideoApplication;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SamsungPhoneInfo {
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME  + SamsungPhoneInfo.class.getSimpleName();
    private final String SAMSUNG_MANUFACTURER = "samsung";

    private ArrayList<String> galaxyS = new ArrayList<>();
    private ArrayList<String> galaxySII = new ArrayList<>();
    private ArrayList<String> galaxySIII = new ArrayList<>();
    private ArrayList<String> galaxyS4 = new ArrayList<>();
    private ArrayList<String> galaxyS5 = new ArrayList<>();
    private ArrayList<String> galaxyS6 = new ArrayList<>();
    private ArrayList<String> galaxyJ1 = new ArrayList<>();
    private ArrayList<String> galaxyJ2 = new ArrayList<>();
    private ArrayList<String> galaxyJ3 = new ArrayList<>();
    private ArrayList<String> galaxyJ5 = new ArrayList<>();
    private ArrayList<String> galaxyJ7 = new ArrayList<>();

    private static SamsungPhoneInfo instance = null;
    public static SamsungPhoneInfo getInstance() {
        if (instance == null)
            instance = new SamsungPhoneInfo();

        return instance;
    }

    public SamsungPhoneInfo() {
        initializeGalaxyS();
        initializeGalaxySII();
        initializeGalaxySIII();
        initializeGalaxyS4();
        initializeGalaxyS5();
        initializeGalaxyS6();
        initializeGalaxyJ1();
        initializeGalaxyJ2();
        initializeGalaxyJ3();
        initializeGalaxyJ5();
        initializeGalaxyJ7();
    }

    private void initializeGalaxyS() {
        // unbranded models
        galaxyS.add("GT-I9003");
        galaxyS.add("GT-I9000");
        galaxyS.add("GT-I9000B");
        galaxyS.add("GT-I9000M");
        galaxyS.add("GT-I9000T");

        // branded mode;s
        galaxyS.add("SGH-I997");
        galaxyS.add("SGH-I897");
        galaxyS.add("SGH-I896");
        galaxyS.add("SGH-T959");
        galaxyS.add("SCH-I500");
        galaxyS.add("SCH-S950C");
        galaxyS.add("SPH-D700");
        galaxyS.add("SCH-I405");
        galaxyS.add("SCH-R930");
        galaxyS.add("SCH-S720C");
        galaxyS.add("SCH-R910");
        galaxyS.add("SCH-R915");
    }

    private void initializeGalaxySII() {
        galaxySII.add("GT-I9100");
        galaxySII.add("GT-I9210");
        galaxySII.add("SGH-I757M");
        galaxySII.add("SGH-I727");
        galaxySII.add("SGH-I927");
        galaxySII.add("SGH-T989D");
        galaxySII.add("GT-I9108");
        galaxySII.add("GT-i9100");
        galaxySII.add("SCH-i919");
        galaxySII.add("ISW11SC");
        galaxySII.add("SC-02C");
        galaxySII.add("SHW-M250");
        galaxySII.add("SGH-I777");
        galaxySII.add("SPH-D710");
        galaxySII.add("SGH-T989");
        galaxySII.add("SCH-R760");

        // Galaxy S II plus
        galaxySII.add("GT-I9105");
    }

    private void initializeGalaxySIII() {
        galaxySIII.add("GT-I9300");
        galaxySIII.add("GT-I9305");
        galaxySIII.add("SHV-E210");
        galaxySIII.add("SGH-T999");
        galaxySIII.add("SGH-N064");
        galaxySIII.add("SGH-N035");
        galaxySIII.add("SCH-J021");
        galaxySIII.add("SCH-R530");
        galaxySIII.add("SCH-I535");
        galaxySIII.add("SCH-S960");
        galaxySIII.add("SCH-S968");
        galaxySIII.add("GT-I9308");
        galaxySIII.add("SCH-I939");
        galaxySIII.add("GT-I9301");
    }

    private void initializeGalaxyS4() {
        galaxyS4.add("GT-I9500");
        galaxyS4.add("SHV-E300");
        galaxyS4.add("SHV-E330");
        galaxyS4.add("GT-I9505");
        galaxyS4.add("GT-I9506");
        galaxyS4.add("SGH-I337");
        galaxyS4.add("SGH-M919");
        galaxyS4.add("SCH-I545");
        galaxyS4.add("SPH-L720");
        galaxyS4.add("SCH-R970");
        galaxyS4.add("GT-I9508");
        galaxyS4.add("SCH-I959");
        galaxyS4.add("GT-I9502");
        galaxyS4.add("SCH-N045");
    }

    private void initializeGalaxyS5() {
        galaxyS5.add("SM-G900");
    }

    private void initializeGalaxyS6() {
        galaxyS6.add("SM-G920");
        galaxyS6.add("SM-G925");
    }

    private void initializeGalaxyJ1() {
        galaxyJ1.add("SM-J100");
    }

    private void initializeGalaxyJ2() {
        galaxyJ2.add("SM-J200");
    }

    private void initializeGalaxyJ3() {
        galaxyJ3.add("SM-J300");
    }

    private void initializeGalaxyJ5() {
        galaxyJ5.add("SM-J500");
    }

    private void initializeGalaxyJ7() {
        galaxyJ7.add("SM-J700");
    }

    public boolean isSamsung() {
        return Build.MANUFACTURER.equalsIgnoreCase(SAMSUNG_MANUFACTURER);
    }

    public String modelString() {
        if (HotStarApplication.DEBUG == true)
            return "S5";

        String phoneModel = Build.MODEL;

        for (int i=0; i<galaxyS.size(); i++) {
            String model = galaxyS.get(i);
            if (phoneModel.contains(model))
                return "S";
        }
        for (int i=0; i<galaxySII.size(); i++) {
            String model = galaxySII.get(i);
            if (phoneModel.contains(model))
                return "S2";
        }
        for (int i=0; i<galaxySIII.size(); i++) {
            String model = galaxySIII.get(i);
            if (phoneModel.contains(model))
                return "S3";
        }
        for (int i=0; i<galaxyS4.size(); i++) {
            String model = galaxyS4.get(i);
            if (phoneModel.contains(model))
                return "S4";
        }
        for (int i=0; i<galaxyS5.size(); i++) {
            String model = galaxyS5.get(i);
            if (phoneModel.contains(model))
                return "S5";
        }
        for (int i=0; i<galaxyS6.size(); i++) {
            String model = galaxyS6.get(i);
            if (phoneModel.contains(model))
                return "S6";
        }
        for (int i=0; i<galaxyJ1.size(); i++) {
            String model = galaxyJ1.get(i);
            if (phoneModel.contains(model))
                return "J1";
        }
        for (int i=0; i<galaxyJ2.size(); i++) {
            String model = galaxyJ2.get(i);
            if (phoneModel.contains(model))
                return "J2";
        }
        for (int i=0; i<galaxyJ3.size(); i++) {
            String model = galaxyJ3.get(i);
            if (phoneModel.contains(model))
                return "J3";
        }
        for (int i=0; i<galaxyJ5.size(); i++) {
            String model = galaxyJ5.get(i);
            if (phoneModel.contains(model))
                return "J5";
        }
        for (int i=0; i<galaxyJ7.size(); i++) {
            String model = galaxyJ7.get(i);
            if (phoneModel.contains(model))
                return "J7";
        }

        return "XX";
    }
}