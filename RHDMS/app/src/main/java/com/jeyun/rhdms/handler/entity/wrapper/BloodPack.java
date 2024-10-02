package com.jeyun.rhdms.handler.entity.wrapper;

import com.jeyun.rhdms.handler.entity.Blood;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BloodPack implements Serializable
{
    public ArrayList<Blood> values;
    public BloodPack(ArrayList<Blood> list)
    {
        this.values = new ArrayList<>();
        if (Objects.nonNull(list)) this.values.addAll(list);
    }
}
