package com.jeyun.rhdms.handler.entity.wrapper;

import com.jeyun.rhdms.handler.entity.Pill;

import java.io.Serializable;
import java.util.ArrayList;

public class PillBox implements Serializable
{
    public ArrayList<Pill> values;

    public PillBox(ArrayList<Pill> values)
    {
        this.values = values;
    }
}
