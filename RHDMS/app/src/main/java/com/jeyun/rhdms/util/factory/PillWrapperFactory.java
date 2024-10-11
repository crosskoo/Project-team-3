package com.jeyun.rhdms.util.factory;

import com.jeyun.rhdms.adapter.wrapper.PillInfo;
import com.jeyun.rhdms.handler.entity.Pill;

public class PillWrapperFactory implements WrapperFactory<Pill, PillInfo>
{
    @Override
    public PillInfo createWrapper(Pill data)
    {
          return new PillInfo(data);
    }
}