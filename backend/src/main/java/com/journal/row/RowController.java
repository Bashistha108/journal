package com.journal.row;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tables/{tableId}/rows")
public class RowController {
    
    private final RowService rowService;

    public RowController(RowService rowService) {
        this.rowService = rowService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RowDetail createRow(@PathVariable Long tableId, @RequestBody RowSaveRequest request) {
        return rowService.createRow(tableId, request);
    }

    @GetMapping("/{rowId}")
    public RowDetail getRow(@PathVariable Long tableId, @PathVariable Long rowId) {
        return rowService.getRow(tableId, rowId);
    }

    @PatchMapping("/{rowId}")
    public RowDetail updateRow(@PathVariable Long tableId, @PathVariable Long rowId, @RequestBody RowSaveRequest request) {
        return rowService.updateRow(tableId, rowId, request);
    }

    @DeleteMapping("/{rowId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRow(@PathVariable Long tableId, @PathVariable Long rowId) {
        rowService.deleteRow(tableId, rowId);
    }
}
