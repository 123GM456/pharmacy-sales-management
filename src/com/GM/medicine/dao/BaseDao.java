package com.GM.medicine.dao;

import java.util.List;

public interface BaseDao<T> {

    boolean add(T entity);

    boolean delete(Integer id);

    T findById(Integer id);

    List<T> findAll();
}