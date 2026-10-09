package com.journal.table;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tables")
public class TableController {
    private final TableService tableService;

    public TableController(TableService tableService) {
        this.tableService = tableService;
    }

    @GetMapping
    public List<TableDetail> listTables() {
        return tableService.listTables();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TableDetail createTable(@RequestBody CreateTableRequest request) {
        return tableService.createTable(request);
    }

    @GetMapping("/{tableId}")
    public TableDetail getTable(@PathVariable Long tableId) {
        return tableService.getTable(tableId);
    }
}
