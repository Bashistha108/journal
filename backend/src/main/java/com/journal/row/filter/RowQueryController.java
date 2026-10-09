package com.journal.row.filter;

import com.journal.row.RowListItem;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tables/{tableId}/rows")
public class RowQueryController {

    private final RowQueryService queryService;

    public RowQueryController(RowQueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping("/query")
    public PageResponse<RowListItem> query(
            @PathVariable Long tableId,
            @RequestBody QueryRequest request) {
        return queryService.query(tableId, request);
    }
}
