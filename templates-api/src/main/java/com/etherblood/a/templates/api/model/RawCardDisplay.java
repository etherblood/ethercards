package com.etherblood.ethercards.templates.api.model;

import com.etherblood.ethercards.templates.api.CardColor;
import java.util.List;

public class RawCardDisplay {

    public String name, flavourText, description, imagePath;
    public int frames = 1;
    public int loopMillis = 1000;
    public List<CardColor> colors;

}
