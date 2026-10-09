package com.journal.row.filter;

import java.util.List;

public class PageResponse<T> {
    private int page;
    private int pageSize;
    private int totalItems;
    private int totalPages;
    private List<T> items;

    public PageResponse(int page, int pageSize, int totalItems, int totalPages, List<T> items) {
        this.page = page;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.items = items;
    }

    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public int getTotalItems() { return totalItems; }
    public int getTotalPages() { return totalPages; }
    public List<T> getItems() { return items; }
}
