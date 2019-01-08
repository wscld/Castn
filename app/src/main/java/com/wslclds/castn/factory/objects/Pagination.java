package com.wslclds.castn.factory.objects;

public class Pagination {
    int limit;
    int begin;

    public Pagination(int limit, int begin){
        this.begin = begin;
        this.limit = limit;
    }

    public Pagination(){
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }
}
