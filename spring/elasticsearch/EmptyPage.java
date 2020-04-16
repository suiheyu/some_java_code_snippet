package com.inspur.bss.waf.elasticsearch;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * @author hexinyu
 * @create 2020/03/17 15:08
 */
public class EmptyPage implements Pageable {
    public static final EmptyPage INSTANCE = new EmptyPage();

    @Override
    public int getPageNumber() {
        return 0;
    }

    @Override
    public int getPageSize() {
        return 0;
    }

    @Override
    public long getOffset() {
        return 0;
    }

    @Override
    public Sort getSort() {
        return null;
    }

    @Override
    public Pageable next() {
        return null;
    }

    @Override
    public Pageable previousOrFirst() {
        return null;
    }

    @Override
    public Pageable first() {
        return null;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }
}