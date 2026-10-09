package com.journal.table;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tables/{tableId}/columns")
public class ColumnController {
    private final ColumnService columnService;

    public ColumnController(ColumnService columnService) {
        this.columnService = columnService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ColumnDetail addColumn(@PathVariable Long tableId, @RequestBody CreateColumnRequest request) {
        return columnService.addColumn(tableId, request);
    }
}
