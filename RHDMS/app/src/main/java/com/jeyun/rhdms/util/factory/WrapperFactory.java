package com.jeyun.rhdms.util.factory;

public interface WrapperFactory<T, R>
{
    R createWrapper(T data);
}